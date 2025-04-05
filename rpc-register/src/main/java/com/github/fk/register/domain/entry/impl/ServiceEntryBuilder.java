package com.github.fk.register.domain.entry.impl;

import com.github.fk.register.domain.entry.ServiceEntry;

public class ServiceEntryBuilder {
    public static ServiceEntry of(final String serviceId,
                                  final String ip,
                                  final int port) {
        DefaultServiceEntry entry = new DefaultServiceEntry();
        entry.serviceId(serviceId).port(port).ip(ip);
        return entry;
    }
    public static ServiceEntry of(final String serviceId) {
        return of(serviceId, null, 0);
    }

}
