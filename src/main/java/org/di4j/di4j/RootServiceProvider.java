package org.di4j.di4j;

import org.di4j.di4j.exceptions.CannotUseScopedServiceInRootScopeException;
import org.di4j.di4j.exceptions.InvalidServiceRegistrationException;
import org.di4j.di4j.registry.Service;
import org.di4j.di4j.registry.ServiceRegistry;
import org.di4j.di4j.scope.ServiceScope;

import java.util.HashMap;
import java.util.Map;

public class RootServiceProvider extends ServiceProvider {

    private Map<Class<?>, Object> singletonServices = new HashMap<>();


    ServiceRegistry registry;

    protected RootServiceProvider(ServiceRegistry registry) {
        this.registry = registry;
    }

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

    @Override
    public ServiceScope getScope() {
        return new ServiceScope(this, registry);
    }

}
