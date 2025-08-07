package com.mxai.jslt.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mxai.jslt.core.JsltTransformationService;
import com.mxai.jslt.model.TransformationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.UUID;

/**
 * MCP (Model Context Protocol) server service that exposes JSLT transformation
 * as MCP tools for AI models to invoke.
 * 
 * Implements the MCP JSON-RPC protocol over stdio transport for Claude Desktop integration.
 */
@Service
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true", matchIfMissing = true)
public class McpServerService {
    
    private static final Logger logger = LoggerFactory.getLogger(McpServerService.class);
    
    private final JsltTransformationService jsltService;
    private final McpServerConfiguration config;
    private final ObjectMapper objectMapper;
    
    public McpServerService(JsltTransformationService jsltService, 
                           McpServerConfiguration config,
                           ObjectMapper objectMapper) {
        this.jsltService = jsltService;
        this.config = config;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Start the MCP server with stdio transport
     */
    public void startStdioServer() {
        if (!config.getStdio().isEnabled()) {
            logger.info("MCP stdio transport is disabled");
            return;
        }
        
        logger.info("Starting MCP server with stdio transport");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(System.out, true)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonNode request = objectMapper.readTree(line);
                    JsonNode response = handleRequest(request);
                    
                    if (response != null) {
                        writer.println(objectMapper.writeValueAsString(response));
                    }
                } catch (Exception e) {
                    logger.error("Error processing MCP request: {}", line, e);
                    JsonNode errorResponse = createErrorResponse(null, -32603, "Internal error", e.getMessage());
                    writer.println(objectMapper.writeValueAsString(errorResponse));
                }
            }
        } catch (IOException e) {
            logger.error("Error in MCP stdio server", e);
        }
    }
    
    /**
     * Handle incoming MCP JSON-RPC request
     */
    JsonNode handleRequest(JsonNode request) {
        String method = request.path("method").asText();
        JsonNode params = request.path("params");
        JsonNode id = request.path("id");
        
        logger.debug("Handling MCP request: method={}, id={}", method, id);
        
        try {
            return switch (method) {
                case "initialize" -> handleInitialize(params, id);
                case "tools/list" -> handleToolsList(params, id);
                case "tools/call" -> handleToolsCall(params, id);
                default -> createErrorResponse(id, -32601, "Method not found", "Unknown method: " + method);
            };
        } catch (Exception e) {
            logger.error("Error handling MCP method: {}", method, e);
            return createErrorResponse(id, -32603, "Internal error", e.getMessage());
        }
    }
    
    /**
     * Handle MCP initialize request
     */
    private JsonNode handleInitialize(JsonNode params, JsonNode id) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");
        
        ObjectNode capabilities = objectMapper.createObjectNode();
        ObjectNode tools = objectMapper.createObjectNode();
        tools.put("listChanged", false);
        capabilities.set("tools", tools);
        result.set("capabilities", capabilities);
        
        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", config.getName());
        serverInfo.put("version", config.getVersion());
        result.set("serverInfo", serverInfo);
        
        return createSuccessResponse(id, result);
    }
    
    /**
     * Handle tools/list request - return available MCP tools
     */
    private JsonNode handleToolsList(JsonNode params, JsonNode id) {
        ArrayNode tools = objectMapper.createArrayNode();
        
        // JSLT Transform Tool
        ObjectNode jsltTool = objectMapper.createObjectNode();
        jsltTool.put("name", "jslt_transform");
        jsltTool.put("description", "Transform JSON data using JSLT (JSON Stylesheet Language Transformations). " +
                                   "Provide JSON data and a JSLT query to transform the data structure.");
        
        ObjectNode inputSchema = objectMapper.createObjectNode();
        inputSchema.put("type", "object");
        
        ObjectNode properties = objectMapper.createObjectNode();
        
        // jsonData property
        ObjectNode jsonDataProp = objectMapper.createObjectNode();
        jsonDataProp.put("type", "string");
        jsonDataProp.put("description", "JSON data to transform (as JSON string or object)");
        properties.set("jsonData", jsonDataProp);
        
        // jsltQuery property
        ObjectNode jsltQueryProp = objectMapper.createObjectNode();
        jsltQueryProp.put("type", "string");
        jsltQueryProp.put("description", "JSLT transformation query");
        properties.set("jsltQuery", jsltQueryProp);
        
        // prettyPrint property (optional)
        ObjectNode prettyPrintProp = objectMapper.createObjectNode();
        prettyPrintProp.put("type", "boolean");
        prettyPrintProp.put("description", "Whether to pretty-print the output JSON (default: true)");
        properties.set("prettyPrint", prettyPrintProp);
        
        // returnAsString property (optional)
        ObjectNode returnAsStringProp = objectMapper.createObjectNode();
        returnAsStringProp.put("type", "boolean");
        returnAsStringProp.put("description", "Whether to return result as JSON string instead of object (default: false)");
        properties.set("returnAsString", returnAsStringProp);
        
        inputSchema.set("properties", properties);
        
        ArrayNode required = objectMapper.createArrayNode();
        required.add("jsonData");
        required.add("jsltQuery");
        inputSchema.set("required", required);
        
        jsltTool.set("inputSchema", inputSchema);
        tools.add(jsltTool);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.set("tools", tools);
        
        return createSuccessResponse(id, result);
    }
    
    /**
     * Handle tools/call request - execute the requested tool
     */
    private JsonNode handleToolsCall(JsonNode params, JsonNode id) {
        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");
        
        if (!"jslt_transform".equals(toolName)) {
            return createErrorResponse(id, -32602, "Invalid params", "Unknown tool: " + toolName);
        }
        
        try {
            // Extract parameters
            String jsonDataStr = arguments.path("jsonData").asText();
            String jsltQuery = arguments.path("jsltQuery").asText();
            boolean prettyPrint = arguments.path("prettyPrint").asBoolean(true);
            boolean returnAsString = arguments.path("returnAsString").asBoolean(false);
            
            if (jsonDataStr.isEmpty() || jsltQuery.isEmpty()) {
                return createErrorResponse(id, -32602, "Invalid params", "jsonData and jsltQuery are required");
            }
            
            // Parse JSON data
            JsonNode jsonData;
            try {
                jsonData = objectMapper.readTree(jsonDataStr);
            } catch (Exception e) {
                // If parsing fails, treat as string literal
                jsonData = objectMapper.valueToTree(jsonDataStr);
            }
            
            // Perform JSLT transformation
            TransformationResult transformResult = jsltService.transformJson(jsonData, jsltQuery);
            
            // Prepare result based on transformation outcome
            ObjectNode result = objectMapper.createObjectNode();
            result.put("requestId", UUID.randomUUID().toString());
            result.put("timestamp", Instant.now().toString());
            result.put("success", transformResult.success());
            
            if (transformResult.success()) {
                result.putNull("errorMessage");
                
                if (returnAsString) {
                    try {
                        String resultStr = prettyPrint ? 
                            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(transformResult.transformedJson()) :
                            objectMapper.writeValueAsString(transformResult.transformedJson());
                        result.put("result", resultStr);
                    } catch (JsonProcessingException e) {
                        logger.warn("Failed to convert result to string, returning as JSON object", e);
                        result.set("result", transformResult.transformedJson());
                    }
                } else {
                    result.set("result", transformResult.transformedJson());
                }
            } else {
                result.put("errorMessage", transformResult.errorMessage());
                result.putNull("result");
            }
            
            // Wrap in MCP tool result format
            ObjectNode toolResult = objectMapper.createObjectNode();
            ArrayNode content = objectMapper.createArrayNode();
            
            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            content.add(textContent);
            
            toolResult.set("content", content);
            
            return createSuccessResponse(id, toolResult);
            
        } catch (Exception e) {
            logger.error("Error executing JSLT transformation", e);
            
            // Create error result
            ObjectNode errorResult = objectMapper.createObjectNode();
            errorResult.put("requestId", UUID.randomUUID().toString());
            errorResult.put("timestamp", Instant.now().toString());
            errorResult.put("success", false);
            errorResult.put("errorMessage", e.getMessage());
            errorResult.putNull("result");
            
            // Wrap in MCP tool result format
            ObjectNode toolResult = objectMapper.createObjectNode();
            ArrayNode content = objectMapper.createArrayNode();
            
            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            try {
                textContent.put("text", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorResult));
            } catch (JsonProcessingException jsonEx) {
                textContent.put("text", "Error processing transformation result: " + e.getMessage());
            }
            content.add(textContent);
            
            toolResult.set("content", content);
            
            return createSuccessResponse(id, toolResult);
        }
    }
    
    /**
     * Create a successful JSON-RPC response
     */
    private JsonNode createSuccessResponse(JsonNode id, JsonNode result) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        return response;
    }
    
    /**
     * Create an error JSON-RPC response
     */
    private JsonNode createErrorResponse(JsonNode id, int code, String message, String data) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        if (data != null) {
            error.put("data", data);
        }
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("error", error);
        return response;
    }
}
