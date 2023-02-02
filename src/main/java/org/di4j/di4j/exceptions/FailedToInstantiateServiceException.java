package org.di4j.di4j.exceptions;

public class FailedToInstantiateServiceException extends DI4JException{
    public FailedToInstantiateServiceException(String message) {
        super(message);
    }

    public FailedToInstantiateServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
