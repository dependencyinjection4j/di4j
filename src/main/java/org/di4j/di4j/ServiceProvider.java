package org.di4j.di4j;

import org.di4j.di4j.exceptions.CannotUseScopedServiceInRootScopeException;
import org.di4j.di4j.exceptions.ClassNotAssignableException;
import org.di4j.di4j.exceptions.FailedToInstantiateServiceException;
import org.di4j.di4j.exceptions.InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException;
import org.di4j.di4j.exceptions.InvalidServiceRegistrationException;
import org.di4j.di4j.exceptions.MissingServiceException;
import org.di4j.di4j.exceptions.ServiceNotFoundException;
import org.di4j.di4j.scope.ServiceScope;

/**
 * The service provider class represents a service provider for a service hierarchy.<br>
 * It contains a map of singleton services and provides methods to retrieve a service instance from the root scope.<br>
 * The service provider is responsible for creating service instances and injecting dependencies into them.<br>
 */
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
     * Retrieves a service instance of the given type from the root scope using the given context.
     *
     * @param type the type of the service to retrieve
     * @param context the context to use to retrieve the service instance
     * @param <T> the type of the service to retrieve
     * @return the service instance of the given type, or null if the service is not registered
     * @throws FailedToInstantiateServiceException if the service instance could not be instantiated
     * @throws InvalidServiceRegistrationException if the service registration is invalid
     * @throws CannotUseScopedServiceInRootScopeException if the service is a scoped service and cannot be used in the root scope
     * @throws MissingServiceException if a service instance cannot be retrieved, this is thrown when fetching child services
     * @throws ClassNotAssignableException if the created instance for a service cannot be assigned to the expected class type
     * @throws InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException if an injection-only factory is used without an injection target
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
