package com.mxai.jslt.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxai.jslt.exception.FileProcessingException;

import com.mxai.jslt.model.TransformationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsltTransformationService.
 * 
 * Tests cover:
 * - Basic JSON transformation scenarios
 * - File I/O operations
 * - Error handling and validation
 * - Performance measurement
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
class JsltTransformationServiceTest {

    private JsltTransformationService transformationService;
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        transformationService = new JsltTransformationService(objectMapper);
    }

    @Test
    @DisplayName("Should successfully transform simple JSON with basic JSLT query")
    void shouldTransformSimpleJson() throws Exception {
        // Given
        String inputJson = "{\"name\": \"John Doe\", \"age\": 30, \"city\": \"New York\"}";
        String jsltQuery = "{\"fullName\": .name, \"years\": .age, \"location\": .city}";

        JsonNode inputNode = objectMapper.readTree(inputJson);

        // When
        TransformationResult result = transformationService.transformJson(inputNode, jsltQuery);

        // Then
        assertTrue(result.success());
        assertNotNull(result.transformedJson());
        assertTrue(result.processingTimeMs() >= 0);
        assertNull(result.errorMessage());

        JsonNode transformed = result.transformedJson();
        assertEquals("John Doe", transformed.get("fullName").asText());
        assertEquals(30, transformed.get("years").asInt());
        assertEquals("New York", transformed.get("location").asText());
    }

    @Test
    @DisplayName("Should handle array transformation with JSLT")
    void shouldTransformJsonArray() throws Exception {
        // Given
        String inputJson = "{\"users\": [{\"name\": \"Alice\", \"age\": 25}, {\"name\": \"Bob\", \"age\": 30}]}";
        String jsltQuery = "{\"userNames\": [for (.users) .name], \"totalUsers\": size(.users)}";

        JsonNode inputNode = objectMapper.readTree(inputJson);

        // When
        TransformationResult result = transformationService.transformJson(inputNode, jsltQuery);

        // Then
        assertTrue(result.success());
        JsonNode transformed = result.transformedJson();
        assertEquals(2, transformed.get("totalUsers").asInt());
        assertTrue(transformed.get("userNames").isArray());
        assertEquals("Alice", transformed.get("userNames").get(0).asText());
        assertEquals("Bob", transformed.get("userNames").get(1).asText());
    }

    @Test
    @DisplayName("Should return failure result for invalid JSLT query")
    void shouldHandleInvalidJsltQuery() throws Exception {
        // Given
        String inputJson = "{\"name\": \"test\"}";
        String invalidJsltQuery = "invalid jslt syntax {{{";

        JsonNode inputNode = objectMapper.readTree(inputJson);

        // When
        TransformationResult result = transformationService.transformJson(inputNode, invalidJsltQuery);

        // Then
        assertFalse(result.success());
        assertNull(result.transformedJson());
        assertNotNull(result.errorMessage());
        assertTrue(result.processingTimeMs() >= 0);
    }

    @Test
    @DisplayName("Should successfully transform from files")
    void shouldTransformFromFiles() throws Exception {
        // Given
        String inputJson = "{\"message\": \"Hello World\"}";
        String jsltQuery = "{\"greeting\": .message}";

        Path inputFile = tempDir.resolve("input.json");
        Path queryFile = tempDir.resolve("query.jslt");

        Files.writeString(inputFile, inputJson);
        Files.writeString(queryFile, jsltQuery);

        // When
        TransformationResult result = transformationService.transformFromFiles(inputFile, queryFile);

        // Then
        assertTrue(result.success());
        assertEquals("Hello World", result.transformedJson().get("greeting").asText());
    }

    @Test
    @DisplayName("Should throw FileProcessingException for non-existent input file")
    void shouldThrowExceptionForNonExistentFile() {
        // Given
        Path nonExistentFile = tempDir.resolve("does-not-exist.json");
        Path queryFile = tempDir.resolve("query.jslt");

        // When & Then
        assertThrows(FileProcessingException.class, () -> {
            transformationService.transformFromFiles(nonExistentFile, queryFile);
        });
    }

    @Test
    @DisplayName("Should throw FileProcessingException for invalid JSON file")
    void shouldThrowExceptionForInvalidJson() throws Exception {
        // Given
        String invalidJson = "{ invalid json content";
        Path inputFile = tempDir.resolve("invalid.json");
        Path queryFile = tempDir.resolve("query.jslt");

        Files.writeString(inputFile, invalidJson);
        Files.writeString(queryFile, ".");

        // When & Then
        assertThrows(FileProcessingException.class, () -> {
            transformationService.transformFromFiles(inputFile, queryFile);
        });
    }

    @Test
    @DisplayName("Should write JSON to file with pretty printing")
    void shouldWriteJsonToFileWithPrettyPrint() throws Exception {
        // Given
        String inputJson = "{\"name\": \"test\", \"value\": 123}";
        JsonNode jsonNode = objectMapper.readTree(inputJson);
        Path outputFile = tempDir.resolve("output.json");

        // When
        transformationService.writeJsonToFile(jsonNode, outputFile, true);

        // Then
        assertTrue(Files.exists(outputFile));
        String writtenContent = Files.readString(outputFile);
        assertTrue(writtenContent.contains("\n")); // Pretty-printed should have newlines
        assertTrue(writtenContent.contains("  ")); // Pretty-printed should have indentation
    }

    @Test
    @DisplayName("Should format JSON as string with pretty printing")
    void shouldFormatJsonAsStringWithPrettyPrint() throws Exception {
        // Given
        String inputJson = "{\"name\": \"test\", \"value\": 123}";
        JsonNode jsonNode = objectMapper.readTree(inputJson);

        // When
        String formatted = transformationService.formatJsonAsString(jsonNode, true);

        // Then
        assertNotNull(formatted);
        assertTrue(formatted.contains("\n")); // Pretty-printed should have newlines
        assertTrue(formatted.contains("  ")); // Pretty-printed should have indentation
    }

    @Test
    @DisplayName("Should format JSON as string without pretty printing")
    void shouldFormatJsonAsStringWithoutPrettyPrint() throws Exception {
        // Given
        String inputJson = "{\"name\": \"test\", \"value\": 123}";
        JsonNode jsonNode = objectMapper.readTree(inputJson);

        // When
        String formatted = transformationService.formatJsonAsString(jsonNode, false);

        // Then
        assertNotNull(formatted);
        assertFalse(formatted.contains("\n")); // Compact format should not have newlines
        assertTrue(formatted.contains("\"name\":\"test\""));
    }
}
