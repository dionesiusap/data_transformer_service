package com.mxai.jslt.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxai.jslt.exception.FileProcessingException;
import com.mxai.jslt.exception.TransformationException;
import com.mxai.jslt.model.TransformationResult;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Core service for JSLT transformation operations.
 * 
 * This service provides the main business logic for:
 * - Loading and parsing JSON input files
 * - Compiling JSLT query expressions
 * - Executing transformations with performance monitoring
 * - Handling errors and providing detailed diagnostics
 * 
 * The service is stateless and thread-safe, supporting concurrent operations.
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public class JsltTransformationService {

    private static final Logger log = LoggerFactory.getLogger(JsltTransformationService.class);
    
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new JsltTransformationService with default JSON processing configuration.
     */
    public JsltTransformationService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a new JsltTransformationService with custom ObjectMapper configuration.
     *
     * @param objectMapper the Jackson ObjectMapper to use for JSON processing
     */
    public JsltTransformationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Transforms JSON data using a JSLT query from file paths.
     *
     * @param inputFilePath path to the input JSON file
     * @param queryFilePath path to the JSLT query file
     * @return TransformationResult containing the result or error information
     * @throws FileProcessingException if file I/O operations fail
     * @throws TransformationException if JSLT processing fails
     */
    public TransformationResult transformFromFiles(Path inputFilePath, Path queryFilePath) 
            throws FileProcessingException, TransformationException {
        
        log.info("Starting transformation from files: input={}, query={}", inputFilePath, queryFilePath);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Load and parse input JSON
            JsonNode inputJson = loadJsonFromFile(inputFilePath);
            log.debug("Successfully loaded input JSON with {} nodes", 
                inputJson.isObject() ? inputJson.size() : "unknown");
            
            // Load JSLT query
            String jsltQuery = loadTextFromFile(queryFilePath);
            log.debug("Successfully loaded JSLT query: {} characters", jsltQuery.length());
            
            // Perform transformation
            return transformJson(inputJson, jsltQuery, startTime);
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Transformation failed after {}ms", processingTime, e);
            return TransformationResult.failure(e.getMessage(), processingTime);
        }
    }

    /**
     * Transforms JSON data using a JSLT query string.
     *
     * @param inputJson the input JSON data to transform
     * @param jsltQuery the JSLT query string
     * @return TransformationResult containing the result or error information
     * @throws TransformationException if JSLT processing fails
     */
    public TransformationResult transformJson(JsonNode inputJson, String jsltQuery) 
            throws TransformationException {
        
        return transformJson(inputJson, jsltQuery, System.currentTimeMillis());
    }

    /**
     * Internal method to perform JSON transformation with timing.
     *
     * @param inputJson the input JSON data
     * @param jsltQuery the JSLT query string
     * @param startTime the start time for performance measurement
     * @return TransformationResult containing the result or error information
     * @throws TransformationException if JSLT processing fails
     */
    private TransformationResult transformJson(JsonNode inputJson, String jsltQuery, long startTime) 
            throws TransformationException {
        
        try {
            // Compile JSLT expression
            log.debug("Compiling JSLT expression");
            Expression jsltExpression = Parser.compileString(jsltQuery);
            
            // Execute transformation
            log.debug("Executing JSLT transformation");
            JsonNode result = jsltExpression.apply(inputJson);
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Transformation completed successfully in {}ms", processingTime);
            
            return TransformationResult.success(result, processingTime);
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("JSLT transformation failed after {}ms", processingTime, e);
            throw new TransformationException("JSLT transformation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads and parses JSON content from a file.
     *
     * @param filePath path to the JSON file
     * @return parsed JsonNode
     * @throws FileProcessingException if file cannot be read or parsed
     */
    private JsonNode loadJsonFromFile(Path filePath) throws FileProcessingException {
        validateFileExists(filePath);
        
        try {
            log.debug("Loading JSON from file: {}", filePath);
            return objectMapper.readTree(filePath.toFile());
            
        } catch (IOException e) {
            log.error("Failed to load JSON from file: {}", filePath, e);
            throw new FileProcessingException("Cannot read JSON file: " + filePath, e);
        }
    }

    /**
     * Loads text content from a file.
     *
     * @param filePath path to the text file
     * @return file content as string
     * @throws FileProcessingException if file cannot be read
     */
    private String loadTextFromFile(Path filePath) throws FileProcessingException {
        validateFileExists(filePath);
        
        try {
            log.debug("Loading text from file: {}", filePath);
            return Files.readString(filePath);
            
        } catch (IOException e) {
            log.error("Failed to load text from file: {}", filePath, e);
            throw new FileProcessingException("Cannot read file: " + filePath, e);
        }
    }

    /**
     * Validates that a file exists and is readable.
     *
     * @param filePath path to validate
     * @throws FileProcessingException if file doesn't exist or isn't readable
     */
    private void validateFileExists(Path filePath) throws FileProcessingException {
        if (filePath == null) {
            throw new FileProcessingException("File path cannot be null");
        }
        
        if (!Files.exists(filePath)) {
            throw new FileProcessingException("File does not exist: " + filePath);
        }
        
        if (!Files.isReadable(filePath)) {
            throw new FileProcessingException("File is not readable: " + filePath);
        }
        
        if (Files.isDirectory(filePath)) {
            throw new FileProcessingException("Path is a directory, not a file: " + filePath);
        }
    }

    /**
     * Writes JSON content to a file with optional pretty-printing.
     *
     * @param jsonNode the JSON content to write
     * @param outputPath the output file path
     * @param prettyPrint whether to format the JSON with indentation
     * @throws FileProcessingException if file cannot be written
     */
    public void writeJsonToFile(JsonNode jsonNode, Path outputPath, boolean prettyPrint) 
            throws FileProcessingException {
        
        try {
            log.debug("Writing JSON to file: {} (pretty={})", outputPath, prettyPrint);
            
            if (prettyPrint) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), jsonNode);
            } else {
                objectMapper.writeValue(outputPath.toFile(), jsonNode);
            }
            
            log.info("Successfully wrote JSON to file: {}", outputPath);
            
        } catch (IOException e) {
            log.error("Failed to write JSON to file: {}", outputPath, e);
            throw new FileProcessingException("Cannot write to file: " + outputPath, e);
        }
    }

    /**
     * Formats JSON node as string with optional pretty-printing.
     *
     * @param jsonNode the JSON node to format
     * @param prettyPrint whether to format with indentation
     * @return formatted JSON string
     * @throws TransformationException if JSON serialization fails
     */
    public String formatJsonAsString(JsonNode jsonNode, boolean prettyPrint) throws TransformationException {
        try {
            if (prettyPrint) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            } else {
                return objectMapper.writeValueAsString(jsonNode);
            }
        } catch (IOException e) {
            log.error("Failed to format JSON as string", e);
            throw new TransformationException("Cannot format JSON as string", e);
        }
    }
}
