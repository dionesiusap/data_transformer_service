package com.mxai.jslt.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxai.jslt.core.JsltTransformationService;
import com.mxai.jslt.model.TransformationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JsltTransformerCli.
 * 
 * Tests cover:
 * - Command line argument parsing
 * - Successful transformation scenarios
 * - Error handling and exit codes
 * - Output formatting options
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class JsltTransformerCliTest {

    @Mock
    private JsltTransformationService mockTransformationService;

    private JsltTransformerCli cli;
    private CommandLine commandLine;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorStream;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        cli = new JsltTransformerCli(mockTransformationService);
        commandLine = new CommandLine(cli);
        
        // Capture stdout and stderr for testing
        outputStream = new ByteArrayOutputStream();
        errorStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @Test
    @DisplayName("Should execute successful transformation with basic arguments")
    void shouldExecuteSuccessfulTransformation() throws Exception {
        // Given
        Path inputFile = createTempFile("input.json", "{\"name\": \"test\"}");
        Path queryFile = createTempFile("query.jslt", ".name");
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultJson = mapper.readTree("{\"result\": \"test\"}");
        TransformationResult successResult = TransformationResult.success(resultJson, 100L);
        
        when(mockTransformationService.transformFromFiles(eq(inputFile), eq(queryFile)))
            .thenReturn(successResult);
        when(mockTransformationService.formatJsonAsString(eq(resultJson), eq(false)))
            .thenReturn("{\"result\":\"test\"}");

        // When
        int exitCode = commandLine.execute(inputFile.toString(), queryFile.toString());

        // Then
        assertEquals(0, exitCode);
        String output = outputStream.toString();
        assertTrue(output.contains("\"result\":\"test\""));
    }

    @Test
    @DisplayName("Should execute transformation with pretty print option")
    void shouldExecuteTransformationWithPrettyPrint() throws Exception {
        // Given
        Path inputFile = createTempFile("input.json", "{\"name\": \"test\"}");
        Path queryFile = createTempFile("query.jslt", ".name");
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultJson = mapper.readTree("{\"result\": \"test\"}");
        TransformationResult successResult = TransformationResult.success(resultJson, 100L);
        
        when(mockTransformationService.transformFromFiles(eq(inputFile), eq(queryFile)))
            .thenReturn(successResult);
        when(mockTransformationService.formatJsonAsString(eq(resultJson), eq(true)))
            .thenReturn("{\n  \"result\" : \"test\"\n}");

        // When
        int exitCode = commandLine.execute(
            inputFile.toString(), 
            queryFile.toString(), 
            "--pretty"
        );

        // Then
        assertEquals(0, exitCode);
        String output = outputStream.toString();
        assertTrue(output.contains("{\n  \"result\" : \"test\"\n}"));
    }

    @Test
    @DisplayName("Should write output to file when output option is specified")
    void shouldWriteOutputToFile() throws Exception {
        // Given
        Path inputFile = createTempFile("input.json", "{\"name\": \"test\"}");
        Path queryFile = createTempFile("query.jslt", ".name");
        Path outputFile = tempDir.resolve("output.json");
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultJson = mapper.readTree("{\"result\": \"test\"}");
        TransformationResult successResult = TransformationResult.success(resultJson, 100L);
        
        when(mockTransformationService.transformFromFiles(eq(inputFile), eq(queryFile)))
            .thenReturn(successResult);

        // When
        int exitCode = commandLine.execute(
            inputFile.toString(), 
            queryFile.toString(), 
            "--output", outputFile.toString()
        );

        // Then
        assertEquals(0, exitCode);
        String output = outputStream.toString();
        assertTrue(output.contains("Transformation completed successfully"));
        assertTrue(output.contains(outputFile.toString()));
    }

    @Test
    @DisplayName("Should return failure exit code for transformation error")
    void shouldReturnFailureExitCodeForTransformationError() throws Exception {
        // Given
        Path inputFile = createTempFile("input.json", "{\"name\": \"test\"}");
        Path queryFile = createTempFile("query.jslt", ".name");
        
        TransformationResult failureResult = TransformationResult.failure("JSLT compilation failed", 50L);
        
        when(mockTransformationService.transformFromFiles(eq(inputFile), eq(queryFile)))
            .thenReturn(failureResult);

        // When
        int exitCode = commandLine.execute(inputFile.toString(), queryFile.toString());

        // Then
        assertEquals(1, exitCode);
        String errorOutput = errorStream.toString();
        assertTrue(errorOutput.contains("Transformation failed: JSLT compilation failed"));
    }

    @Test
    @DisplayName("Should return usage error exit code for missing arguments")
    void shouldReturnUsageErrorForMissingArguments() {
        // When
        int exitCode = commandLine.execute(); // No arguments

        // Then
        assertEquals(2, exitCode);
    }

    @Test
    @DisplayName("Should show help when help option is specified")
    void shouldShowHelpWhenHelpOptionSpecified() {
        // When
        int exitCode = commandLine.execute("--help");

        // Then
        assertEquals(0, exitCode);
        String output = outputStream.toString();
        assertTrue(output.contains("Transform JSON data using JSLT queries"));
        assertTrue(output.contains("Usage:"));
    }

    @Test
    @DisplayName("Should show version when version option is specified")
    void shouldShowVersionWhenVersionOptionSpecified() {
        // When
        int exitCode = commandLine.execute("--version");

        // Then
        assertEquals(0, exitCode);
        String output = outputStream.toString();
        assertTrue(output.contains("1.0.0"));
    }

    @Test
    @DisplayName("Should handle verbose mode")
    void shouldHandleVerboseMode() throws Exception {
        // Given
        Path inputFile = createTempFile("input.json", "{\"name\": \"test\"}");
        Path queryFile = createTempFile("query.jslt", ".name");
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultJson = mapper.readTree("{\"result\": \"test\"}");
        TransformationResult successResult = TransformationResult.success(resultJson, 150L);
        
        when(mockTransformationService.transformFromFiles(eq(inputFile), eq(queryFile)))
            .thenReturn(successResult);
        when(mockTransformationService.formatJsonAsString(eq(resultJson), eq(false)))
            .thenReturn("{\"result\":\"test\"}");

        // When
        int exitCode = commandLine.execute(
            inputFile.toString(), 
            queryFile.toString(), 
            "--verbose"
        );

        // Then
        assertEquals(0, exitCode);
        String errorOutput = errorStream.toString();
        assertTrue(errorOutput.contains("Processing completed in 150ms"));
    }

    /**
     * Helper method to create temporary files for testing.
     *
     * @param filename the name of the file
     * @param content the content to write
     * @return Path to the created file
     * @throws Exception if file creation fails
     */
    private Path createTempFile(String filename, String content) throws Exception {
        Path file = tempDir.resolve(filename);
        Files.writeString(file, content);
        return file;
    }
}
