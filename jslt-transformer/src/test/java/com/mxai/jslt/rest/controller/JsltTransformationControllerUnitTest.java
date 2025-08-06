package com.mxai.jslt.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxai.jslt.core.JsltTransformationService;
import com.mxai.jslt.model.TransformationResult;
import com.mxai.jslt.rest.model.TransformationRequest;
import com.mxai.jslt.rest.model.TransformationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Simple unit tests for JsltTransformationController.
 * 
 * Tests controller logic without Spring Boot context to avoid integration test complexity.
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
class JsltTransformationControllerUnitTest {

    @Mock
    private JsltTransformationService transformationService;

    @InjectMocks
    private JsltTransformationController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testTransformSuccess() throws Exception {
        // Arrange
        String inputJson = "{\"name\": \"John\", \"age\": 30}";
        String jsltQuery = "{\"fullName\": .name, \"years\": .age}";
        JsonNode jsonData = objectMapper.readTree(inputJson);
        TransformationRequest request = new TransformationRequest(jsonData, jsltQuery, false, false);
        
        JsonNode outputNode = objectMapper.readTree("{\"fullName\": \"John\", \"years\": 30}");
        TransformationResult mockResult = new TransformationResult(outputNode, true, 15L, null);
        
        when(transformationService.transformFromStrings(eq(inputJson), eq(jsltQuery)))
            .thenReturn(mockResult);
        when(transformationService.formatJsonAsString(eq(outputNode), eq(false)))
            .thenReturn("{\"fullName\":\"John\",\"years\":30}");

        // Act
        ResponseEntity<TransformationResponse> response = controller.transform(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals("{\"fullName\":\"John\",\"years\":30}", response.getBody().result());
        assertEquals(15L, response.getBody().processingTimeMs());
        assertNull(response.getBody().errorMessage());
    }

    @Test
    void testTransformWithPrettyPrint() throws Exception {
        // Arrange
        String inputJson = "{\"name\": \"John\", \"age\": 30}";
        String jsltQuery = "{\"fullName\": .name, \"years\": .age}";
        JsonNode jsonData = objectMapper.readTree(inputJson);
        TransformationRequest request = new TransformationRequest(jsonData, jsltQuery, true, false);
        
        JsonNode outputNode = objectMapper.readTree("{\"fullName\": \"John\", \"years\": 30}");
        TransformationResult mockResult = new TransformationResult(outputNode, true, 20L, null);
        
        when(transformationService.transformFromStrings(eq(inputJson), eq(jsltQuery)))
            .thenReturn(mockResult);
        when(transformationService.formatJsonAsString(eq(outputNode), eq(true)))
            .thenReturn("{\n  \"fullName\" : \"John\",\n  \"years\" : 30\n}");

        // Act
        ResponseEntity<TransformationResponse> response = controller.transform(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertEquals("{\n  \"fullName\" : \"John\",\n  \"years\" : 30\n}", response.getBody().result());
        assertEquals(20L, response.getBody().processingTimeMs());
        assertNull(response.getBody().errorMessage());
    }

    @Test
    void testTransformFailure() throws Exception {
        // Arrange
        String inputJson = "{\"name\": \"John\"}";
        String jsltQuery = "invalid query";
        JsonNode jsonData = objectMapper.readTree(inputJson);
        TransformationRequest request = new TransformationRequest(jsonData, jsltQuery, false, false);
        
        String errorMessage = "JSLT compilation failed";
        TransformationResult mockResult = new TransformationResult(null, false, 5L, errorMessage);
        
        when(transformationService.transformFromStrings(eq(inputJson), eq(jsltQuery)))
            .thenReturn(mockResult);

        // Act
        ResponseEntity<TransformationResponse> response = controller.transform(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertNull(response.getBody().result());
        assertNull(response.getBody().processingTimeMs());
        assertEquals(errorMessage, response.getBody().errorMessage());
    }

    @Test
    void testHealthEndpoint() {
        // Act
        ResponseEntity<?> response = controller.health();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testVersionEndpoint() {
        // Act
        ResponseEntity<?> response = controller.version();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
