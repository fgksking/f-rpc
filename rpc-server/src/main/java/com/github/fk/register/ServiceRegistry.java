package com.github.fk.register;

import com.github.fk.config.ServiceConfig;

/**
 * 服务注册实现类
 *（1）每个应用唯一
 * （2）协议也应唯一
 */
public interface ServiceRegistry {
    /**
     * 把方法注册来管理
     *
     * @param serviceId  方法id
     * @param ServiceImpl  接口实现类
     * @return
     */

    ServiceRegistry register(final int serviceId,final Object ServiceImpl);
    /**
     * 暴露的 rpc 服务端口信息
     * @param port 端口信息
     * @return this
     * @since 0.0.6
     */
    ServiceRegistry port(final int port);

    /**
     * 方法注册
     * @param config  方法配置类
     * @return
     */

    ServiceRegistry register(ServiceConfig config);

    /**
     *  暴露所有服务
     * (1) 本地初始化服务
     * (2) 启动服务端
     *  (3) 注册服务到注册中心
     * @return  this
     */
    ServiceRegistry expose();

}
