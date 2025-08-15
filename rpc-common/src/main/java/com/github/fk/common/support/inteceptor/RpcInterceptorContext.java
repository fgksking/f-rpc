package com.github.fk.common.support.inteceptor;

public interface RpcInterceptorContext {
    /**
     * 调用唯一标识
     * @return 唯一标识
     */
    String traceId();

    /**
     * 开始时间
     * @return 开始时间
     */
    long startTime();

    /**
     * 结束时间
     * @return 结束时间
     */
    long endTime();
    RpcInterceptorContext put(final String key, final Object value);

    /**
     * 获取对应的值
     * @param key key
     * @return this
     */
    Object get(final String key);

    /**
     * 获取对应的值
     * @param key key
     * @param <T> 泛型
     * @param tClass 类型
     * @return this
     */
    <T> T get(final String key, final Class<T> tClass);

    /**
     * 获取请求参数
     * @return 获取请求参数
     */
    Object[] params();

    /**
     * 请求结果
     * @return 请求结果
     */
    Object result();
}
