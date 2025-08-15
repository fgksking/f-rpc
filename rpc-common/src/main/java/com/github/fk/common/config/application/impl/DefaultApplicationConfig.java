package com.github.fk.common.config.application.impl;

import com.github.fk.common.config.application.ApplicationConfig;

public class DefaultApplicationConfig implements ApplicationConfig {

    private String name;
    private String env;

    public DefaultApplicationConfig name(String name){
        this.name = name;
        return this;
    }
    @Override
    public String name() {
        return name;
    }
    public DefaultApplicationConfig env(String env) {
        this.env = env;
        return this;
    }

    @Override
    public String env() {
        return env;
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" +
                "name='" + name + '\'' +
                ", env='" + env + '\'' +
                '}';
    }
}
