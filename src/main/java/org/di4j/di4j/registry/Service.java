package org.di4j.di4j.registry;

import org.di4j.di4j.ServiceProvider;
import org.di4j.di4j.annotations.ServiceProviderConstructor;
import org.di4j.di4j.exceptions.ClassNotAssignableException;
import org.di4j.di4j.exceptions.InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException;
import org.di4j.di4j.exceptions.InvalidConstructorCountException;
import org.di4j.di4j.exceptions.MissingServiceException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The `Service` class represents a service that can be provided by a service provider.<br>
 * It contains information about the service's class, implementation, constructor, and creation method.<br>
 * It also provides methods to retrieve the service's required services and to create an instance of the service.<br>
 */
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

    /**
     * Creates a new service with the given class.<br>
     * The service will be created using the class's constructor.<br>
     *
     * @param clazz the class of the service
     */
    public Service(Class<T> clazz) {
        this.clazz = clazz;
        extractConstructor(clazz);
    }

    /**
     * Creates a new service with the given class and singleton object.<br>
     * The service will be a singleton and will return the given object every time it is requested.<br>
     *
     * @param clazz the class of the service
     * @param singletonObject the singleton object to return
     */
    public Service(Class<T> clazz, T singletonObject) {
        this.clazz = clazz;
        this.singletonObject = singletonObject;
        this.isSingleton = true;
    }

    /**
     * Creates a new service with the given class and factory.<br>
     * The service will be created using the factory.<br>
     *
     * @param clazz the class of the service
     * @param factory the factory to create the service
     */
    public Service(Class<T> clazz, Function<ServiceProvider, ? extends T> factory) {
        this.clazz = clazz;
        this.factory = factory;
    }

    /**
     * Creates a new service with the given class and injection-only factory.<br>
     * The service will be created using the injection-only factory.<br>
     *
     * @param clazz the class of the service
     * @param factory the injection-only factory to create the service
     */
    public Service(Class<T> clazz, BiFunction<ServiceProvider, Class<?>, ? extends T> factory) {
        this.clazz = clazz;
        this.injectionOnlyFactory = factory;
        isInjectionOnly = true;
    }

    /**
     * Creates a new service with the given class and implementation class.<br>
     * The service will be created using the implementation class's constructor.<br>
     *
     * @param clazz the class of the service
     * @param implementation the implementation class of the service
     */
    public Service(Class<T> clazz, Class<? extends T> implementation) {
        this.clazz = clazz;
        this.implementationClazz = implementation;
        extractConstructor(implementation);
    }

    /**
     * Extracts the constructor for the service.<br>
     * If the class has a service provider constructor, uses that constructor.<br>
     * Otherwise, uses the first constructor.<br>
     *
     * @param clazz the class to extract the constructor from
     * @throws InvalidConstructorCountException if the class does not have exactly one constructor or has more than one service provider constructor
     */
    private void extractConstructor(Class<?> clazz) {
        // Verify constructors
        var constructors = clazz.getConstructors();
        var serviceProviderConstructor = Arrays.stream(constructors).filter(x -> x.isAnnotationPresent(ServiceProviderConstructor.class)).toList();
        if(constructors.length != 1 && serviceProviderConstructor.isEmpty()) throw new InvalidConstructorCountException("The class " + clazz.getName() + " does not have exactly one constructor");
        if(serviceProviderConstructor.size() > 1) throw new InvalidConstructorCountException("The class " + clazz.getName() + " has more than one service provider constructor");

        // Extract the constructor
        constructor = serviceProviderConstructor.isEmpty() ? constructors[0] : serviceProviderConstructor.get(0);
    }

    /**
     * Returns an instance of the service.<br>
     * If the service is a singleton and an instance has already been created, returns the existing instance.<br>
     * If the service has a factory, uses the factory to create an instance.<br>
     * If the service has an injection-only factory and an injection target is provided, uses the factory to create an instance.<br>
     * If none of the above apply, creates an instance of the service using the constructor with the given parameters.<br>
     *
     * @param collection the service provider to retrieve services from
     * @param injectInto the class to inject the service into (if using an injection-only factory)
     * @return an instance of the service
     * @throws InvocationTargetException if the constructor throws an exception
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the constructor is not accessible
     * @throws MissingServiceException if a service instance cannot be retrieved
     * @throws ClassNotAssignableException if the instance cannot be assigned to the class
     * @throws InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException if an injection-only factory is used without an injection target
     */
    public T getInstance(ServiceProvider collection, Class<?> injectInto) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if(isSingleton && singletonObject != null) {
            return singletonObject;
        } else if(factory != null) {
            return factory.apply(collection);
        } else if(injectionOnlyFactory != null) {
            if(injectInto != null) {
                return injectionOnlyFactory.apply(collection, injectInto);
            } else {
                throw new InjectionOnlyFactoryCannotBeUsedForNonInjectionServicesException(clazz.getName());
            }
        }
        return createInstanceFromClazz(collection);
    }

    /**
     * Creates an instance of the class using the constructor with the given parameters.<br>
     * If the instance cannot be created, throws a ClassNotAssignableException.<br>
     *
     * @param collection the service provider to retrieve services from
     * @return an instance of the class
     * @throws InvocationTargetException if the constructor throws an exception
     * @throws InstantiationException if the class cannot be instantiated
     * @throws IllegalAccessException if the constructor is not accessible
     * @throws MissingServiceException if a service instance cannot be retrieved
     * @throws ClassNotAssignableException if the created instance cannot be assigned to the class
     */
    private T createInstanceFromClazz(ServiceProvider collection) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var clazz = implementationClazz != null ? implementationClazz : this.clazz; // Get the class depending on the parameters provided

        // Create the constructor parameters
        var paramOrder = new Object[constructor.getParameterCount()];
        var paramTypes = constructor.getParameterTypes();

        // Try to get all services from the constructor
        for (int i = 0; i < paramOrder.length; i++) {
            paramOrder[i] = collection.getService(paramTypes[i], clazz);
            if(paramOrder[i] == null) {
                throw new MissingServiceException("The service " + clazz.getName() + " could not get a instance of the service " + paramTypes[i].getName());
            }
        }

        // Create an instance of the class using the constructor with the given parameters.
        // If the instance cannot be created, throw a ClassNotAssignableException.

        var instance = constructor.newInstance(paramOrder);
        if(!this.clazz.isAssignableFrom(instance.getClass())) throw new ClassNotAssignableException("The class " + instance.getClass().getName() + " is not assignable to " + clazz.getName());
        return this.clazz.cast(instance);
    }



    /**
     * Returns whether the service is a singleton.<br>
     * A singleton service is created only once and the same instance is returned every time it is requested.<br>
     *
     * @return true if the service is a singleton, false otherwise
     */
    public boolean isSingleton() {
        return isSingleton;
    }

    /**
     * Sets whether the service is a singleton.<br>
     * A singleton service is created only once and the same instance is returned every time it is requested.<br>
     *
     * @param singleton true if the service is a singleton, false otherwise
     */
    public void setSingleton(boolean singleton) {
        isSingleton = singleton;
    }

    /**
     * Returns whether the service is transient.<br>
     * A transient service is created every time it is requested.<br>
     *
     * @return true if the service is transient, false otherwise
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Sets whether the service is transient.<br>
     * A transient service is created every time it is requested.<br>
     *
     * @param aTransient true if the service is transient, false otherwise
     */
    public void setTransient(boolean aTransient) {
        isTransient = aTransient;
    }

    /**
     * Returns whether the service is scoped.<br>
     * A scoped service is created once per scope and the same instance is returned every time it is requested within that scope.<br>
     *
     * @return true if the service is scoped, false otherwise
     */
    public boolean isScoped() {
        return isScoped;
    }

    /**
     * Sets whether the service is scoped.<br>
     * A scoped service is created once per scope and the same instance is returned every time it is requested within that scope.<br>
     *
     * @param scoped true if the service is scoped, false otherwise
     */
    public void setScoped(boolean scoped) {
        isScoped = scoped;
    }

    /**
     * Returns whether the service is injection-only.<br>
     * An injection-only service is created only when it is injected into another service.<br>
     *
     * @return true if the service is injection-only, false otherwise
     */
    public boolean isInjectionOnly() {
        return isInjectionOnly;
    }

    /**
     * Sets whether the service is injection-only.<br>
     * An injection-only service is created only when it is injected into another service.<br>
     *
     * @param injectionOnly true if the service is injection-only, false otherwise
     */
    public void setInjectionOnly(boolean injectionOnly) {
        isInjectionOnly = injectionOnly;
    }

    /**
     * Returns a list of the required services for the service's constructor.<br>
     * The list is generated by inspecting the constructor's parameter types.<br>
     *
     * @return a list of the required services for the service's constructor
     */
    public List<? extends Class<?>> getRequiredServices() {
        return Arrays.stream(constructor.getParameterTypes()).toList();
    }

    /**
     * Checks whether the service has a constructor or not.<br>
     * This is used for checking the service's type internally.<br>
     * @return true if the service has a constructor, false otherwise
     */
    public boolean hasConstructor() {
        return constructor != null;
    }

    /**
     * Get service type
     * @return Service type
     */
    public Class<T> getType() {
        return this.clazz;
    }
}
