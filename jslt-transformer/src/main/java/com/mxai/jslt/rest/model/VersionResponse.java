package com.mxai.jslt.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for version information endpoint.
 * 
 * Provides service version and name information.
 * 
 * @param version service version string
 * @param serviceName name of the service
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public record VersionResponse(
    @JsonProperty("version") String version,
    @JsonProperty("serviceName") String serviceName
) {
    
    /**
     * Compact constructor for validation.
     */
    public VersionResponse {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be null or empty");
        }
    }
}
