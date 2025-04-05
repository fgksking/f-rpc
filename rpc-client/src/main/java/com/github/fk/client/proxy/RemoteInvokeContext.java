package com.github.fk.client.proxy;

import com.github.fk.client.proxy.impl.DefaultRemoteInvokeContext;
import com.github.fk.common.rpc.domain.RpcChannelFuture;
import com.github.fk.common.rpc.domain.RpcRequest;
import com.github.fk.common.rpc.domain.RpcResponse;

public interface RemoteInvokeContext<T>{
    RpcRequest request();

    ServiceContext<T> serviceProxyContext();

    RemoteInvokeContext<T> channelFuture(final RpcChannelFuture channelFuture);

    /**
     * 请求响应结果
     * @return 请求响应结果
     */
    RpcResponse rpcResponse();

    /**
     * 请求响应结果
     * @param rpcResponse 响应结果
     * @return 请求响应结果
     */
    DefaultRemoteInvokeContext<T> rpcResponse(final RpcResponse rpcResponse);

    /**
     * 获取重试次数
     * @return 重试次数
     */
    int retryTimes();

    /**
     * 设置重试次数
     * @param retryTimes 设置重试次数
     * @return this
     */
    DefaultRemoteInvokeContext<T> retryTimes(final int retryTimes);

    /**
     * 在整个调用生命周期中唯一的标识号
     * （1）重试也不会改变
     * （2）只在第一次调用的时候进行设置。
     * @return 订单号
     */
    String traceId();

    /**
     * 远程调用服务信息
     * @return 远程调用服务信息
     */
    RemoteInvokeService remoteInvokeService();
}
