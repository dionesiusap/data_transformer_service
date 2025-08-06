package com.mxai.jslt.model;

import java.nio.file.Path;

/**
 * Record representing CLI configuration parameters for JSLT transformation.
 * 
 * This immutable data transfer object contains all CLI arguments:
 * - Input JSON file path
 * - JSLT query file path
 * - Output file path (optional)
 * - Formatting and verbosity options
 * 
 * @param inputFile path to the input JSON file
 * @param queryFile path to the JSLT query file
 * @param outputFile optional path to output file (null for stdout)
 * @param prettyPrint whether to format output JSON with indentation
 * @param verbose whether to enable verbose logging
 * @param debug whether to enable debug mode with detailed error information
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public record CliConfiguration(
    Path inputFile,
    Path queryFile,
    Path outputFile,
    boolean prettyPrint,
    boolean verbose,
    boolean debug
) {
    
    /**
     * Compact constructor for validation.
     * Ensures required files are specified and exist.
     */
    public CliConfiguration {
        if (inputFile == null) {
            throw new IllegalArgumentException("Input file path cannot be null");
        }
        if (queryFile == null) {
            throw new IllegalArgumentException("Query file path cannot be null");
        }
    }
    
    /**
     * Creates a basic CLI configuration with required parameters only.
     *
     * @param inputFile path to the input JSON file
     * @param queryFile path to the JSLT query file
     * @return a CliConfiguration with default options
     */
    public static CliConfiguration basic(Path inputFile, Path queryFile) {
        return new CliConfiguration(inputFile, queryFile, null, false, false, false);
    }
    
    /**
     * Creates a CLI configuration with pretty-print enabled.
     *
     * @param inputFile path to the input JSON file
     * @param queryFile path to the JSLT query file
     * @param outputFile optional output file path
     * @return a CliConfiguration with pretty-print enabled
     */
    public static CliConfiguration withPrettyPrint(Path inputFile, Path queryFile, Path outputFile) {
        return new CliConfiguration(inputFile, queryFile, outputFile, true, false, false);
    }
}
