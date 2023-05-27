package org.di4j.di4j.exceptions;

/**
 * Thrown when a factory is used without an injection context.
 */
public class InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException extends DI4JException {

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException(String message) {
        super(message);
    }
}
