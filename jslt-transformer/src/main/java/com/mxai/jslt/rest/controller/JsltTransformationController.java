package com.mxai.jslt.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxai.jslt.core.JsltTransformationService;
import com.mxai.jslt.model.TransformationResult;
import com.mxai.jslt.rest.model.HealthResponse;
import com.mxai.jslt.rest.model.TransformationRequest;
import com.mxai.jslt.rest.model.TransformationResponse;
import com.mxai.jslt.rest.model.VersionResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * REST controller for JSLT transformation operations.
 * 
 * Provides endpoints for JSON transformation using JSLT queries,
 * health checks, and version information.
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class JsltTransformationController {

    private static final Logger log = LoggerFactory.getLogger(JsltTransformationController.class);
    private static final String SERVICE_NAME = "JSLT Transformation Service";
    private static final String SERVICE_VERSION = "1.0.0";
    private static final int MAX_JSON_SIZE = 100 * 1024 * 1024; // 100MB limit

    private final JsltTransformationService transformationService;
    private final ObjectMapper objectMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param transformationService the JSLT transformation service
     * @param objectMapper the JSON object mapper
     */
    @Autowired
    public JsltTransformationController(JsltTransformationService transformationService, ObjectMapper objectMapper) {
        this.transformationService = transformationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Transform JSON data using a JSLT query.
     *
     * @param request the transformation request containing JSON data and JSLT query
     * @return transformation response with result or error information
     */
    @PostMapping("/transform")
    public ResponseEntity<TransformationResponse> transform(@Valid @RequestBody TransformationRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        log.info("Processing transformation request: {} with query length: {}", 
                requestId, request.jsltQuery().length());
        
        try {
            // Convert JsonNode to string for processing - handle both object and string inputs
            String jsonDataString;
            if (request.jsonData().isTextual()) {
                // Input is already a JSON string, use it directly
                jsonDataString = request.jsonData().asText();
            } else {
                // Input is a JSON object, serialize it to string
                jsonDataString = objectMapper.writeValueAsString(request.jsonData());
            }
            
            // Validate input JSON length for large file support
            if (jsonDataString.length() > MAX_JSON_SIZE) {
                String errorMsg = String.format("JSON data size (%d bytes) exceeds maximum allowed size (%d bytes)", 
                    jsonDataString.length(), MAX_JSON_SIZE);
                log.warn("Request {} rejected: {}", requestId, errorMsg);
                return ResponseEntity.ok(
                    TransformationResponse.error(errorMsg, requestId));
            }
            
            // Perform transformation
            TransformationResult result = transformationService.transformFromStrings(
                jsonDataString, request.jsltQuery());
            
            if (result.success()) {
                Object formattedResult;
                if (request.returnAsString()) {
                    // Return as JSON string
                    formattedResult = transformationService.formatJsonAsString(
                        result.transformedJson(), request.prettyPrint());
                } else {
                    // Return as JSON object
                    formattedResult = result.transformedJson();
                }
                
                log.info("Transformation successful for request: {} in {}ms", 
                        requestId, result.processingTimeMs());
                
                return ResponseEntity.ok(
                    TransformationResponse.success(formattedResult, result.processingTimeMs(), requestId));
            } else {
                log.warn("Transformation failed for request: {} - {}", requestId, result.errorMessage());
                return ResponseEntity.ok(
                    TransformationResponse.error(result.errorMessage(), requestId));
            }
            
        } catch (Exception e) {
            String errorMsg = "Unexpected error during transformation: " + e.getMessage();
            log.error("Unexpected error for request: {}", requestId, e);
            return ResponseEntity.ok(
                TransformationResponse.error(errorMsg, requestId));
        }
    }

    /**
     * Health check endpoint.
     *
     * @return health status response
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        log.debug("Health check requested");
        
        HealthResponse response = new HealthResponse(
            "UP", 
            Instant.now().toString()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Version information endpoint.
     *
     * @return version information response
     */
    @GetMapping("/version")
    public ResponseEntity<VersionResponse> version() {
        log.debug("Version information requested");
        
        VersionResponse response = new VersionResponse(
            SERVICE_VERSION, 
            SERVICE_NAME
        );
        
        return ResponseEntity.ok(response);
    }
}
