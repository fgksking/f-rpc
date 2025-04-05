package com.github.fk.client.support.fail.impl;

import com.github.fk.client.proxy.RemoteInvokeContext;
import com.github.fk.client.support.fail.FailStrategy;
import com.github.fk.common.rpc.domain.impl.RpcResponses;

public class FailFastStrategy implements FailStrategy {

    @Override
    public Object fail(RemoteInvokeContext context) {
        Class returnType = context.request().returnType();
        return RpcResponses.getResult(context.rpcResponse(),returnType);
    }
}
