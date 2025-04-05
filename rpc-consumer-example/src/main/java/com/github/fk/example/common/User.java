package com.github.fk.example.common;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private Integer id;

    public User(String name, Integer id) {
        this.name = name;
        this.id = id;
    }
}
