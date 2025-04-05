package com.github.fk.example.server;

import com.github.fk.example.common.ServiceTestConst;
import com.github.fk.example.common.UserService;
import com.github.fk.server.core.ServerBs;

public class ServerMain {
    public static void main(String[] args) throws Exception{
        ServerBs.getInstance()
                .register(UserService.class.getName(),new UserServiceImpl())
                .registerCenter(ServiceTestConst.REGISTER_CENTER)
                .expose();
    }
}
