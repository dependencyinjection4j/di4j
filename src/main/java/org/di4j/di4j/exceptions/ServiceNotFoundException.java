package org.di4j.di4j.exceptions;

/**
 * Thrown when a service is missing but was requested.
 */
public class ServiceNotFoundException extends DI4JException{

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public ServiceNotFoundException(String message) {
        super(message);
    }
}
