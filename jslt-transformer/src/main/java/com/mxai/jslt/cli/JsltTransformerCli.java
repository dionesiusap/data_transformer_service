package com.mxai.jslt.cli;

import com.mxai.jslt.core.JsltTransformationService;
import com.mxai.jslt.exception.FileProcessingException;
import com.mxai.jslt.exception.TransformationException;
import com.mxai.jslt.model.CliConfiguration;
import com.mxai.jslt.model.TransformationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Command-line interface for JSLT transformation operations.
 * 
 * This CLI application provides a user-friendly interface for:
 * - Loading JSON input and JSLT query files
 * - Executing transformations with performance monitoring
 * - Outputting results to stdout or files
 * - Providing verbose logging and debug information
 * 
 * Usage examples:
 * <pre>
 * # Basic transformation to stdout
 * java -jar jslt-transformer.jar --input data.json --query transform.jslt
 * 
 * # Transform with pretty-print to file
 * java -jar jslt-transformer.jar --input data.json --query transform.jslt --output result.json --pretty
 * 
 * # Verbose mode with debug information
 * java -jar jslt-transformer.jar --input data.json --query transform.jslt --verbose --debug
 * </pre>
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
@Command(
    name = "jslt-transform",
    description = "Transform JSON data using JSLT queries",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    showDefaultValues = true
)
public class JsltTransformerCli implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(JsltTransformerCli.class);

    // Exit codes following Unix conventions
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_USAGE_ERROR = 2;

    @Parameters(
        index = "0",
        description = "Path to the input JSON file",
        paramLabel = "INPUT_FILE"
    )
    private Path inputFile;

    @Parameters(
        index = "1", 
        description = "Path to the JSLT query file",
        paramLabel = "QUERY_FILE"
    )
    private Path queryFile;

    @Option(
        names = {"-o", "--output"},
        description = "Output file path (default: stdout)",
        paramLabel = "OUTPUT_FILE"
    )
    private Path outputFile;

    @Option(
        names = {"-p", "--pretty"},
        description = "Pretty-print the output JSON with indentation"
    )
    private boolean prettyPrint = false;

    @Option(
        names = {"-v", "--verbose"},
        description = "Enable verbose logging"
    )
    private boolean verbose = false;

    @Option(
        names = {"-d", "--debug"},
        description = "Enable debug mode with detailed error information"
    )
    private boolean debug = false;

    private final JsltTransformationService transformationService;

    /**
     * Default constructor for CLI framework instantiation.
     */
    public JsltTransformerCli() {
        this.transformationService = new JsltTransformationService();
    }

    /**
     * Constructor for dependency injection (useful for testing).
     *
     * @param transformationService the transformation service to use
     */
    public JsltTransformerCli(JsltTransformationService transformationService) {
        this.transformationService = transformationService;
    }

    /**
     * Main entry point for the CLI application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new JsltTransformerCli()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Executes the JSLT transformation command.
     *
     * @return exit code (0 for success, non-zero for failure)
     */
    @Override
    public Integer call() {
        try {
            // Configure logging level based on CLI options
            configureLogging();

            // Validate input parameters
            CliConfiguration config = validateAndCreateConfiguration();
            
            log.info("Starting JSLT transformation: input={}, query={}, output={}", 
                inputFile, queryFile, outputFile != null ? outputFile : "stdout");

            // Perform transformation
            TransformationResult result = transformationService.transformFromFiles(inputFile, queryFile);

            // Handle result
            return handleTransformationResult(result, config);

        } catch (FileProcessingException e) {
            handleError("File processing error", e, EXIT_USAGE_ERROR);
            return EXIT_USAGE_ERROR;
        } catch (TransformationException e) {
            handleError("Transformation error", e, EXIT_FAILURE);
            return EXIT_FAILURE;
        } catch (Exception e) {
            handleError("Unexpected error", e, EXIT_FAILURE);
            return EXIT_FAILURE;
        }
    }

    /**
     * Validates CLI parameters and creates configuration object.
     *
     * @return validated CliConfiguration
     * @throws FileProcessingException if validation fails
     */
    private CliConfiguration validateAndCreateConfiguration() throws FileProcessingException {
        if (inputFile == null) {
            throw new FileProcessingException("Input file must be specified");
        }
        if (queryFile == null) {
            throw new FileProcessingException("Query file must be specified");
        }

        return new CliConfiguration(inputFile, queryFile, outputFile, prettyPrint, verbose, debug);
    }

    /**
     * Handles the transformation result by outputting to stdout or file.
     *
     * @param result the transformation result
     * @param config the CLI configuration
     * @return exit code
     */
    private Integer handleTransformationResult(TransformationResult result, CliConfiguration config) {
        if (!result.success()) {
            System.err.println("Transformation failed: " + result.errorMessage());
            log.error("Transformation failed after {}ms: {}", result.processingTimeMs(), result.errorMessage());
            return EXIT_FAILURE;
        }

        try {
            // Output the result
            if (config.outputFile() != null) {
                // Write to file
                transformationService.writeJsonToFile(result.transformedJson(), config.outputFile(), config.prettyPrint());
                System.out.println("Transformation completed successfully. Output written to: " + config.outputFile());
            } else {
                // Write to stdout
                String jsonOutput = transformationService.formatJsonAsString(result.transformedJson(), config.prettyPrint());
                System.out.println(jsonOutput);
            }

            // Show performance information if verbose
            if (config.verbose()) {
                System.err.println("Processing completed in " + result.processingTimeMs() + "ms");
            }

            log.info("Transformation completed successfully in {}ms", result.processingTimeMs());
            return EXIT_SUCCESS;

        } catch (Exception e) {
            handleError("Output error", e, EXIT_FAILURE);
            return EXIT_FAILURE;
        }
    }

    /**
     * Handles errors by logging and printing user-friendly messages.
     *
     * @param context error context description
     * @param exception the exception that occurred
     * @param exitCode the exit code to suggest
     */
    private void handleError(String context, Exception exception, int exitCode) {
        String message = context + ": " + exception.getMessage();
        
        System.err.println(message);
        log.error(message, exception);

        if (debug) {
            System.err.println("\nDebug information:");
            exception.printStackTrace(System.err);
        }
    }

    /**
     * Configures logging level based on CLI options.
     * Note: This is a simplified approach. In production, you might use
     * logback configuration files or programmatic configuration.
     */
    private void configureLogging() {
        // For now, we rely on logback configuration
        // In a more sophisticated setup, we could programmatically adjust log levels
        if (verbose) {
            log.info("Verbose logging enabled");
        }
        if (debug) {
            log.info("Debug mode enabled");
        }
    }
}
