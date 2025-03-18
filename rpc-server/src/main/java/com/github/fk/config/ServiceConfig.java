package com.github.fk.config;

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
    T reference(String id);

    /**
     * 设置引用方法
     * @param reference
     * @return this
     */
    ServiceConfig<T> reference(T reference);





}
