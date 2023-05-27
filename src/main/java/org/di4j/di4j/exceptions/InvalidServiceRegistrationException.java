package org.di4j.di4j.exceptions;

/**
 * Thrown when a service registration had an invalid configuration.
 */
public class InvalidServiceRegistrationException extends DI4JException{

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public InvalidServiceRegistrationException(String message) {
        super(message);
    }
}
