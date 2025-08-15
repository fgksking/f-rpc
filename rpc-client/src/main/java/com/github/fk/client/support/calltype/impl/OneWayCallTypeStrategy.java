package com.github.fk.client.support.calltype.impl;

import com.github.fk.client.proxy.ServiceContext;
import com.github.fk.client.support.calltype.CallTypeStrategy;
import com.github.fk.common.rpc.domain.RpcRequest;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.rpc.domain.impl.RpcResponses;

/**
 * 调用方不关心结果
 */
public class OneWayCallTypeStrategy implements CallTypeStrategy {

    private static  CallTypeStrategy callTypeStrategy =new OneWayCallTypeStrategy();

    public  static CallTypeStrategy getInstance(){
        return callTypeStrategy;
    }
    @Override
    public RpcResponse result(ServiceContext proxyContext, RpcRequest rpcRequest) {
        final String seqId = rpcRequest.seqId();
        RpcResponse rpcResponse = RpcResponses.result(null, rpcRequest.returnType());
        return rpcResponse;
    }
}
