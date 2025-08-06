package com.mxai.jslt.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for health check endpoint.
 * 
 * Provides service health status and timestamp information.
 * 
 * @param status health status (UP, DOWN, etc.)
 * @param timestamp current timestamp in ISO format
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public record HealthResponse(
    @JsonProperty("status") String status,
    @JsonProperty("timestamp") String timestamp
) {
    
    /**
     * Compact constructor for validation.
     */
    public HealthResponse {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (timestamp == null || timestamp.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }
    }
}
