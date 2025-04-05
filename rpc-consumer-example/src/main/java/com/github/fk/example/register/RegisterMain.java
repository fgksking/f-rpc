package com.github.fk.example.register;

import com.github.fk.example.common.ServiceTestConst;
import com.github.fk.register.core.RegisterBs;

public class RegisterMain {

    public static void main(String[] args) {
        RegisterBs.newInstance().port(8080).start();
    }
}
