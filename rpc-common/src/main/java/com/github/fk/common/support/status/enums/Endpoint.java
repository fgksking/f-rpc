package com.github.fk.common.support.status.enums;

public enum Endpoint {

    CONSUMER("consumer"),
    SERVER("server");
    private final String name;


    Endpoint(String name) {
        this.name = name;
    }
}
