package com.github.fk.server.config;

public class DefaultServiceConfig<T> implements ServiceConfig<T>{
    private String id;
    private T reference;
    private boolean register = true;

    /**
     * 延迟暴露的毫秒数
     */
    private long delay = 0;
    @Override
    public String id() {
        return id;
    }

    @Override
    public DefaultServiceConfig<T> id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public T reference() {
        return reference;
    }

    @Override
    public DefaultServiceConfig<T> reference(T reference) {
        this.reference = reference;
        return this;
    }

    @Override
    public boolean register() {
        return register;
    }

    @Override
    public DefaultServiceConfig<T> register(boolean register) {
        this.register = register;
        return this;
    }

    @Override
    public long delay() {
        return delay;
    }

    @Override
    public DefaultServiceConfig<T> delay(long delay) {
        this.delay = delay;
        return this;
    }
}
