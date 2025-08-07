package com.mxai.jslt.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxai.jslt.core.JsltTransformationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Manual test for MCP server functionality.
 * This test uses real objects instead of mocks to validate MCP integration.
 */
public class McpServerManualTest {
    
    private McpServerService mcpServerService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        JsltTransformationService jsltService = new JsltTransformationService();
        McpServerConfiguration config = createTestConfig();
        objectMapper = new ObjectMapper();
        mcpServerService = new McpServerService(jsltService, config, objectMapper);
    }
    
    private McpServerConfiguration createTestConfig() {
        McpServerConfiguration config = new McpServerConfiguration();
        config.setEnabled(true);
        config.setName("Test MCP Server");
        config.setVersion("1.0.0");
        config.setDescription("Test MCP server for JSLT transformations");
        
        McpServerConfiguration.StdioTransport stdio = new McpServerConfiguration.StdioTransport();
        stdio.setEnabled(true);
        config.setStdio(stdio);
        
        McpServerConfiguration.HttpTransport http = new McpServerConfiguration.HttpTransport();
        http.setEnabled(false);
        http.setPort(8081);
        config.setHttp(http);
        
        return config;
    }
    
    private JsonNode createJsonRequest(String jsonString) throws Exception {
        return objectMapper.readTree(jsonString);
    }
    
    @Test
    void testInitializeMethod() throws Exception {
        // Arrange
        String initRequest = """
            {
                "jsonrpc": "2.0",
                "method": "initialize",
                "id": 1,
                "params": {
                    "protocolVersion": "2024-11-05",
                    "capabilities": {},
                    "clientInfo": {
                        "name": "test-client",
                        "version": "1.0.0"
                    }
                }
            }
            """;
        
        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(initRequest));
        
        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(1, response.get("id").asInt());
        assertNotNull(response.get("result"));
        
        JsonNode result = response.get("result");
        assertEquals("2024-11-05", result.get("protocolVersion").asText());
        assertNotNull(result.get("capabilities"));
        assertNotNull(result.get("serverInfo"));
        assertEquals("Test MCP Server", result.get("serverInfo").get("name").asText());
        assertEquals("1.0.0", result.get("serverInfo").get("version").asText());
        
        System.out.println("✓ Initialize method test passed");
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
    
    @Test
    void testToolsListMethod() throws Exception {
        // Arrange
        String toolsListRequest = """
            {
                "jsonrpc": "2.0",
                "method": "tools/list",
                "id": 2,
                "params": {}
            }
            """;
        
        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(toolsListRequest));
        
        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(2, response.get("id").asInt());
        assertNotNull(response.get("result"));
        
        JsonNode result = response.get("result");
        assertNotNull(result.get("tools"));
        assertTrue(result.get("tools").isArray());
        assertEquals(1, result.get("tools").size());
        
        JsonNode jsltTool = result.get("tools").get(0);
        assertEquals("jslt_transform", jsltTool.get("name").asText());
        assertTrue(jsltTool.get("description").asText().contains("JSLT"));
        assertNotNull(jsltTool.get("inputSchema"));
        
        System.out.println("✓ Tools list method test passed");
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
    
    @Test
    void testToolsCallMethodWithSuccess() throws Exception {
        // Arrange
        String toolsCallRequest = """
            {
                "jsonrpc": "2.0",
                "method": "tools/call",
                "id": 3,
                "params": {
                    "name": "jslt_transform",
                    "arguments": {
                        "jsonData": "{\\"name\\": \\"John\\", \\"age\\": 30}",
                        "jsltQuery": ".name"
                    }
                }
            }
            """;
        
        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(toolsCallRequest));
        
        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(3, response.get("id").asInt());
        assertNotNull(response.get("result"));
        
        JsonNode result = response.get("result");
        assertNotNull(result.get("content"));
        assertTrue(result.get("content").isArray());
        assertEquals(1, result.get("content").size());
        
        JsonNode content = result.get("content").get(0);
        assertEquals("text", content.get("type").asText());
        assertTrue(content.get("text").asText().contains("John"));
        
        System.out.println("✓ Tools call method with success test passed");
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
    
    @Test
    void testToolsCallMethodWithInvalidTool() throws Exception {
        // Arrange
        String toolsCallRequest = """
            {
                "jsonrpc": "2.0",
                "method": "tools/call",
                "id": 4,
                "params": {
                    "name": "invalid_tool",
                    "arguments": {}
                }
            }
            """;
        
        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(toolsCallRequest));
        
        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(4, response.get("id").asInt());
        assertNotNull(response.get("error"));
        
        JsonNode error = response.get("error");
        assertEquals(-32602, error.get("code").asInt());
        assertEquals("Invalid params", error.get("message").asText());
        assertTrue(error.get("data").asText().contains("Unknown tool"));
        
        System.out.println("✓ Tools call method with invalid tool test passed");
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
    
    @Test
    void testInvalidMethod() throws Exception {
        // Arrange
        String invalidRequest = """
            {
                "jsonrpc": "2.0",
                "method": "invalid_method",
                "id": 5,
                "params": {}
            }
            """;
        
        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(invalidRequest));
        
        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(5, response.get("id").asInt());
        assertNotNull(response.get("error"));
        
        JsonNode error = response.get("error");
        assertEquals(-32601, error.get("code").asInt());
        assertEquals("Method not found", error.get("message").asText());
        assertTrue(error.get("data").asText().contains("Unknown method"));
        
        System.out.println("✓ Invalid method test passed");
        System.out.println("Response: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
}
