package org.di4j.di4j.registry;

import org.di4j.di4j.ServiceProvider;
import org.di4j.di4j.annotations.ServiceProviderConstructor;
import org.di4j.di4j.exceptions.ClassNotAssignableException;
import org.di4j.di4j.exceptions.InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException;
import org.di4j.di4j.exceptions.InvalidConstructorCountException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Service<T> {

    private Constructor<? extends Object> constructor;
    private boolean isSingleton;
    private boolean isTransient;
    private boolean isScoped;
    private boolean isInjectionOnly;

    private Class<T> clazz;
    private Class<? extends T> implementationClazz;

    private T singletonObject;

    private Function<ServiceProvider, ? extends T> factory;
    private BiFunction<ServiceProvider, Class<?>, ? extends T> injectionOnlyFactory;

    private List<? extends Class<?>> requiredServices;

    public Service(Class<T> clazz) {
        this.clazz = clazz;
        extractConstructor(clazz);
    }

    public Service(Class<T> clazz, T singletonObject) {
        this.clazz = clazz;
        this.singletonObject = singletonObject;
        this.isSingleton = true;
    }

    public Service(Class<T> clazz, Function<ServiceProvider, ? extends T> factory) {
        this.clazz = clazz;
        this.factory = factory;
    }

    public Service(Class<T> clazz, BiFunction<ServiceProvider, Class<?>, ? extends T> factory) {
        this.clazz = clazz;
        this.injectionOnlyFactory = factory;
        isInjectionOnly = true;
    }

    public Service(Class<T> clazz, Class<? extends T> implementation) {
        this.clazz = clazz;
        this.implementationClazz = implementation;
        extractConstructor(implementation);
    }

    private void extractConstructor(Class<?> clazz) {
        // Verify constructors
        var constructors = clazz.getConstructors();
        var serviceProviderConstructor = Arrays.stream(constructors).filter(x -> x.isAnnotationPresent(ServiceProviderConstructor.class)).toList();
        if(constructors.length != 1 && serviceProviderConstructor.isEmpty()) throw new InvalidConstructorCountException("The class " + clazz.getName() + " does not have exactly one constructor");
        if(serviceProviderConstructor.size() > 1) throw new InvalidConstructorCountException("The class " + clazz.getName() + " has more than one service provider constructor");

        // Extract the constructor
        constructor = serviceProviderConstructor.isEmpty() ? constructors[0] : serviceProviderConstructor.get(0);
    }

    public T getInstance(ServiceProvider collection, Class<?> injectInto) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if(isSingleton) {
            return singletonObject;
        } else if(factory != null) {
            return factory.apply(collection);
        } else if(injectionOnlyFactory != null) {
            if(injectInto != null) {

            } else {
                throw new InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException(clazz.getName());
            }
        }
        return createInstanceFromClazz(collection);
    }

    private T createInstanceFromClazz(ServiceProvider collection) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var clazz = implementationClazz != null ? implementationClazz : this.clazz; // Get the class depending on the parameters provided

        // Create the constructor parameters
        var paramOrder = new Object[constructor.getParameterCount()];
        var paramTypes = constructor.getParameterTypes();

        for (int i = 0; i < paramOrder.length; i++) {
            paramOrder[i] = collection.getService(paramTypes[i], clazz);
        }

        var instance = constructor.newInstance(paramOrder);
        if(!this.clazz.isAssignableFrom(instance.getClass())) throw new ClassNotAssignableException("The class " + instance.getClass().getName() + " is not assignable to " + clazz.getName());
        return this.clazz.cast(instance);
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean aTransient) {
        isTransient = aTransient;
    }

    public boolean isScoped() {
        return isScoped;
    }

    public void setScoped(boolean scoped) {
        isScoped = scoped;
    }

    public boolean isInjectionOnly() {
        return isInjectionOnly;
    }

    public void setInjectionOnly(boolean injectionOnly) {
        isInjectionOnly = injectionOnly;
    }

    public List<? extends Class<?>> getRequiredServices() {
        return Arrays.stream(constructor.getParameterTypes()).toList();
    }
}
