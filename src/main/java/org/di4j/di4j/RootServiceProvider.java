package org.di4j.di4j;

import org.di4j.di4j.exceptions.CannotUseScopedServiceInRootScopeException;
import org.di4j.di4j.exceptions.ClassNotAssignableException;
import org.di4j.di4j.exceptions.FailedToInstantiateServiceException;
import org.di4j.di4j.exceptions.InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException;
import org.di4j.di4j.exceptions.InvalidServiceRegistrationException;
import org.di4j.di4j.exceptions.MissingServiceException;
import org.di4j.di4j.registry.Service;
import org.di4j.di4j.registry.ServiceRegistry;
import org.di4j.di4j.scope.ServiceScope;

import java.util.HashMap;
import java.util.Map;

/**
 * The `RootServiceProvider` class represents a service provider for the root scope of a service hierarchy.<br>
 * It contains a map of singleton services and provides methods to retrieve a service instance from the root scope.<br>
 */
public class RootServiceProvider extends ServiceProvider {

    private Map<Class<?>, Object> singletonServices = new HashMap<>();


    ServiceRegistry registry;

    /**
     * Creates a new root service provider with the given service registry.
     *
     * @param registry the service registry to use for the root service provider
     */
    protected RootServiceProvider(ServiceRegistry registry) {
        this.registry = registry;
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
    @Override
    public <T> T getService(Class<T> type, Class<?> context) {
        // Check to see if we already have an instance for this
        if(singletonServices.containsKey(type)) {
            return type.cast(singletonServices.get(type));
        }

        Service<T> service = registry.getRegistration(type);
        if(service == null) return null;

        if(service.isScoped()) throw new CannotUseScopedServiceInRootScopeException("The service " + type.getName() + " is a scoped service and cannot be used in the root scope");

        if(service.isTransient()) return registry.getService(type, this, context);

        // If the service is an injection only service, create a new instance
        if(service.isInjectionOnly()) return registry.getService(type, this, context);

        if(service.isSingleton()) {
            Object instance = registry.getService(type, this, context);
            if(instance == null) {
                return null;
            }
            singletonServices.put(type, instance);
            return type.cast(instance);
        }

        throw new InvalidServiceRegistrationException("The service " + type.getName() + " did not have a valid registration, must be either a singleton, transient, scoped or injection only service.");
    }

    /**
     * Creates a new child scope of the root service provider
     * 
     * @return the service scope of the root service provider
     */
    @Override
    public ServiceScope getScope() {
        return new ServiceScope(this, registry);
    }

}
