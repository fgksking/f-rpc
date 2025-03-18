package com.github.fk.core;

import com.github.fk.config.ServiceConfig;
import com.github.fk.register.ServiceRegistry;

import java.util.List;

public class ServerBus implements ServiceRegistry {

    private List<ServiceConfig> serviceConfigList;
    private int port;
    @Override
    public ServiceRegistry register(int serviceId, Object ServiceImpl) {
        return null;
    }

    @Override
    public ServiceRegistry port(int port) {
        return null;
    }

    @Override
    public ServiceRegistry register(ServiceConfig config) {
        return null;
    }

    @Override
    public ServiceRegistry expose() {
        return null;
    }
}
