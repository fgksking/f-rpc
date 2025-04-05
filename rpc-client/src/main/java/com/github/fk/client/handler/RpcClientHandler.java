package com.github.fk.client.handler;

import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.support.invoke.InvokeManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 *   客户端处理类
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler {
    private final InvokeManager invokeManager;

    public RpcClientHandler(InvokeManager invokeManager) {
        this.invokeManager = invokeManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcResponse rpcResponse = (RpcResponse)msg;
        log.info("客户端接读取到response ={} ",rpcResponse);
        invokeManager.addResponse(rpcResponse.seqId(), rpcResponse);
        log.info("[Client] server response is :{}", rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[Rpc Client] meet ex ", cause);
        ctx.close();
    }
}
