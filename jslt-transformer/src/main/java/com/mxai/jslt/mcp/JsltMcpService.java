package com.mxai.jslt.mcp;

import com.mxai.jslt.core.JsltTransformationService;
import com.mxai.jslt.model.TransformationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * MCP service that exposes JSLT transformation functionality as MCP tools.
 * 
 * This service integrates the core JSLT transformation service with Spring AI's
 * MCP framework, allowing AI models to perform JSON transformations using JSLT
 * queries through the Model Context Protocol.
 * 
 * The service exposes a single tool:
 * - jslt_transform: Transforms JSON data using JSLT transformation queries
 * 
 * @author JSLT Transformer Team
 * @version 1.0
 * @since 2025-08-08
 */
@Service
public class JsltMcpService {
    
    private static final Logger logger = LoggerFactory.getLogger(JsltMcpService.class);
    
    private final JsltTransformationService jsltService;
    
    @Autowired
    public JsltMcpService(JsltTransformationService jsltService) {
        this.jsltService = jsltService;
        logger.info("JSLT MCP Service initialized");
    }
    
    /**
     * JSLT transformation tool exposed via MCP.
     * 
     * This tool allows AI models to transform JSON data using JSLT queries.
     * The tool accepts JSON input data and a JSLT transformation query,
     * then returns the transformed result.
     * 
     * @param inputJson The JSON data to transform
     * @param jsltQuery The JSLT transformation query
     * @return JsltTransformResponse with transformation result
     */
    @Tool(description = "Transform JSON data using JSLT (JSON Transformation Language). " +
                       "Provide input JSON data and a JSLT transformation query to get the transformed result. " +
                       "JSLT supports complex transformations, filtering, mapping, and restructuring of JSON data.")
    public JsltTransformResponse jsltTransform(String inputJson, String jsltQuery) {
        logger.info("Received JSLT transformation request via MCP");
        logger.debug("Input JSON length: {} characters", 
                    inputJson != null ? inputJson.length() : 0);
        logger.debug("JSLT query length: {} characters", 
                    jsltQuery != null ? jsltQuery.length() : 0);
        
        try {
            // Perform the transformation using the core service
            TransformationResult result = jsltService.transformFromStrings(inputJson, jsltQuery);
            
            // Convert to MCP response format
            JsltTransformResponse response = new JsltTransformResponse(
                result.success(),
                result.transformedJson() != null ? result.transformedJson().toString() : null,
                result.errorMessage(),
                result.processingTimeMs()
            );
            
            logger.info("JSLT transformation completed via MCP - Success: {}, Time: {}ms", 
                       result.success(), result.processingTimeMs());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Unexpected error during JSLT transformation via MCP", e);
            return new JsltTransformResponse(
                false,
                null,
                "Unexpected error: " + e.getMessage(),
                0L
            );
        }
    }
    
    /**
     * Creates the ToolCallbackProvider bean for MCP server.
     * 
     * @return ToolCallbackProvider containing JSLT transformation tools
     */
    @Bean
    public ToolCallbackProvider jsltTools() {
        return MethodToolCallbackProvider.builder()
            .toolObjects(this)
            .build();
    }
    

    
    /**
     * Response record for JSLT transformation via MCP.
     * 
     * @param success Whether the transformation was successful
     * @param result The transformed JSON result (if successful)
     * @param errorMessage Error message (if transformation failed)
     * @param executionTimeMs Execution time in milliseconds
     */
    public record JsltTransformResponse(
        boolean success,
        String result,
        String errorMessage,
        long executionTimeMs
    ) {}
}
