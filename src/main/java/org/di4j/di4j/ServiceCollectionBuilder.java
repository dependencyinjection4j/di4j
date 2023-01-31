package org.di4j.di4j;

import org.di4j.di4j.registry.Service;
import org.di4j.di4j.registry.ServiceRegistry;
import org.di4j.di4j.scope.ServiceScope;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ServiceCollectionBuilder {

    private final Map<Class<?>, IService> services = new HashMap<>();

    //#region Scoped
    public <T> void addScoped(Class<T> serviceClass) {
        services.put(serviceClass, new ScopedService(serviceClass, null, null));
    }

    public <T> void addScoped(Class<T> serviceClass, Class<? extends T> implementationClass) {
        services.put(serviceClass, new ScopedService(serviceClass, implementationClass, null));
    }

    public <T> void addScoped(Class<T> serviceClass, Function<ServiceProvider, Object> factory) {
        services.put(serviceClass, new ScopedService(serviceClass, null, factory));
    }
    //#endregion


    //#region Singleton
    public <T> void addSingleton(Class<T> serviceClass) {
        services.put(serviceClass, new SingletonService(serviceClass, null, null, null));
    }

    public <T> void addSingleton(Class<T> serviceClass, Class<? extends T> implementationClass) {
        services.put(serviceClass, new SingletonService(serviceClass, null, implementationClass, null));
    }

    public <T> void addSingleton(Class<T> serviceClass, Function<ServiceProvider, Object> factory) {
        services.put(serviceClass, new SingletonService(serviceClass, null, null, factory));
    }

    public <T> void addSingleton(Class<T> serviceClass, T serviceInstance) {
        services.put(serviceClass, new SingletonService(serviceClass, serviceInstance, null, null));
    }
    //#endregion

    //#region Transient
    public <T> void addTransient(Class<T> serviceClass) {
        services.put(serviceClass, new TransientService(serviceClass, null, null));
    }

    public <T> void addTransient(Class<T> serviceClass, Class<? extends T> implementationClass) {
        services.put(serviceClass, new TransientService(serviceClass, implementationClass, null));
    }

    public <T> void addTransient(Class<T> serviceClass, Function<ServiceProvider, Object> factory) {
        services.put(serviceClass, new TransientService(serviceClass, null, factory));
    }
    //#endregion

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
        Service<?> service = null;
        switch (typeService.getType()) {
            case SCOPED -> {
                var scopedService = (ScopedService) typeService;
                if (scopedService.implementationClazz != null) {
                    service = new Service<T>((Class<T>) scopedService.clazz, (T) scopedService.implementationClazz);
                } else if(scopedService.factory != null) {
                    service = new Service<T>((Class<T>) scopedService.clazz, (T) scopedService.factory);
                } else {
                    service = new Service<T>((Class<T>) scopedService.clazz);
                }
                service.setScoped(true);
            }
            case SINGLETON -> {
                var singletonService = (SingletonService) typeService;

                if (singletonService.instance != null) {
                    service = new Service<T>((Class<T>) singletonService.clazz, (T) singletonService.instance);
                } else if (singletonService.implementationClazz != null) {
                    service = new Service<T>((Class<T>) singletonService.clazz, (T) singletonService.implementationClazz);
                } else if(singletonService.factory != null) {
                    service = new Service<T>((Class<T>) singletonService.clazz, (T) singletonService.factory);
                } else {
                    service = new Service<T>((Class<T>) singletonService.clazz);
                }
                service.setSingleton(true);
            }
            case TRANSIENT-> {
                var transientService = (ScopedService) typeService;
                if (transientService.implementationClazz != null) {
                    service = new Service<T>((Class<T>) transientService.clazz, (T) transientService.implementationClazz);
                } else if(transientService.factory != null) {
                    service = new Service<T>((Class<T>) transientService.clazz, (T) transientService.factory);
                } else {
                    service = new Service<T>((Class<T>) transientService.clazz);
                }
                service.setTransient(true);
            }
        }

        return (Service<T>) service;
    }


    private interface IService { ServiceType getType(); }
    private record ScopedService(Class<?> clazz, Class<?> implementationClazz, Function<ServiceProvider, Object> factory) implements IService {
        @Override
        public ServiceType getType() {
            return ServiceType.SCOPED;
        }
    }
    private record SingletonService(Class<?> clazz, Object instance, Class<?> implementationClazz, Function<ServiceProvider, Object> factory) implements IService {
        @Override
        public ServiceType getType() {
            return ServiceType.SINGLETON;
        }
    }
    private record TransientService(Class<?> clazz, Class<?> implementationClazz, Function<ServiceProvider, Object> factory) implements IService {
        @Override
        public ServiceType getType() {
            return ServiceType.TRANSIENT;
        }
    }
    private enum ServiceType { SCOPED, SINGLETON, TRANSIENT }
}
