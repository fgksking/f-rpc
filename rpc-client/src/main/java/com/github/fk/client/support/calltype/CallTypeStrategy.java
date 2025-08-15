package com.github.fk.client.support.calltype;

import com.github.fk.client.proxy.ServiceContext;
import com.github.fk.common.rpc.domain.RpcRequest;
import com.github.fk.common.rpc.domain.RpcResponse;

/**
 * 调用方式上下文
 */
public interface CallTypeStrategy {

    RpcResponse result(final ServiceContext proxyContext,
                       final RpcRequest rpcRequest);
}
