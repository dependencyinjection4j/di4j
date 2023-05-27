package org.di4j.di4j.exceptions;

/**
 * Base class for all DI4J exceptions.
 */
public class DI4JException extends RuntimeException {

    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    public DI4JException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public DI4JException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public DI4JException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     * @param cause the cause.
     */
    public DI4JException(Throwable cause) {
        super(cause);
    }
}
