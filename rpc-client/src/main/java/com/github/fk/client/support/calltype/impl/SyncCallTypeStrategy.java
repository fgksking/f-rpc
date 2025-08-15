package com.github.fk.client.support.calltype.impl;

import com.github.fk.client.proxy.ServiceContext;
import com.github.fk.client.support.calltype.CallTypeStrategy;
import com.github.fk.common.rpc.domain.RpcRequest;
import com.github.fk.common.rpc.domain.RpcResponse;

/**
 * 同步调用实现类
 */
public class SyncCallTypeStrategy implements CallTypeStrategy {

    private static final CallTypeStrategy INSTANCE = new SyncCallTypeStrategy();

    static CallTypeStrategy getInstance(){
        return INSTANCE;
    }

    @Override
    public RpcResponse result(ServiceContext proxyContext, RpcRequest rpcRequest) {
        final String seqId = rpcRequest.seqId();
        return proxyContext.invokeManager().getResponse(seqId);
    }
}
