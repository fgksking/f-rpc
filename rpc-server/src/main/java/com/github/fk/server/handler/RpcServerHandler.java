package com.github.fk.server.handler;


import com.github.fk.common.constant.CallTypeEnum;
import com.github.fk.common.rpc.domain.RpcRequest;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.rpc.domain.impl.DefaultRpcResponse;
import com.github.fk.common.rpc.domain.impl.RpcResponseFactory;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.status.StatusManager;
import com.github.fk.common.support.status.enums.StatusEnum;
import com.github.fk.common.threadpoll.manager.ExecutorRepository;
import com.github.fk.common.threadpoll.manager.DefaultExecutorRepository;
import com.github.fk.common.support.status.enums.Endpoint;
import com.github.fk.server.service.impl.DefaultServiceFactory;
import com.github.fk.server.task.ServerRpcTask;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

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

    /**
     * 线程池管理器
     */
    private final ExecutorRepository executorRepository;

    public RpcServerHandler(InvokeManager invokeManager, StatusManager statusManager, ExecutorRepository executorRepository) {
        this.invokeManager = invokeManager;
        this.statusManager = statusManager;
        this.executorRepository = executorRepository;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String id = ctx.channel().id().asLongText();
        log.info("[Server] channel {} connected " + id);
    }

    /**
     *
     * @param ctx           the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *                      belongs to
     * @param msg           the message to handle
     * @throws Exception
     * 这里直接封装成任务，丢给线程池去处理，因为是接收端，交给响应线程池
     * 封装DefaultFuture 实现异步执行
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        final String id = ctx.channel().id().asLongText();
        log.info("[Server] channel read start: {}", id);
        if(msg instanceof RpcRequest){
            myProtocolHandle(ctx,msg);
        }
        //这里暂且增加新的协议，如Http2，需要重构整个代码块

    }
    public void myProtocolHandle(ChannelHandlerContext ctx, Object msg){
        // 1. 接受客户端请求
        RpcRequest rpcRequest = (RpcRequest)msg;
        final String seqId = rpcRequest.seqId();

        try {
            log.info("[Server] receive seqId: {} request: {}", seqId, rpcRequest);

            // 2. 创建任务并提交到线程池
            ServerRpcTask serverRpcTask = new ServerRpcTask(ctx, rpcRequest, invokeManager, statusManager);
            ExecutorService serverExecutor = executorRepository.getExecutor(Endpoint.SERVER);

            if (serverExecutor != null) {
                serverExecutor.submit(serverRpcTask);
                log.info("[Server] 任务已提交到线程池, seqId: {}, taskName: {}", seqId, serverRpcTask.getTaskName());
            } else {
                log.error("[Server] 服务端线程池未初始化，无法处理请求, seqId: {}", seqId);
                // 降级处理：直接在当前线程处理
                handleRpcRequestDirectly(ctx, rpcRequest);
            }

        } catch (Exception e) {
            log.error("[Server] 处理请求异常, seqId: {}", seqId, e);
            // 发送错误响应
            if (ctx.channel().isActive()) {
                DefaultRpcResponse errorResponse = new DefaultRpcResponse();
                errorResponse.seqId(seqId);
                errorResponse.error(e);
                ctx.writeAndFlush(errorResponse);
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("[server channel]出现异常");
        ctx.close();
    }

    /**
     * 降级处理：直接在当前线程处理请求
     */
    private void handleRpcRequestDirectly(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
        final String seqId = rpcRequest.seqId();

        try {
            // 设置请求信息和超时时间
            invokeManager.addRequest(seqId, rpcRequest.timeout());

            // 处理RPC请求
            RpcResponse rpcResponse = this.handleRpcRequest(rpcRequest);

            // 回写到客户端
            final CallTypeEnum callType = rpcRequest.callType();
            if(CallTypeEnum.SYNC.equals(callType)) {
                log.info("SYNC 调用");
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
            // 移除对应的信息，便于优雅关闭
            invokeManager.removeReqAndResp(seqId);
        }
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