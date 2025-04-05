package com.github.fk.example.server;

import com.github.fk.example.common.ServiceTestConst;
import com.github.fk.example.common.UserService;
import com.github.fk.server.core.ServerBs;

public class ServerMain2 {

    public static void main(String[] args) throws Exception{
        ServerBs.getInstance()
                .register(UserService.class.getName(),new UserServiceImpl())
                .registerCenter(ServiceTestConst.MANY_REGISTER_CENTER)
                .port(8083)
                .expose();
    }
}
