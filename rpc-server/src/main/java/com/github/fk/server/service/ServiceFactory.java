package com.github.fk.server.service;

import com.github.fk.server.config.ServiceConfig;

import java.util.List;

public interface ServiceFactory {

    ServiceFactory registerServicesLocal(final List<ServiceConfig> serviceConfigList);

    Object invoke(final String serviceId, final String methodName,
                  List<String> paramTypeNames, final Object[] paramValues);
}
