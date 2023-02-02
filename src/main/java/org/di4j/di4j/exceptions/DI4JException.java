package org.di4j.di4j.exceptions;

public class DI4JException extends RuntimeException {

    public DI4JException() {
    }

    public DI4JException(String message) {
        super(message);
    }

    public DI4JException(String message, Throwable cause) {
        super(message, cause);
    }

    public DI4JException(Throwable cause) {
        super(cause);
    }
}
