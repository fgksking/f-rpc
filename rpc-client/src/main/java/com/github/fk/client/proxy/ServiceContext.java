package com.github.fk.client.proxy;

import com.github.fk.client.support.fail.enums.FailTypeEnum;
import com.github.fk.client.support.filter.RpcFilter;
import com.github.fk.client.support.register.ClientRegisterManager;
import com.github.fk.common.constant.CallTypeEnum;
import com.github.fk.common.support.inteceptor.RpcInterceptor;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.loadBalance.ILoadBalance;
import com.github.fk.common.support.status.StatusManager;

/**
 * 反射上下文 ，获取反射所需的各种信息
 * @param <T>
 */
public interface ServiceContext<T> {
    String serviceId();

    Class<T> serviceInterface();

    InvokeManager invokeManager();
    /**
     * 超时时间
     * 单位：mills
     * @return 超时时间
     * @since 0.0.7
     */
    long timeout();

    /**
     * 调用方式
     * @return 枚举值
     * @since 0.1.0
     */
    CallTypeEnum callType();

    /**
     * 失败策略
     * @return 失败策略枚举
     * @since 0.1.1
     */
    FailTypeEnum failType();

    /**
     * 是否进行泛化调用
     * @return 是否
     * @since 0.1.2
     */
    boolean generic();

    /**
     * 状态管理类
     * @return 状态管理类
     */
    StatusManager statusManager();

    /**
     * 拦截器
     * @return 拦截器
     */
    RpcInterceptor interceptor();

    /**
     * 客户端注册中心管理类
     * @return 结果
     * @since 0.1.8
     */
    ClientRegisterManager clientRegisterManager();

    /**
     * rpc 过滤器
     * @since 0.2.0
     * @return 过滤器
     */
    RpcFilter rpcFilter();

    /**
     * 负载均衡策略
     * @return 策略
     * @since 0.2.0
     */
    ILoadBalance loadBalance();
}
