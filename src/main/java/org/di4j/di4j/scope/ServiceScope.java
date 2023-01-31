package org.di4j.di4j.scope;

import org.di4j.di4j.ServiceProvider;
import org.di4j.di4j.RootServiceProvider;
import org.di4j.di4j.exceptions.InvalidServiceRegistrationException;
import org.di4j.di4j.registry.Service;
import org.di4j.di4j.registry.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

public class ServiceScope extends ServiceProvider {

    private Map<Class<?>, Object> scopedServices = new HashMap<>();

    RootServiceProvider rootScope;
    ServiceRegistry registry;

    public ServiceScope(RootServiceProvider rootScope, ServiceRegistry registry) {
        this.rootScope = rootScope;
        this.registry = registry;
    }

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
