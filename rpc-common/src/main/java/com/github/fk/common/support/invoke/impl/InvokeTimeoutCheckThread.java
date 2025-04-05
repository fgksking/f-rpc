package com.github.fk.common.support.invoke.impl;

import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.rpc.domain.impl.RpcResponseFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InvokeTimeoutCheckThread implements Runnable{
    /**
     * 请求信息
     *
     */
    private final ConcurrentHashMap<String, Long> requestMap;

    /**
     * 请求信息
     *
     */
    private final ConcurrentHashMap<String, RpcResponse> responseMap;

    /**
     * 新建
     *
     * @param requestMap  请求 Map
     * @param responseMap 结果 map
     * @since 0.0.7
     */
    public InvokeTimeoutCheckThread(ConcurrentHashMap<String, Long> requestMap,
                                    ConcurrentHashMap<String, RpcResponse> responseMap) {
        if(requestMap ==null){
            throw new IllegalArgumentException("requestMap不能为空");
        }
        this.requestMap = requestMap;
        this.responseMap = responseMap;
    }

    @Override
    public void run() {
        for (Map.Entry<String, Long> entry : requestMap.entrySet()) {
            Long value = entry.getValue();
            long expireTime = entry.getValue();
            System.out.println("currentTime");
            long currentTime = System.currentTimeMillis();
            if (currentTime > expireTime) {
                final String key = entry.getKey();
                // 结果设置为超时，从请求 map 中移除, 同时设置 response 为超时
                responseMap.putIfAbsent(key, RpcResponseFactory.timeout());
                requestMap.remove(key);
            }
        }
    }
}
