package com.github.fk.common.support.inteceptor.impl;

import com.github.fk.common.support.inteceptor.RpcInterceptor;

public class RpcInterceptors {

    public static RpcInterceptor none() {
        return new NoneRpcInterceptor();
    }
}
