package org.di4j.di4j.exceptions;

/**
 * Thrown when a class does not have exactly one constructor or has multiple constructors with the {@link org.di4j.di4j.annotations.ServiceProviderConstructor} annotation.
 */
public class InvalidConstructorCountException extends DI4JException{

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public InvalidConstructorCountException(String message) {
        super(message);
    }

}
