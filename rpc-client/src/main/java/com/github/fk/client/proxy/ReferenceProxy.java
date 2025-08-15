package com.github.fk.client.proxy;

import java.lang.reflect.InvocationHandler;

public interface ReferenceProxy<T> extends InvocationHandler {
    /**
     * 获取代理类
     * @return
     */

    T proxy();
}
