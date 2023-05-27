package org.di4j.di4j.exceptions;

/**
 * Thrown when a class is not assignable to another class.
 */
public class ClassNotAssignableException extends DI4JException {

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public ClassNotAssignableException(String message) {
        super(message);
    }
}
