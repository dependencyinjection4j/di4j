package org.di4j.di4j;

import org.di4j.di4j.exceptions.ServiceNotFoundException;
import org.di4j.di4j.scope.ServiceScope;

public abstract class ServiceProvider {

    /**
     * Gets a service or returns null if the service was not found
     * @param type The type of the service to get an instance for
     * @return The instance for the service or null if the service was found or null if the service has not been registered
     * @param <T> The type of the service to lookup
     */
    public <T> T getService(Class<T> type) {
        return getService(type, null);
    }

    /**
     * Gets a service or returns null if the service was not found
     * @param type The type of the service to get an instance for
     * @return The instance for the service or null if the service was found or null if the service has not been registered
     * @param <T> The type of the service to lookup
     */
    public abstract <T> T getService(Class<T> type, Class<?> context);

    /**
     * Gets a new service scope that can be used to get scoped services
     * @return A new service scope
     */
    public abstract ServiceScope getScope();


    /**
     * Gets a service or throws an exception if the service was not found
     * @param type The type of the service to get an instance for
     * @return The instance for the service
     * @param <T> The type of the service to lookup
     * @throws ServiceNotFoundException If the service was not found this exception is thrown
     */
    public <T> T getRequiredService(Class<T> type) {
        T service = getService(type);
        if(service == null) throw new ServiceNotFoundException("The service of type" + type.getName() + " was not found");
        return service;
    }

}
