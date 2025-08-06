package com.mxai.jslt.exception;

/**
 * Exception thrown when JSLT transformation operations fail.
 * 
 * This exception is thrown when:
 * - JSLT query compilation fails
 * - JSON parsing errors occur
 * - Transformation execution encounters runtime errors
 * - Input validation fails
 * 
 * @author MXAI Development Team
 * @since 1.0.0
 */
public class TransformationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new TransformationException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public TransformationException(String message) {
        super(message);
    }

    /**
     * Constructs a new TransformationException with the specified detail message and cause.
     *
     * @param message the detail message explaining the cause of the exception
     * @param cause the underlying cause of this exception
     */
    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new TransformationException with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public TransformationException(Throwable cause) {
        super(cause);
    }
}
