package org.di4j.di4j.exceptions;

/**
 * Thrown when a scoped service is used in the root scope.
 */
public class CannotUseScopedServiceInRootScopeException extends DI4JException {
    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public CannotUseScopedServiceInRootScopeException(String message) {
        super(message);
    }
}
