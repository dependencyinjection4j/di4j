package org.di4j.di4j.exceptions;

public class CannotUseScopedServiceInRootScopeException extends DI4JException{
    public CannotUseScopedServiceInRootScopeException(String message) {
        super(message);
    }
}
