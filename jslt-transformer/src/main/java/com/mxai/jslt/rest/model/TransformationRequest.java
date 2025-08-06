package com.mxai.jslt.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request model for JSLT transformation operations.
 * 
 * Contains the input JSON data, JSLT query, and optional formatting preferences.
 * 
 * @param jsonData the input JSON data as a string
 * @param jsltQuery the JSLT transformation query
 * @param prettyPrint whether to format the output JSON with indentation
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public record TransformationRequest(
    @JsonProperty("jsonData")
    @NotNull(message = "JSON data is required")
    @NotBlank(message = "JSON data cannot be blank")
    String jsonData,
    
    @JsonProperty("jsltQuery")
    @NotNull(message = "JSLT query is required")
    @NotBlank(message = "JSLT query cannot be blank")
    String jsltQuery,
    
    @JsonProperty("prettyPrint")
    Boolean prettyPrint
) {
    
    /**
     * Compact constructor for validation and default values.
     */
    public TransformationRequest {
        if (prettyPrint == null) {
            prettyPrint = false;
        }
    }
}
