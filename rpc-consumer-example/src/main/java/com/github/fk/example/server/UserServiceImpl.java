package com.github.fk.example.server;

import com.github.fk.example.common.User;
import com.github.fk.example.common.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User getUser(String id) {
        return new User("zhansan",100);
    }
}
