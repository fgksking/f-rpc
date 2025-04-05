package com.github.fk.common.support.invoke.impl;

import cn.hutool.core.util.ObjectUtil;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.rpc.domain.impl.RpcResponseFactory;
import com.github.fk.common.support.invoke.InvokeManager;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 调用服务接口
 *
 */
@Slf4j
public class DefaultInvokeManager implements InvokeManager {

    /**
     * 请求序列号 map
     * （1）这里后期如果要添加超时检测，可以添加对应的超时时间。
     * 可以把这里调整为 map
     *
     * key: seqId 唯一标识一个请求
     * value: 存入该请求最长的有效时间。用于定时删除和超时判断。
     */
    private final ConcurrentHashMap<String, Long> requestMap;
    /**
     * 响应结果
     * @since 0.0.6
     */
    private final ConcurrentHashMap<String, RpcResponse> responseMap;
    public DefaultInvokeManager() {
        requestMap = new ConcurrentHashMap<>();
        responseMap = new ConcurrentHashMap<>();

        final Runnable timeoutThread = new InvokeTimeoutCheckThread(requestMap, responseMap);
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(timeoutThread,10, 10, TimeUnit.SECONDS);
    }


    @Override
    public InvokeManager addRequest(String seqId, long timeoutMills) {
        final long expireTime =System.currentTimeMillis() +timeoutMills;
        requestMap.putIfAbsent(seqId, expireTime);
        return this;
    }

    @Override
    public InvokeManager addResponse(String seqId, RpcResponse rpcResponse) {
        //判断是否有效
        Long expireTime = this.requestMap.get(seqId);
        if(expireTime ==null){
            log.warn("[Invoke] seqId: {} has been removed, maybe timeout!", seqId);
            return this;
        }
        if(expireTime < System.currentTimeMillis()){
            log.info("信息已经超时，直接覆盖返回结果");
            rpcResponse = RpcResponseFactory.timeout();
        }
        // 这里放入之前，可以添加判断。
        // 如果 seqId 必须处理请求集合中，才允许放入。或者直接忽略丢弃。
        // 通知所有等待方
        responseMap.putIfAbsent(seqId, rpcResponse);
        log.info("[Invoke] 获取结果信息，seqId: {}, rpcResponse: {}", seqId, rpcResponse);
        log.info("[Invoke] seqId:{} 信息已经放入，通知所有等待方", seqId);
        synchronized (this) {
            this.notifyAll();
        }
        return this;
    }


    @Override
    public RpcResponse getResponse(String seqId) {
        try {
            RpcResponse rpcResponse = this.responseMap.get(seqId);
            if(ObjectUtil.isNotNull(rpcResponse)) {
                log.info("[Invoke] seq {} 对应结果已经获取: {}", seqId, rpcResponse);
            } else {
                // 进入等待
                while (rpcResponse == null) {
                    log.info("[Invoke] seq {} 对应结果为空，进入等待", seqId);
                    // 同步等待锁
                    synchronized (this) {
                        this.wait();
                    }
                    // addResponse 方法被执行
                    rpcResponse = this.responseMap.get(seqId);
                    log.info("[Invoke] seq {} 对应结果已经获取: {}", seqId, rpcResponse);
                }
            }

            // 移除 request
            this.requestMap.remove(seqId);
            return rpcResponse;
        } catch (InterruptedException e) {
            log.error("[Invoke] get response meet InterruptedException ex", e);
            return RpcResponseFactory.interrupted();
        }
    }

    @Override
    public boolean remainsRequest() {
        return this.requestMap.size() > 0;
    }

    @Override
    public InvokeManager removeReqAndResp(String seqId) {
        this.requestMap.remove(seqId);
        this.responseMap.remove(seqId);
        return this;
    }
}
