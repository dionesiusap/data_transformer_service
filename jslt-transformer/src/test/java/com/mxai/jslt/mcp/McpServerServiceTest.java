package com.mxai.jslt.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mxai.jslt.core.JsltTransformationService;
import com.mxai.jslt.model.TransformationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for McpServerService.
 * Tests MCP JSON-RPC protocol implementation and JSLT transformation integration.
 */
@ExtendWith(MockitoExtension.class)
class McpServerServiceTest {

    @Mock
    private JsltTransformationService jsltService;

    private McpServerService mcpServerService;
    private JsltTransformationService mockJsltService;
    private McpServerConfiguration mockConfig;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        mockJsltService = mock(JsltTransformationService.class);
        mockConfig = mock(McpServerConfiguration.class);
        objectMapper = new ObjectMapper();
        mcpServerService = new McpServerService(mockJsltService, mockConfig, objectMapper);
    }
    
    private JsonNode createJsonRequest(String jsonString) throws Exception {
        return objectMapper.readTree(jsonString);
    }

    @Test
    void testInitializeMethod() throws Exception {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "initialize");
        request.put("id", 1);
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        params.put("clientInfo", "Test Client");
        request.set("params", params);

        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(request.toString()));

        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(1, response.get("id").asInt());
        assertTrue(response.has("result"));
        
        JsonNode result = response.get("result");
        assertEquals("2024-11-05", result.get("protocolVersion").asText());
        assertTrue(result.has("serverInfo"));
        
        JsonNode serverInfo = result.get("serverInfo");
        assertEquals("JSLT Transformation Service", serverInfo.get("name").asText());
        assertEquals("1.0.0", serverInfo.get("version").asText());
    }

    @Test
    void testToolsListMethod() throws Exception {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/list");
        request.put("id", 2);

        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(request.toString()));

        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(2, response.get("id").asInt());
        assertTrue(response.has("result"));
        
        JsonNode result = response.get("result");
        assertTrue(result.has("tools"));
        assertTrue(result.get("tools").isArray());
        assertEquals(1, result.get("tools").size());
        
        JsonNode tool = result.get("tools").get(0);
        assertEquals("jslt_transform", tool.get("name").asText());
        assertEquals("Transform JSON data using JSLT queries", tool.get("description").asText());
        assertTrue(tool.has("inputSchema"));
    }

    @Test
    void testToolsCallMethodSuccess() throws Exception {
        // Arrange
        ObjectNode inputJson = objectMapper.createObjectNode();
        inputJson.put("name", "John");
        inputJson.put("age", 30);
        
        ObjectNode transformedJson = objectMapper.createObjectNode();
        transformedJson.put("fullName", "John");
        transformedJson.put("years", 30);
        
        TransformationResult successResult = TransformationResult.success(transformedJson, 100L);
        when(jsltService.transformJson(any(JsonNode.class), anyString())).thenReturn(successResult);

        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 3);
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "jslt_transform");
        
        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.set("jsonData", inputJson);
        arguments.put("jsltQuery", ".name as $name | .age as $age | {\"fullName\": $name, \"years\": $age}");
        arguments.put("prettyPrint", false);
        arguments.put("returnAsString", false);
        params.set("arguments", arguments);
        
        request.set("params", params);

        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(request.toString()));

        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(3, response.get("id").asInt());
        assertTrue(response.has("result"));
        
        JsonNode result = response.get("result");
        assertTrue(result.has("content"));
        assertTrue(result.get("content").isArray());
        
        JsonNode content = result.get("content").get(0);
        assertEquals("text", content.get("type").asText());
        
        // Parse the text content as JSON to verify the transformation result
        String textContent = content.get("text").asText();
        JsonNode parsedResult = objectMapper.readTree(textContent);
        assertTrue(parsedResult.get("success").asBoolean());
        assertNull(parsedResult.get("errorMessage").asText(null));
        assertNotNull(parsedResult.get("result"));
    }

    @Test
    void testToolsCallMethodWithTransformationFailure() throws Exception {
        // Arrange
        TransformationResult failureResult = TransformationResult.failure("Invalid JSLT query", 50L);
        when(jsltService.transformJson(any(JsonNode.class), anyString())).thenReturn(failureResult);

        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 4);
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "jslt_transform");
        
        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.put("jsonData", "{}");
        arguments.put("jsltQuery", "invalid query");
        arguments.put("prettyPrint", false);
        arguments.put("returnAsString", false);
        params.set("arguments", arguments);
        
        request.set("params", params);

        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(request.toString()));

        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(4, response.get("id").asInt());
        assertTrue(response.has("result"));
        
        JsonNode result = response.get("result");
        assertTrue(result.has("content"));
        
        JsonNode content = result.get("content").get(0);
        assertEquals("text", content.get("type").asText());
        
        // Parse the text content as JSON to verify the error result
        String textContent = content.get("text").asText();
        JsonNode parsedResult = objectMapper.readTree(textContent);
        assertFalse(parsedResult.get("success").asBoolean());
        assertEquals("Invalid JSLT query", parsedResult.get("errorMessage").asText());
        assertTrue(parsedResult.get("result").isNull());
    }

    @Test
    void testToolsCallMethodWithStringInput() throws Exception {
        // Arrange
        ObjectNode transformedJson = objectMapper.createObjectNode();
        transformedJson.put("processed", true);
        
        TransformationResult successResult = TransformationResult.success(transformedJson, 75L);
        when(jsltService.transformJson(any(JsonNode.class), anyString())).thenReturn(successResult);

        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 5);
        
        ObjectNode params = objectMapper.createObjectNode();
        params.put("name", "jslt_transform");
        
        ObjectNode arguments = objectMapper.createObjectNode();
        arguments.put("jsonData", "{\"input\": \"test\"}");  // String input
        arguments.put("jsltQuery", "{\"processed\": true}");
        arguments.put("prettyPrint", true);
        arguments.put("returnAsString", true);
        params.set("arguments", arguments);
        
        request.set("params", params);

        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(request.toString()));

        // Assert
        assertNotNull(response);
        assertTrue(response.has("result"));
        
        JsonNode result = response.get("result");
        JsonNode content = result.get("content").get(0);
        String textContent = content.get("text").asText();
        JsonNode parsedResult = objectMapper.readTree(textContent);
        
        assertTrue(parsedResult.get("success").asBoolean());
        // Result should be a string when returnAsString is true
        assertTrue(parsedResult.get("result").isTextual());
    }

    @Test
    void testInvalidMethodError() throws Exception {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "invalid/method");
        request.put("id", 6);

        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(request.toString()));

        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertEquals(6, response.get("id").asInt());
        assertTrue(response.has("error"));
        
        JsonNode error = response.get("error");
        assertEquals(-32601, error.get("code").asInt());
        assertEquals("Method not found", error.get("message").asText());
    }

    @Test
    void testInvalidJsonError() throws Exception {
        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest("{\"method\": \"initialize\", \"id\": 1}"));

        // Assert
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc").asText());
        assertTrue(response.get("id").isNull());
        assertTrue(response.has("error"));
        
        JsonNode error = response.get("error");
        assertEquals(-32700, error.get("code").asInt());
        assertEquals("Parse error", error.get("message").asText());
    }

    @Test
    void testMissingToolNameError() throws Exception {
        // Arrange
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 7);
        
        ObjectNode params = objectMapper.createObjectNode();
        // Missing tool name
        request.set("params", params);

        // Act
        JsonNode response = mcpServerService.handleRequest(createJsonRequest(request.toString()));

        // Assert
        assertNotNull(response);
        assertTrue(response.has("error"));
        
        JsonNode error = response.get("error");
        assertEquals(-32602, error.get("code").asInt());
        assertEquals("Invalid params", error.get("message").asText());
    }
}
