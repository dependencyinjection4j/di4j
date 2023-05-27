package org.di4j.di4j.scope;

import org.di4j.di4j.ServiceProvider;
import org.di4j.di4j.RootServiceProvider;
import org.di4j.di4j.exceptions.ClassNotAssignableException;
import org.di4j.di4j.exceptions.FailedToInstantiateServiceException;
import org.di4j.di4j.exceptions.InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException;
import org.di4j.di4j.exceptions.InvalidServiceRegistrationException;
import org.di4j.di4j.exceptions.MissingServiceException;
import org.di4j.di4j.registry.Service;
import org.di4j.di4j.registry.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * The `ServiceScope` class represents a scope for services that can be provided by a service provider.<br>
 * It contains a map of scoped services and provides methods to retrieve a service instance from the scope.<br>
 */
public class ServiceScope extends ServiceProvider {

    private Map<Class<?>, Object> scopedServices = new HashMap<>();

    RootServiceProvider rootScope;
    ServiceRegistry registry;

    
    /**
     * Creates a new service scope with the given root scope and service registry.
     *
     * @param rootScope the root scope of the new scope
     * @param registry the service registry to use for the new scope
     */
    public ServiceScope(RootServiceProvider rootScope, ServiceRegistry registry) {
        this.rootScope = rootScope;
        this.registry = registry;
    }

    /**
     * Retrieves a service instance of the given type from the scope using the given context.
     *
     * @param type the type of the service to retrieve
     * @param context the context to use to retrieve the service instance
     * @param <T> the type of the service to retrieve
     * @return the service instance of the given type, or null if the service is not registered
     * @throws FailedToInstantiateServiceException if the service instance could not be instantiated
     * @throws InvalidServiceRegistrationException if the service registration is invalid
     * @throws MissingServiceException if a service instance cannot be retrieved, this is thrown when fetching child services
     * @throws ClassNotAssignableException if the created instance for a service cannot be assigned to the expected class type
     * @throws InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException if an injection-only factory is used without an injection target
     */
    @Override
    public <T> T getService(Class<T> type, Class<?> context) {
        // Check to see if the scope already has an instance for this
        if(scopedServices.containsKey(type)) {
            return type.cast(scopedServices.get(type));
        }

        // Get the service registration
        Service<?> service = registry.getRegistration(type);
        if(service == null) return null;
        // If the service is a singleton, get the instance from the singleton
        if(service.isSingleton()) return rootScope.getService(type, context);

        // If the service is a transient service, create a new instance
        if(service.isTransient()) return registry.getService(type, this, context);

        // If the service is an injection only service, create a new instance
        if(service.isInjectionOnly()) return registry.getService(type, this, context);

        // If the service is a scoped service, create a scoped instance and save it.
        if(service.isScoped()) {
            Object instance = registry.getService(type, this, context);
            if(instance == null) {
                return null;
            }
            scopedServices.put(type, instance);
            return type.cast(instance);
        }

        // If the service is a injection only service

        throw new InvalidServiceRegistrationException("The service " + type.getName() + " did not have a valid registration, must be either a singleton, transient, scoped or injection only service.");
    }

    public ServiceScope getScope() {
        return rootScope.getScope(); // Create a new root scope
    }
}
