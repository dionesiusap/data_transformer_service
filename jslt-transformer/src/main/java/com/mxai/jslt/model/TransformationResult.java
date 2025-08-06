package com.mxai.jslt.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Record representing the result of a JSLT transformation operation.
 * 
 * This immutable data transfer object contains:
 * - The transformed JSON result
 * - Success/failure status
 * - Processing time metrics
 * - Optional error information
 * 
 * @param transformedJson the result of the JSLT transformation, null if transformation failed
 * @param success whether the transformation completed successfully
 * @param processingTimeMs the time taken to complete the transformation in milliseconds
 * @param errorMessage optional error message if transformation failed
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public record TransformationResult(
    JsonNode transformedJson,
    boolean success,
    long processingTimeMs,
    String errorMessage
) {
    
    /**
     * Compact constructor for validation.
     * Ensures processing time is non-negative and validates success/error consistency.
     */
    public TransformationResult {
        if (processingTimeMs < 0) {
            throw new IllegalArgumentException("Processing time cannot be negative");
        }
        if (!success && errorMessage == null) {
            throw new IllegalArgumentException("Error message must be provided when success is false");
        }
        if (success && transformedJson == null) {
            throw new IllegalArgumentException("Transformed JSON must be provided when success is true");
        }
    }
    
    /**
     * Creates a successful transformation result.
     *
     * @param transformedJson the successfully transformed JSON
     * @param processingTimeMs the processing time in milliseconds
     * @return a successful TransformationResult
     */
    public static TransformationResult success(JsonNode transformedJson, long processingTimeMs) {
        return new TransformationResult(transformedJson, true, processingTimeMs, null);
    }
    
    /**
     * Creates a failed transformation result.
     *
     * @param errorMessage the error message describing the failure
     * @param processingTimeMs the processing time before failure in milliseconds
     * @return a failed TransformationResult
     */
    public static TransformationResult failure(String errorMessage, long processingTimeMs) {
        return new TransformationResult(null, false, processingTimeMs, errorMessage);
    }
}
