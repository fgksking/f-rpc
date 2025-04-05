package com.github.fk.client.support.fail.impl;

import com.github.fk.client.proxy.RemoteInvokeContext;
import com.github.fk.client.support.fail.FailStrategy;
import com.github.fk.common.exception.RpcRuntimeException;
import com.github.fk.common.exception.RpcTimeoutException;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.rpc.domain.impl.RpcResponses;

/**
 * 重试策略
 */
public class FailOverStrategy implements FailStrategy {
    @Override
    public Object fail(RemoteInvokeContext context) {
        try {
            Class returnType = context.request().returnType();
            RpcResponse rpcResponse = context.rpcResponse();
            return RpcResponses.getResult(rpcResponse,returnType);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            if(throwable instanceof RpcTimeoutException) {
                throw new RpcRuntimeException();
            }

            // 进行失败重试。
            int retryTimes = context.retryTimes();
            if(retryTimes > 0) {
                // 进行重试
                retryTimes--;
                context.retryTimes(retryTimes);
                return context.remoteInvokeService()
                        .remoteInvoke(context);
            } else {
                throw e;
            }
        }
    }
}
