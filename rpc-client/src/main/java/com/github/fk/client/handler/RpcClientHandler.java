package com.github.fk.client.handler;

import com.github.fk.client.task.ClientRpcTask;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.threadpoll.manager.ExecutorRepository;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

/**
 * 客户端处理类
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler {
    private final InvokeManager invokeManager;

    private final Executor executor;


    public RpcClientHandler(InvokeManager invokeManager,Executor executor) {
        this.invokeManager = invokeManager;
        this.executor = executor;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        RpcResponse rpcResponse = (RpcResponse) msg;
        ClientRpcTask clientRpcTask = new ClientRpcTask(rpcResponse,invokeManager);
        if(executor ==null){
            handleResponseDirectly(rpcResponse);
            return;
        }
        try {
            executor.execute(clientRpcTask);
        } catch (Exception e) {
            log.error("client线程池执行异常");
        }
        log.info("客户端接读取到response ={} ", rpcResponse);
        invokeManager.addResponse(rpcResponse.seqId(), rpcResponse);
        log.info("[Client] server response is :{}", rpcResponse);
    }
    /**
     * 降级处理：直接在当前线程处理响应
     */
    private void handleResponseDirectly(RpcResponse rpcResponse) {
        final String seqId = rpcResponse.seqId();
        log.info("[Client] 直接处理响应, seqId: {}", seqId);
        invokeManager.addResponse(seqId, rpcResponse);
        log.info("[Client] server response is :{}", rpcResponse);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("[Rpc Client] meet ex ", cause);
        ctx.close();
    }
}
