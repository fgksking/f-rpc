package com.github.fk.register.domain.message.body;

import java.io.Serializable;

public class RegisterCenterRemoveNotifyBody implements Serializable {
    private String ip;

    /**
     * 端口信息
     * @since 0.1.8
     */
    private int port;

    public String ip() {
        return ip;
    }

    public RegisterCenterRemoveNotifyBody ip(String ip) {
        this.ip = ip;
        return this;
    }

    public int port() {
        return port;
    }

    public RegisterCenterRemoveNotifyBody port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public String toString() {
        return "RegisterCenterAddNotifyBody{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
