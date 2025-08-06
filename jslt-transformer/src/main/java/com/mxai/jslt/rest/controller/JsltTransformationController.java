package com.mxai.jslt.rest.controller;

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
import org.springframework.http.HttpStatus;
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

    private static final Logger logger = LoggerFactory.getLogger(JsltTransformationController.class);
    private static final String SERVICE_NAME = "JSLT Transformation Service";
    private static final String SERVICE_VERSION = "1.0.0";

    private final JsltTransformationService transformationService;

    /**
     * Constructor with dependency injection.
     *
     * @param transformationService the JSLT transformation service
     */
    @Autowired
    public JsltTransformationController(JsltTransformationService transformationService) {
        this.transformationService = transformationService;
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
        
        logger.info("Processing transformation request: requestId={}, jsonDataLength={}, queryLength={}, prettyPrint={}", 
            requestId, request.jsonData().length(), request.jsltQuery().length(), request.prettyPrint());

        try {
            // Perform transformation using core service
            TransformationResult result = transformationService.transformFromStrings(
                request.jsonData(), 
                request.jsltQuery()
            );

            if (result.success()) {
                // Format output based on prettyPrint preference
                String formattedResult = request.prettyPrint() 
                    ? transformationService.formatJsonAsString(result.transformedJson(), true)
                    : result.transformedJson().toString();

                TransformationResponse response = TransformationResponse.success(
                    formattedResult, 
                    result.processingTimeMs(), 
                    requestId
                );

                logger.info("Transformation completed successfully: requestId={}, processingTime={}ms", 
                    requestId, result.processingTimeMs());

                return ResponseEntity.ok(response);

            } else {
                TransformationResponse response = TransformationResponse.error(
                    result.errorMessage(), 
                    requestId
                );

                logger.warn("Transformation failed: requestId={}, error={}", requestId, result.errorMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            String errorMessage = "Unexpected error during transformation: " + e.getMessage();
            logger.error("Unexpected error in transformation: requestId={}", requestId, e);

            TransformationResponse response = TransformationResponse.error(errorMessage, requestId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint.
     *
     * @return health status response
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        logger.debug("Health check requested");
        
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
        logger.debug("Version information requested");
        
        VersionResponse response = new VersionResponse(
            SERVICE_VERSION, 
            SERVICE_NAME
        );
        
        return ResponseEntity.ok(response);
    }
}
