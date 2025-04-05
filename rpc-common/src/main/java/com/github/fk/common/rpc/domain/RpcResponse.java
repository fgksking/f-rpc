package com.github.fk.common.rpc.domain;

public interface RpcResponse extends BaseRpc{

    Throwable error();

    Object result();
}
