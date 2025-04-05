package com.github.fk.server.handler;


import com.github.fk.common.constant.CallTypeEnum;
import com.github.fk.common.rpc.domain.RpcRequest;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.rpc.domain.impl.DefaultRpcResponse;
import com.github.fk.common.rpc.domain.impl.RpcResponseFactory;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.status.StatusManager;
import com.github.fk.common.support.status.enums.StatusEnum;
import com.github.fk.server.service.impl.DefaultServiceFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler {
    /**
     * 调用管理类
     */
    private final InvokeManager invokeManager;

    /**
     * 状态管理类
     */
    private final StatusManager statusManager;

    public RpcServerHandler(InvokeManager invokeManager, StatusManager statusManager) {
        this.invokeManager = invokeManager;
        this.statusManager = statusManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String id = ctx.channel().id().asLongText();
        log.info("[Server] channel {} connected " + id);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        final String id = ctx.channel().id().asLongText();
        log.info("[Server] channel read start: {}", id);

        // 1. 接受客户端请求
        RpcRequest rpcRequest = (RpcRequest)msg;
        final String seqId = rpcRequest.seqId();
        try {
            log.info("[Server] receive seqId: {} request: {}", seqId, rpcRequest);
            // 2. 设置请求信息和超时时间
            invokeManager.addRequest(rpcRequest.seqId(), rpcRequest.timeout());

            // 3. 回写到 client 端
            //3.1 获取结果
            RpcResponse rpcResponse = this.handleRpcRequest(rpcRequest);
            //3.2 回写结果
            final CallTypeEnum callType = rpcRequest.callType();
            if(CallTypeEnum.SYNC.equals(callType)) {
                log.info("SYNC 嗲用");
                if(ctx.channel().isActive()) {
                    ctx.writeAndFlush(rpcResponse).addListener(future -> {
                        if(!future.isSuccess()) {
                            log.error("发送响应失败，seqId: {}", seqId, future.cause());
                        }
                    });
                } else {
                    log.warn("Channel已关闭，无法发送响应，seqId: {}", seqId);
                }
            } else {
                log.info("[Server] seqId: {} callType: {} ignore write back.", seqId, callType);
            }
            log.info("[Server] seqId: {} response {}", seqId, rpcResponse);
        } finally {
            // 3.3 移除对应的信息，便于优雅关闭
            invokeManager.removeReqAndResp(seqId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("[server channel]出现异常");
        ctx.close();
    }

    private RpcResponse handleRpcRequest(RpcRequest rpcRequest) {
        // 状态判断
        final int statusCode = statusManager.status();
        if(StatusEnum.ENABLE.code() != statusCode) {
            log.error("[Server] current status is: {} , not enable to handle request", statusCode);
            return RpcResponseFactory.shutdown();
        }

        //异步执行调用
        DefaultRpcResponse rpcResponse = new DefaultRpcResponse();
        rpcResponse.seqId(rpcRequest.seqId());
        try {
            // 获取对应的 service 实现类
            // rpcRequest=>invocationRequest
            // 执行 invoke
            Object result = DefaultServiceFactory.getInstance()
                    .invoke(rpcRequest.serviceId(),
                            rpcRequest.methodName(),
                            rpcRequest.paramTypeNames(),
                            rpcRequest.paramValues());
            rpcResponse.result(result);
        } catch (Exception e) {
            rpcResponse.error(e);
            log.error("[Server] execute meet ex for request: {}", rpcRequest, e);
        }
        return  rpcResponse;
    }
}
