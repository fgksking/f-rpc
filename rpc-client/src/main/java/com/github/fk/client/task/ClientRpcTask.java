package com.github.fk.client.task;

import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.threadpoll.task.RpcTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientRpcTask implements RpcTask {

    private final RpcResponse rpcResponse;
    private final InvokeManager invokeManager;
    public ClientRpcTask(RpcResponse rpcResponse, InvokeManager invokeManager) {
        this.rpcResponse = rpcResponse;
        this.invokeManager = invokeManager;
    }

    @Override
    public void run() {
        final String seqId = rpcResponse.seqId();
        try {
            beforeExecute();
            log.info("[Client Task] 开始处理响应, seqId: {}", seqId);
            // 将响应添加到调用管理器
            invokeManager.addResponse(seqId, rpcResponse);
            log.info("[Client Task] 响应处理完成, seqId: {}, response: {}", seqId, rpcResponse);

            afterExecute();

        } catch (Exception e) {
            log.error("[Client Task] 处理响应异常, seqId: {}", seqId, e);
            onException(e);
        }
    }

    @Override
    public String getTaskName() {
        return "ClientRpcTask-" + rpcResponse.seqId();
    }

    @Override
    public int getPriority() {
        return 5; // 中等优先级
    }

    @Override
    public void beforeExecute() {
        log.debug("[Client Task] 任务执行前处理, seqId: {}", rpcResponse.seqId());
    }

    @Override
    public void afterExecute() {
        log.debug("[Client Task] 任务执行后处理, seqId: {}", rpcResponse.seqId());
    }

    @Override
    public void onException(Throwable throwable) {
        log.error("[Client Task] 任务执行异常, seqId: {}", rpcResponse.seqId(), throwable);
        // 可以在这里添加异常处理逻辑
    }
}
