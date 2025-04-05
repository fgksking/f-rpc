package com.github.fk.register.domain.entry;

import java.io.Serializable;

/**
 * 一台 serviceId 可对应多个ip 地址（注册中心角度）
 */
public interface ServiceEntry extends Serializable {
    /**
     * 服务标识
     * @return 服务标识
     * @since 0.0.8
     */
    String serviceId();

    /**
     * 服务描述
     * @return 服务描述
     * @since 0.0.8
     */
    String description();

    /**
     * 机器 ip 信息
     *
     * <pre>
     *     InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
     *     String clientIP = insocket.getAddress().getHostAddress();
     * </pre>
     *
     * @return 机器 ip 信息
     * @since 0.0.8
     */
    String ip();

    /**
     * 端口信息
     * @return 端口信息
     * @since 0.0.8
     */
    int port();

    /**
     * 权重信息
     * @return 权重信息
     * @since 0.0.8
     */
    int weight();
}
