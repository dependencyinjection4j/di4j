package org.di4j.di4j;

import org.di4j.di4j.registry.Service;
import org.di4j.di4j.registry.ServiceRegistry;
import org.di4j.di4j.scope.ServiceScope;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * The `ServiceCollectionBuilder` class is used to build a collection of services for a service provider.<br>
 * It provides methods to add scoped, singleton, transient, and injection-only services to the collection.<br>
 */
public class ServiceCollectionBuilder {

    private final Map<Class<?>, IService> services = new HashMap<>();

    //#region Scoped

    /**
     * Adds a scoped service of the given type to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addScoped(Class<T> serviceClass) {
        services.put(serviceClass, new ScopedService(serviceClass, null, null));
        return this;
    }

    /**
     * Adds a scoped service of the given type and implementation to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param implementationClass the implementation of the service to add
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addScoped(Class<T> serviceClass, Class<? extends T> implementationClass) {
        services.put(serviceClass, new ScopedService(serviceClass, implementationClass, null));
        return this;
    }

    /**
     * Adds a scoped service of the given type and factory to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param factory the factory to use to create the service instance
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addScoped(Class<T> serviceClass, Function<ServiceProvider, Object> factory) {
        services.put(serviceClass, new ScopedService(serviceClass, null, factory));
        return this;
    }

    //#endregion

    //#region Singleton

    /**
     * Adds a singleton service of the given type to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addSingleton(Class<T> serviceClass) {
        services.put(serviceClass, new SingletonService(serviceClass, null, null, null));
        return this;
    }

    /**
     * Adds a singleton service of the given type and implementation to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param implementationClass the implementation of the service to add
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addSingleton(Class<T> serviceClass, Class<? extends T> implementationClass) {
        services.put(serviceClass, new SingletonService(serviceClass, null, implementationClass, null));
        return this;
    }

    /**
     * Adds a singleton service of the given type and factory to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param factory the factory to use to create the service instance
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addSingleton(Class<T> serviceClass, Function<ServiceProvider, Object> factory) {
        services.put(serviceClass, new SingletonService(serviceClass, null, null, factory));
        return this;
    }

    /**
     * Adds a singleton service of the given type and instance to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param serviceInstance the instance of the service to add
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addSingleton(Class<T> serviceClass, T serviceInstance) {
        services.put(serviceClass, new SingletonService(serviceClass, serviceInstance, null, null));
        return this;
    }

    //#endregion

    //#region Transient

    /**
     * Adds a transient service of the given type to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addTransient(Class<T> serviceClass) {
        services.put(serviceClass, new TransientService(serviceClass, null, null));
        return this;
    }

    /**
     * Adds a transient service of the given type and implementation to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param implementationClass the implementation of the service to add
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addTransient(Class<T> serviceClass, Class<? extends T> implementationClass) {
        services.put(serviceClass, new TransientService(serviceClass, implementationClass, null));
        return this;
    }

    /**
     * Adds a transient service of the given type and factory to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param factory the factory to use to create the service instance
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addTransient(Class<T> serviceClass, Function<ServiceProvider, Object> factory) {
        services.put(serviceClass, new TransientService(serviceClass, null, factory));
        return this;
    }

    //#endregion

    //#region Injection-only

    /**
     * Adds an injection-only service of the given type and factory to the collection.
     *
     * @param serviceClass the type of the service to add
     * @param factory the factory to use to create the service instance
     * @param <T> the type of the service to add
     * @return the service collection builder
     */
    public <T> ServiceCollectionBuilder addInjectionOnly(Class<T> serviceClass, BiFunction<ServiceProvider, Class<?>, T> factory) {
        services.put(serviceClass, new InjectionOnlyService(serviceClass, factory));
        return this;
    }

    //#endregion

    /**
     * Builds the {@link org.di4j.di4j.registry.ServiceRegistry} and creates a {@link RootServiceProvider} from the registered services<br>
     * The {@link ServiceScope} will be registered as a transient service that creates a new scope and {@link RootServiceProvider} will be registered as a singleton service
     * @return A {@link RootServiceProvider} that has all services registered
     */
    public RootServiceProvider build() {
        var serviceMap = new HashMap<Class<?>, Service<?>>();

        services.forEach((clazz, service) -> serviceMap.put(clazz, constructService(service)));

        // Register the ServiceScope as a transient service to allow services to get a new scope if wanted
        var scopeService = new Service<ServiceScope>(ServiceScope.class, ServiceProvider::getScope);
        scopeService.setTransient(true);
        serviceMap.put(ServiceScope.class, scopeService);

        // Register the RootServiceProvider as a service to allow getting the RootServiceProvider
        var rootServiceProviderService = new Service<RootServiceProvider>(RootServiceProvider.class, RootServiceProvider.class::cast);
        rootServiceProviderService.setSingleton(true); // Setting this to a singleton will force this to run in the root service provider
        serviceMap.put(RootServiceProvider.class, rootServiceProviderService);

        var registry = new ServiceRegistry(serviceMap);
        return new RootServiceProvider(registry);
    }

    private <T> Service<T> constructService(IService typeService) {
        Service<T> service = null;
        switch (typeService.getType()) {
            case SCOPED -> {
                var scopedService = (ScopedService<T>) typeService;
                if (scopedService.implementationClazz != null) {
                    service = new Service<>(scopedService.clazz, scopedService.implementationClazz);
                } else if(scopedService.factory != null) {
                    service = new Service<>(scopedService.clazz, scopedService.factory);
                } else {
                    service = new Service<>(scopedService.clazz);
                }
                service.setScoped(true);
            }
            case SINGLETON -> {
                var singletonService = (SingletonService<T>) typeService;

                if (singletonService.instance != null) {
                    service = new Service<>(singletonService.clazz, singletonService.instance);
                } else if (singletonService.implementationClazz != null) {
                    service = new Service<>(singletonService.clazz, singletonService.implementationClazz);
                } else if(singletonService.factory != null) {
                    service = new Service<>(singletonService.clazz, singletonService.factory);
                } else {
                    service = new Service<>(singletonService.clazz);
                }
                service.setSingleton(true);
            }
            case TRANSIENT-> {
                var transientService = (TransientService<T>) typeService;
                if (transientService.implementationClazz != null) {
                    service = new Service<>(transientService.clazz, transientService.implementationClazz);
                } else if(transientService.factory != null) {
                    service = new Service<>(transientService.clazz, transientService.factory);
                } else {
                    service = new Service<>(transientService.clazz);
                }
                service.setTransient(true);
            }
            case INJECTION_ONLY -> {
                var injectionOnlyService = (InjectionOnlyService<T>) typeService;
                service = new Service<>(injectionOnlyService.clazz, injectionOnlyService.factory);
            }
        }

        return service;
    }


    private interface IService { ServiceType getType(); }
    private record ScopedService<T>(Class<T> clazz, Class<? extends T> implementationClazz, Function<ServiceProvider, T> factory) implements IService {
        @Override
        public ServiceType getType() {
            return ServiceType.SCOPED;
        }
    }
    private record SingletonService<T>(Class<T> clazz, T instance, Class<? extends T> implementationClazz, Function<ServiceProvider, T> factory) implements IService {
        @Override
        public ServiceType getType() {
            return ServiceType.SINGLETON;
        }
    }
    private record TransientService<T>(Class<T> clazz, Class<? extends T> implementationClazz, Function<ServiceProvider, T> factory) implements IService {
        @Override
        public ServiceType getType() {
            return ServiceType.TRANSIENT;
        }
    }
    private record InjectionOnlyService<T>(Class<T> clazz, BiFunction<ServiceProvider, Class<?>, ? extends T> factory) implements IService {
        @Override
        public ServiceType getType() {
            return ServiceType.INJECTION_ONLY;
        }
    }

    private enum ServiceType { SCOPED, SINGLETON, TRANSIENT, INJECTION_ONLY }
}
