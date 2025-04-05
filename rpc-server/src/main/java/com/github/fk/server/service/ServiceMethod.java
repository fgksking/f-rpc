package com.github.fk.server.service;

import java.lang.reflect.Method;
import java.util.List;

public interface ServiceMethod {

    String name();

    Class[] paramTypes();

    List<String> paramTypeNames();

    Method method();
}
