package com.mxai.jslt.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for JSLT transformation operations.
 * 
 * Contains the transformation result, success status, timing information,
 * and request tracking details.
 * 
 * @param success whether the transformation was successful
 * @param result the transformed JSON data (present on success)
 * @param errorMessage error description (present on failure)
 * @param processingTimeMs processing time in milliseconds
 * @param requestId unique identifier for request tracking
 * @param timestamp response timestamp in ISO format
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public record TransformationResponse(
    @JsonProperty("success") boolean success,
    @JsonProperty("result") String result,
    @JsonProperty("errorMessage") String errorMessage,
    @JsonProperty("processingTimeMs") Long processingTimeMs,
    @JsonProperty("requestId") String requestId,
    @JsonProperty("timestamp") String timestamp
) {
    
    /**
     * Create a successful transformation response.
     *
     * @param result the transformed JSON data
     * @param processingTimeMs processing time in milliseconds
     * @param requestId unique request identifier
     * @return successful transformation response
     */
    public static TransformationResponse success(String result, Long processingTimeMs, String requestId) {
        return new TransformationResponse(
            true,
            result,
            null,
            processingTimeMs,
            requestId,
            java.time.Instant.now().toString()
        );
    }
    
    /**
     * Create an error transformation response.
     *
     * @param errorMessage error description
     * @param requestId unique request identifier
     * @return error transformation response
     */
    public static TransformationResponse error(String errorMessage, String requestId) {
        return new TransformationResponse(
            false,
            null,
            errorMessage,
            null,
            requestId,
            java.time.Instant.now().toString()
        );
    }
}
