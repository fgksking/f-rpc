package com.github.fk.config;

public class DefaultServiceConfig<T> implements ServiceConfig<T>{
    private String id;
    private T reference;
    @Override
    public String id() {
        return null;
    }

    @Override
    public ServiceConfig<T> id(String id) {
        return null;
    }

    @Override
    public T reference(String id) {
        return null;
    }

    @Override
    public ServiceConfig<T> reference(T reference) {
        return null;
    }
}
