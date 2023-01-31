package org.di4j.di4j;

import org.di4j.di4j.RootServiceProvider;
import org.di4j.di4j.ServiceProvider;
import org.di4j.di4j.registry.Service;
import org.di4j.di4j.registry.ServiceRegistry;
import org.di4j.di4j.scope.ServiceScope;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class RootServiceProviderTests {

    @Test
    public void canCreateRootServiceProvider() {
        ServiceRegistry registry = new ServiceRegistry(new HashMap<>());
        ServiceProvider provider = new RootServiceProvider(registry);
        assertNotNull(provider);
    }

    @Test
    public void canCreateServiceScope() {
        ServiceRegistry registry = new ServiceRegistry(new HashMap<>());
        ServiceProvider provider = new RootServiceProvider(registry);
        assertNotNull(provider.getScope());
    }

    @Test
    public void canGetService() {
        Service<ServiceScope> scopeService = new Service<ServiceScope>(ServiceScope.class, ServiceProvider::getScope);
        scopeService.setTransient(true);
        Map<Class<?>, Service<?>> serviceMap = new HashMap<>();
        serviceMap.put(ServiceScope.class, scopeService);
        ServiceRegistry registry = new ServiceRegistry(serviceMap);
        ServiceProvider provider = new RootServiceProvider(registry);
        assertNotNull(provider.getService(ServiceScope.class));
    }

}
