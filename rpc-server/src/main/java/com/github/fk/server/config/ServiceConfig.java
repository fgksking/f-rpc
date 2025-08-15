package com.github.fk.server.config;

/**
 *  方法配置项
 */
public interface ServiceConfig<T> {

    /**
     * 获取方法唯一标识
     * @return
     */
    String id();

    /**
     * 设置方法id
     * @param id
     * @return  this
     */
    ServiceConfig<T> id(String id);

    /**
     * 获取引用方法
     * @return  方法
     */
    T reference();

    /**
     * 设置引用方法
     * @param reference
     * @return this
     */
    ServiceConfig<T> reference(T reference);

    /**
     * 设置是否注册到注册中心
     * @param register 是否注册到配置中心
     * @since 0.0.8
     * @return this
     */
    ServiceConfig<T> register(final boolean register);

    /**
     * 返回是否注册到注册中心
     * @since 0.0.8
     * @return 是否进行注册到注册中心
     */
    boolean register();

    /**
     * 设置服务延迟暴露时间
     * @param delayInMills 延迟暴露的毫秒数
     * @since 0.1.7
     * @return this
     */
    ServiceConfig<T> delay(final long delayInMills);

    /**
     * 获取延迟暴露毫秒数
     * @return 暴露毫秒数
     * @since 0.1.7
     */
    long delay();



}
