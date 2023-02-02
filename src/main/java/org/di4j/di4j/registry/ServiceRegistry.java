package org.di4j.di4j.registry;

import org.di4j.di4j.ServiceProvider;
import org.di4j.di4j.exceptions.FailedToInstantiateServiceException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {


    private Map<Class<?>, Service<?>> services = new HashMap<>();

    public ServiceRegistry(Map<Class<?>, Service<?>> services) {
        this.services = services;
    }

    public <T> T getService(Class<T> type, ServiceProvider serviceProvider) {
        return getService(type, serviceProvider, null);
    }

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
            throw new FailedToInstantiateServiceException("The service " + type.getName() + " could not be instantiated");
        }
    }

    public <T> Service<T> getRegistration(Class<T> type) {
        return (Service<T>) services.get(type);
    }



}
