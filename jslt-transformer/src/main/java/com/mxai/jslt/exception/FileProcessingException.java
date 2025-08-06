package com.mxai.jslt.exception;

/**
 * Exception thrown when file I/O operations fail during JSLT transformation.
 * 
 * This exception is thrown when:
 * - Input JSON file cannot be read
 * - JSLT query file cannot be read
 * - Output file cannot be written
 * - File permissions are insufficient
 * - File format is invalid
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public class FileProcessingException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new FileProcessingException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public FileProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileProcessingException with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause the underlying cause of this exception
     */
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new FileProcessingException with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public FileProcessingException(Throwable cause) {
        super(cause);
    }
}
