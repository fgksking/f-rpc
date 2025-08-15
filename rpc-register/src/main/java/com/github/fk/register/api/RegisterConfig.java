package com.github.fk.register.api;

public interface RegisterConfig {
    /**
     * 服务端口号
     * @param port 端口号
     * @return this
     */
    RegisterConfig port(final int port);

    /**
     * 启动服务
     * @return this
     */
    RegisterConfig start();
}
