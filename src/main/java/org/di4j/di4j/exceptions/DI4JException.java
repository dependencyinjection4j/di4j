package org.di4j.di4j.exceptions;

public class DI4JException extends RuntimeException {

    private final String message;

    public DI4JException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
