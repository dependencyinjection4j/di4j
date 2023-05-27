package org.di4j.di4j.exceptions;

/**
 * Thrown when a service cannot be instantiated for any reason.
 */
public class FailedToInstantiateServiceException extends DI4JException {

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public FailedToInstantiateServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public FailedToInstantiateServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
