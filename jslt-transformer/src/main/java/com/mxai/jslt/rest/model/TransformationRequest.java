package com.mxai.jslt.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request model for JSLT transformation operations.
 * 
 * Contains the input JSON data, JSLT query, and formatting preferences.
 * 
 * @param jsonData the input JSON data as either a string or JSON object
 * @param jsltQuery the JSLT transformation query
 * @param prettyPrint whether to format the output JSON with indentation
 * @param returnAsString whether to return the result as a string (true) or JSON object (false)
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public record TransformationRequest(
    @JsonProperty("jsonData")
    @NotNull(message = "JSON data is required")
    JsonNode jsonData,
    
    @JsonProperty("jsltQuery")
    @NotNull(message = "JSLT query is required")
    @NotBlank(message = "JSLT query cannot be blank")
    String jsltQuery,
    
    @JsonProperty("prettyPrint")
    Boolean prettyPrint,
    
    @JsonProperty("returnAsString")
    Boolean returnAsString
) {
    
    /**
     * Compact constructor for validation and default values.
     */
    public TransformationRequest {
        if (prettyPrint == null) {
            prettyPrint = false;
        }
        if (returnAsString == null) {
            returnAsString = false;
        }
    }
}
