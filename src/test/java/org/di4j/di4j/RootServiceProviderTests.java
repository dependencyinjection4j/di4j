package org.di4j.di4j;

import org.di4j.di4j.RootServiceProvider;
import org.di4j.di4j.ServiceProvider;
import org.di4j.di4j.registry.Service;
import org.di4j.di4j.registry.ServiceRegistry;
import org.di4j.di4j.scope.ServiceScope;
import org.di4j.di4j.services.*;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RootServiceProviderTests {

    @Test
    public void canCreateRootServiceProvider() {
        ServiceRegistry registry = new ServiceRegistry(new HashMap<>());
        assertNotNull(registry);
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

    @Test
    public void canGetLoadOrder() {
        ServiceCollectionBuilder builder = new ServiceCollectionBuilder();
        builder.addSingleton(TestServiceLevel1.class);
        builder.addSingleton(TestServiceLevel1A.class);
        builder.addSingleton(TestServiceLevel2.class);
        builder.addSingleton(TestServiceLevel3.class);
        builder.addSingleton(TestServiceLevel3A.class);
        builder.addSingleton(TestServiceLevel4.class);

        RootServiceProvider rsp = builder.build();
        ServiceRegistry registry = rsp.getRegistry();
        assertNotNull(registry);

        List<Service<?>> loadOrder = registry.getLoadOrder();
        assertNotNull(loadOrder);
        assertEquals(6, loadOrder.size());

        assertTrue(TestServiceLevel1.class == loadOrder.get(0).getType() || TestServiceLevel1A.class == loadOrder.get(0).getType());
        assertTrue(TestServiceLevel1.class == loadOrder.get(1).getType() || TestServiceLevel1A.class == loadOrder.get(1).getType());
        assertEquals(TestServiceLevel2.class, loadOrder.get(2).getType());
        assertTrue(TestServiceLevel3.class == loadOrder.get(3).getType() || TestServiceLevel3A.class == loadOrder.get(3).getType());
        assertTrue(TestServiceLevel3.class == loadOrder.get(4).getType() || TestServiceLevel3A.class == loadOrder.get(4).getType());
        assertEquals(TestServiceLevel4.class, loadOrder.get(5).getType());
    }
}