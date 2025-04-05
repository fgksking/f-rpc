package com.github.fk.common.rpc.domain.impl;

import cn.hutool.core.util.ObjectUtil;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.utils.PrimitiveUtil;

public class RpcResponses {
    private RpcResponses(){}

    /**
     * 设置结果
     * @param object 结果
     * @param tClass 结果类型
     * @return 结果
     * @since 0.1.0
     */
    public static RpcResponse result(final Object object,
                                     final Class tClass) {
        DefaultRpcResponse rpcResponse = new DefaultRpcResponse();
        if(ObjectUtil.isNotNull(object)) {
            rpcResponse.result(object);
        } else {
            // 处理基本类型的默认值，避免 NPE
            Object defaultVal = PrimitiveUtil.getDefaultValue(tClass);
            rpcResponse.result(defaultVal);
        }
        return rpcResponse;
    }

    /**
     * 获取结果
     * @param rpcResponse 响应
     * @param returnType 返回值类型
     * @return 结果
     * 如果有异常，则直接抛出异常信息。
     */
    public static Object getResult(final RpcResponse rpcResponse,
                                   final Class returnType) {
        if(ObjectUtil.isNull(rpcResponse)) {
            // 根据返回类型处理
            return PrimitiveUtil.getDefaultValue(returnType);
        }

        // 处理异常信息
        Throwable throwable = rpcResponse.error();
        if(ObjectUtil.isNotNull(throwable)) {
            throw new RuntimeException(throwable);
        }

        // 处理结果信息
        Object result = rpcResponse.result();
        if(ObjectUtil.isNotNull(result)) {
            return result;
        }
        return PrimitiveUtil.getDefaultValue(returnType);
    }

    /**
     * 获取结果
     * @param rpcResponse 响应
     * @return 结果
     * 如果有异常，则直接抛出异常信息。
     * @since 0.0.8
     */
    public static Object getResult(final RpcResponse rpcResponse) {
        return getResult(rpcResponse, Object.class);
    }


}
