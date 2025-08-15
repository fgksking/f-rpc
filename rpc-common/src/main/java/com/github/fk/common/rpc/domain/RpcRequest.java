package com.github.fk.common.rpc.domain;

import com.github.fk.common.constant.CallTypeEnum;

import java.util.List;

public interface RpcRequest extends BaseRpc{

    long createTime();

    String serviceId();
    String methodName();

    List<String> paramTypeNames();

    Object[] paramValues();

    /**
     * 回值类型
     * @return 返回值类型
     */
    Class returnType();

    long timeout();
    CallTypeEnum callType();
}
