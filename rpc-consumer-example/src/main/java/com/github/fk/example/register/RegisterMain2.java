package com.github.fk.example.register;

import com.github.fk.register.core.RegisterBs;

public class RegisterMain2 {

    public static void main(String[] args) {
        RegisterBs.newInstance().port(8090).start();
    }
}
