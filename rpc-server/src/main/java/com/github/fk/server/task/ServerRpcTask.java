package com.github.fk.server.task;

import com.github.fk.common.constant.CallTypeEnum;
import com.github.fk.common.rpc.domain.RpcRequest;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.rpc.domain.impl.DefaultRpcResponse;
import com.github.fk.common.rpc.domain.impl.RpcResponseFactory;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.status.StatusManager;
import com.github.fk.common.support.status.enums.StatusEnum;
import com.github.fk.common.threadpoll.task.RpcTask;
import com.github.fk.server.service.impl.DefaultServiceFactory;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerRpcTask implements RpcTask {
    private final ChannelHandlerContext ctx;
    private final RpcRequest rpcRequest;
    private final InvokeManager invokeManager;
    private final StatusManager statusManager;

    public ServerRpcTask(ChannelHandlerContext ctx, RpcRequest rpcRequest,
                         InvokeManager invokeManager, StatusManager statusManager) {
        this.ctx = ctx;
        this.rpcRequest = rpcRequest;
        this.invokeManager = invokeManager;
        this.statusManager = statusManager;
    }

    @Override
    public void run() {
        final String seqId = rpcRequest.seqId();
        final String channelId = ctx.channel().id().asLongText();

        try {
            beforeExecute();

            log.info("[Server Task] 开始处理请求, seqId: {}, channelId: {}", seqId, channelId);

            // 1. 设置请求信息和超时时间
            invokeManager.addRequest(seqId, rpcRequest.timeout());

            // 2. 处理RPC请求
            RpcResponse rpcResponse = handleRpcRequest(rpcRequest);

            // 3. 回写到客户端
            final CallTypeEnum callType = rpcRequest.callType();
            if (CallTypeEnum.SYNC.equals(callType)) {
                log.info("[Server Task] SYNC调用, seqId: {}", seqId);
                if (ctx.channel().isActive()) {
                    ctx.writeAndFlush(rpcResponse).addListener(future -> {
                        if (!future.isSuccess()) {
                            log.error("[Server Task] 发送响应失败，seqId: {}", seqId, future.cause());
                        } else {
                            log.info("[Server Task] 发送响应成功，seqId: {}", seqId);
                        }
                    });
                } else {
                    log.warn("[Server Task] Channel已关闭，无法发送响应，seqId: {}", seqId);
                }
            } else {
                log.info("[Server Task] seqId: {} callType: {} ignore write back.", seqId, callType);
            }

            log.info("[Server Task] seqId: {} response {}", seqId, rpcResponse);

            afterExecute();

        } catch (Exception e) {
            log.error("[Server Task] 处理请求异常, seqId: {}", seqId, e);
            onException(e);
        } finally {
            // 移除对应的信息，便于优雅关闭
            invokeManager.removeReqAndResp(seqId);
        }
    }

    @Override
    public String getTaskName() {
        return "ServerRpcTask-" + rpcRequest.seqId();
    }

    @Override
    public int getPriority() {
        return 5; // 中等优先级
    }

    @Override
    public void beforeExecute() {
        log.debug("[Server Task] 任务执行前处理, seqId: {}", rpcRequest.seqId());
    }

    @Override
    public void afterExecute() {
        log.debug("[Server Task] 任务执行后处理, seqId: {}", rpcRequest.seqId());
    }

    @Override
    public void onException(Throwable throwable) {
        log.error("[Server Task] 任务执行异常, seqId: {}", rpcRequest.seqId(), throwable);
        // 可以在这里添加异常处理逻辑，比如发送错误响应
        if (ctx.channel().isActive()) {
            RpcResponse errorResponse = RpcResponseFactory.error(throwable);
            errorResponse.seqId(rpcRequest.seqId());
            ctx.writeAndFlush(errorResponse);
        }
    }

    /**
     * 处理RPC请求
     */
    private RpcResponse handleRpcRequest(RpcRequest rpcRequest) {
        // 状态判断
        final int statusCode = statusManager.status();
        if (StatusEnum.ENABLE.code() != statusCode) {
            log.error("[Server Task] current status is: {} , not enable to handle request", statusCode);
            return RpcResponseFactory.shutdown();
        }

        // 异步执行调用
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
            log.error("[Server Task] execute meet ex for request: {}", rpcRequest, e);
        }
        return rpcResponse;
    }
}
