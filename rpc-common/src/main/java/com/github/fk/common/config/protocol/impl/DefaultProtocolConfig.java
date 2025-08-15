package com.github.fk.common.config.protocol.impl;

import com.github.fk.common.config.protocol.ProtocolConfig;

public class DefaultProtocolConfig implements ProtocolConfig {
    /**
     * 名称
     * RPC
     * HTTP
     * HTTPS
     * @since 0.0.6
     */
    private String name;



    @Override
    public String name() {
        return name;
    }

    public DefaultProtocolConfig name(String name) {
        this.name = name;
        return this;
    }

}
