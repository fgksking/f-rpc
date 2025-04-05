package com.github.fk.client.config;

import com.github.fk.client.support.filter.RpcFilter;
import com.github.fk.common.support.inteceptor.RpcInterceptor;
import com.github.fk.common.support.loadBalance.ILoadBalance;

public interface ReferenceConfig<T>{

    T reference();

    ReferenceConfig<T> serviceId(final String serviceId);

    ReferenceConfig<T> serviceInterface(final Class<T> serviceInterface);

    /**
     * 设置服务地址信息
     * （1）单个写法：ip:port:weight
     * （2）集群写法：ip1:port1:weight1,ip2:port2:weight2
     *
     * 其中 weight 权重可以不写，默认为1.
     *
     * @param addresses 地址列表信息
     * @return this
     * @since 0.0.6
     */
    ReferenceConfig<T> addresses(final String addresses);

    /**
     * 设置超时时间
     * @param timeoutInMills 超时时间
     * @since 0.0.7
     * @return this
     */
    ReferenceConfig<T> timeout(final long timeoutInMills);
    /**
     * 是否订阅发现服务
     * 注意：如果指定 {@link #addresses(String)} 信息，这个属性将会失效。
     * @param subscribe 是否订阅模式
     * @since 0.0.8
     * @return this
     */
    ReferenceConfig<T> subscribe(final boolean subscribe);

    /**
     * 注册中心地址
     * （1）正常使用中，这个属性是可以固定写死，对使用者不可见的。
     * @param addresses 注册中心地址
     * @since 0.0.8
     * @return this
     */
    ReferenceConfig<T> registerCenter(final String addresses);


    /**
     * 设置是否进行泛化处理
     * @param generic 是否进行泛化处理
     * @since 0.1.2
     * @return this
     */
    ReferenceConfig<T> generic(final boolean generic);
    ReferenceConfig<T> rpcInterceptor(final RpcInterceptor rpcInterceptor);

    /**
     * 是否检测状态
     * @param check 是否检测
     * @return this
     * @since 0.1.5
     */
    ReferenceConfig<T> check(final boolean check);

    /**
     * 设置过滤器
     * @param rpcFilter 过滤器
     * @return this
     * @since 0.2.0
     */
    ReferenceConfig<T> rpcFilter(final RpcFilter rpcFilter);

    /**
     * 负载均衡
     * @param loadBalance 负载均衡
     * @return this
     * @since 0.2.0
     */
    ReferenceConfig<T> loadBalance(final ILoadBalance loadBalance);
}
