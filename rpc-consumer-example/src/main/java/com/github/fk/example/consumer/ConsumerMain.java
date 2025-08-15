package com.github.fk.example.consumer;

import com.github.fk.client.config.ReferenceConfig;
import com.github.fk.client.core.ClientBus;
import com.github.fk.example.common.ServiceTestConst;
import com.github.fk.example.common.User;
import com.github.fk.example.common.UserService;

public class ConsumerMain {
    public static void main(String[] args) {
        ReferenceConfig<UserService> config = ClientBus.newInstance();
        System.out.println(UserService.class.getName());
        config.serviceId(UserService.class.getName());
        config.serviceInterface(UserService.class);
        config.subscribe(true);
        config.registerCenter(ServiceTestConst.REGISTER_CENTER);
        UserService reference = config.reference();
        User user = reference.getUser("12");
        System.out.println(user);


    }
}
