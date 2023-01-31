package org.di4j.di4j.exceptions;

public class InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException extends DI4JException{
    public InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException(String message) {
        super(message);
    }
}
