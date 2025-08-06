package com.mxai.jslt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the JSLT Transformation Service.
 * 
 * This application provides REST API endpoints for JSLT transformations,
 * building upon the existing CLI functionality.
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
@SpringBootApplication
public class JsltTransformerApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(JsltTransformerApplication.class, args);
    }
}
