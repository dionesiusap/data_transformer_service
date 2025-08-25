package com.mxai.jslt.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mxai.jslt.core.JsltTransformationService;
import com.mxai.jslt.model.TransformationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * Standalone MCP (Model Context Protocol) server main class.
 * 
 * @deprecated This standalone MCP server has been replaced by SpringMcpServerApplication.
 *             Use SpringMcpServerApplication for new deployments as it provides better
 *             integration with Spring AI MCP framework and improved tool management.
 *             This class is kept for backward compatibility but may be removed in future versions.
 * 
 * This server provides JSLT transformation capabilities via the MCP protocol,
 * enabling integration with AI applications like Claude Desktop.
 * 
 * Key features:
 * - Stdio transport for Claude Desktop integration
 * - JSON-RPC 2.0 protocol implementation
 * - File-only logging to avoid stdio interference
 * - Standalone operation (no Spring Boot dependency)
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 * @see SpringMcpServerApplication
 */
public class McpServerMain {
    
    private static final Logger logger = LoggerFactory.getLogger(McpServerMain.class);
    
    // MCP server metadata
    private static final String SERVER_NAME = "JSLT Transformer";
    private static final String SERVER_VERSION = "1.0.0";
    private static final String PROTOCOL_VERSION = "2024-11-05";
    
    private final JsltTransformationService jsltService;
    private final ObjectMapper objectMapper;
    
    public McpServerMain() {
        this.jsltService = new JsltTransformationService();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Main entry point for the standalone MCP server.
     * 
     * @param args command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        // Programmatically disable console logging to avoid stdout interference with MCP JSON-RPC
        disableConsoleLogging();
        
        McpServerMain server = new McpServerMain();
        server.startStdioServer();
    }
    
    /**
     * Programmatically disable console logging to prevent stdout interference.
     * This is critical for MCP stdio transport which uses stdout for JSON-RPC communication.
     */
    private static void disableConsoleLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // Remove console appenders from root logger
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAppender("CONSOLE");
        
        // Remove console appenders from our application logger
        ch.qos.logback.classic.Logger appLogger = context.getLogger("com.mxai.jslt");
        appLogger.detachAppender("CONSOLE");
    }
    
    /**
     * Start the MCP server with stdio transport.
     * This method blocks and handles MCP requests via stdin/stdout.
     */
    public void startStdioServer() {
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
     * Handle incoming MCP JSON-RPC request.
     * 
     * @param request the JSON-RPC request
     * @return JSON-RPC response or null if no response needed
     */
    JsonNode handleRequest(JsonNode request) {
        String method = request.path("method").asText();
        JsonNode params = request.path("params");
        JsonNode id = request.path("id");
        
        logger.debug("Handling MCP request: method={}, id={}", method, id);
        
        try {
            return switch (method) {
                case "initialize" -> handleInitialize(params, id);
                case "initialized" -> handleInitialized(params, id); // Notification, no response needed
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
     * Handle MCP initialize request.
     */
    private JsonNode handleInitialize(JsonNode params, JsonNode id) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);
        
        ObjectNode capabilities = objectMapper.createObjectNode();
        ObjectNode tools = objectMapper.createObjectNode();
        tools.put("listChanged", false);
        capabilities.set("tools", tools);
        result.set("capabilities", capabilities);
        
        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);
        result.set("serverInfo", serverInfo);
        
        return createSuccessResponse(id, result);
    }
    
    /**
     * Handle MCP initialized notification.
     * This is a notification (no response required) sent after initialize.
     */
    private JsonNode handleInitialized(JsonNode params, JsonNode id) {
        logger.info("MCP client initialized successfully");
        // Notifications don't require a response
        return null;
    }
    
    /**
     * Handle tools/list request - return available MCP tools.
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
        prettyPrintProp.put("description", "Format output with pretty printing");
        prettyPrintProp.put("default", false);
        properties.set("prettyPrint", prettyPrintProp);
        
        // returnAsString property (optional)
        ObjectNode returnAsStringProp = objectMapper.createObjectNode();
        returnAsStringProp.put("type", "boolean");
        returnAsStringProp.put("description", "Return result as JSON string instead of object");
        returnAsStringProp.put("default", false);
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
     * Handle tools/call request - execute the specified tool.
     */
    private JsonNode handleToolsCall(JsonNode params, JsonNode id) {
        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");
        
        if (!"jslt_transform".equals(toolName)) {
            return createErrorResponse(id, -32602, "Invalid params", "Unknown tool: " + toolName);
        }
        
        try {
            // Extract parameters
            String jsonData = arguments.path("jsonData").asText();
            String jsltQuery = arguments.path("jsltQuery").asText();
            boolean prettyPrint = arguments.path("prettyPrint").asBoolean(false);
            boolean returnAsString = arguments.path("returnAsString").asBoolean(false);
            
            if (jsonData.isEmpty() || jsltQuery.isEmpty()) {
                return createErrorResponse(id, -32602, "Invalid params", "Missing required parameters: jsonData and jsltQuery");
            }
            
            // Perform transformation
            TransformationResult transformResult = jsltService.transformFromStrings(jsonData, jsltQuery);
            
            // Create MCP response with consistent format
            ObjectNode mcpResult = objectMapper.createObjectNode();
            mcpResult.put("requestId", UUID.randomUUID().toString());
            mcpResult.put("timestamp", java.time.Instant.now().toString());
            mcpResult.put("success", transformResult.success());
            
            if (transformResult.success()) {
                mcpResult.putNull("errorMessage");
                
                // Format result based on returnAsString preference
                if (returnAsString) {
                    String resultStr = jsltService.formatJsonAsString(transformResult.transformedJson(), prettyPrint);
                    mcpResult.put("result", resultStr);
                } else {
                    mcpResult.set("result", transformResult.transformedJson());
                }
            } else {
                mcpResult.put("errorMessage", transformResult.errorMessage());
                mcpResult.putNull("result");
            }
            
            // Wrap in MCP content format
            ArrayNode content = objectMapper.createArrayNode();
            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", objectMapper.writeValueAsString(mcpResult));
            content.add(textContent);
            
            ObjectNode result = objectMapper.createObjectNode();
            result.set("content", content);
            
            return createSuccessResponse(id, result);
            
        } catch (Exception e) {
            logger.error("Error executing jslt_transform tool", e);
            return createErrorResponse(id, -32603, "Internal error", "Tool execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Create a successful JSON-RPC response.
     */
    private JsonNode createSuccessResponse(JsonNode id, JsonNode result) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);
        response.set("result", result);
        return response;
    }
    
    /**
     * Create an error JSON-RPC response.
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
