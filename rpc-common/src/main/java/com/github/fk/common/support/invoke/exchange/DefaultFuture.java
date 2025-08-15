package com.github.fk.common.support.invoke.exchange;

import com.github.fk.common.config.application.impl.DefaultApplicationConfig;
import com.github.fk.common.rpc.domain.RpcRequest;

import java.util.concurrent.*;


//写入消息的具体逻辑如何实现
//全面替换DefaultInvokeManager
public class DefaultFuture extends CompletableFuture<Object> {

    //替换成DefaultFuture，获取结果从这获取
    private static final ConcurrentHashMap<String,DefaultFuture> FUTURE = new ConcurrentHashMap<>();
    //超时的时间
    private final int timeout;
    private final String requestId;
    private final ExecutorService executor;
    private final long start = System.currentTimeMillis();

    private volatile long sent;

    private final RpcRequest request;

    public DefaultFuture(int timeout, RpcRequest request, ExecutorService executor) {
        this.timeout = timeout;
        this.requestId = request.seqId();
        this.executor = executor;
        this.request = request;
        FUTURE.put(requestId,this);
    }

    public static DefaultFuture newDefaultFuture(int timeout, RpcRequest request,ExecutorService executor){
        final DefaultFuture future = new DefaultFuture(timeout, request, executor);
        timeoutCheck(future);
        return future;
    }

    private static void timeoutCheck(DefaultFuture future) {


    }

}
