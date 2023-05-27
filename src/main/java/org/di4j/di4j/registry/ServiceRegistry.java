package org.di4j.di4j.registry;

import org.di4j.di4j.ServiceProvider;
import org.di4j.di4j.exceptions.ClassNotAssignableException;
import org.di4j.di4j.exceptions.FailedToInstantiateServiceException;
import org.di4j.di4j.exceptions.InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException;
import org.di4j.di4j.exceptions.MissingServiceException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * The `ServiceRegistry` class represents a registry of services that can be provided by a service provider.<br>
 * It contains a map of services and provides methods to retrieve a service instance from the registry.<br>
 */
public class ServiceRegistry {


    private Map<Class<?>, Service<?>> services = new HashMap<>();

    /**
     * Creates a new service registry with the given map of services.
     *
     * @param services the map of services to register
     */
    public ServiceRegistry(Map<Class<?>, Service<?>> services) {
        this.services = services;
    }

    /**
     * Retrieves a service instance of the given type from the registry using the given service provider.
     *
     * @param type the type of the service to retrieve
     * @param serviceProvider the service provider to use to retrieve the service instance
     * @param context the context to use to retrieve the service instance
     * @param <T> the type of the service to retrieve
     * @return the service instance of the given type, or null if the service is not registered
     * @throws FailedToInstantiateServiceException if the service instance could not be instantiated
     * @throws MissingServiceException if a service instance cannot be retrieved, this is thrown when fetching child services
     * @throws ClassNotAssignableException if the created instance for a service cannot be assigned to the expected class type
     * @throws InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException if an injection-only factory is used without an injection target
     */
    public <T> T getService(Class<T> type, ServiceProvider serviceProvider) {
        return getService(type, serviceProvider, null);
    }

    /**
     * Retrieves a service instance of the given type from the registry using the given service provider.
     *
     * @param type the type of the service to retrieve
     * @param serviceProvider the service provider to use to retrieve the service instance
     * @param <T> the type of the service to retrieve
     * @return the service instance of the given type, or null if the service is not registered
     * @throws FailedToInstantiateServiceException if the service instance could not be instantiated
     * @throws MissingServiceException if a service instance cannot be retrieved, this is thrown when fetching child services
     * @throws ClassNotAssignableException if the created instance for a service cannot be assigned to the expected class type
     * @throws InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException if an injection-only factory is used without an injection target
     */
    public <T> T getService(Class<T> type, ServiceProvider serviceProvider, Class<?> context) {
        Service<T> service = getRegistration(type);
        if(service == null) return null;
        try {
            var obj = service.getInstance(serviceProvider, context);
            if(obj != null && type.isAssignableFrom(obj.getClass())) {
                return type.cast(obj);
            }
            String typeName = obj != null ? obj.getClass().toString() : "<NULL>";
            throw new FailedToInstantiateServiceException("Could not get a service instance for the service " + type.getName() + ". The type " + typeName + " is not assignable to " + type.getName() + ".");
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new FailedToInstantiateServiceException("The service " + type.getName() + " could not be instantiated", e);
        }
    }

    /**
     * Retrieves the registration for the service of the given type.
     *
     * @param type the type of the service to retrieve the registration for
     * @param <T> the type of the service to retrieve the registration for
     * @return the registration for the service of the given type, or null if the service is not registered
     */
    public <T> Service<T> getRegistration(Class<T> type) {
        return (Service<T>) services.get(type);
    }



}