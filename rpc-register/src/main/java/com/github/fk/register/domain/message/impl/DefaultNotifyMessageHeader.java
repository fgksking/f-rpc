package com.github.fk.register.domain.message.impl;

import com.github.fk.register.domain.message.NotifyMessageHeader;

public class DefaultNotifyMessageHeader implements NotifyMessageHeader {
    private String type;

    @Override
    public String type() {
        return type;
    }

    public DefaultNotifyMessageHeader type(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultRegisterMessageHeader{" +
                "type=" + type +
                '}';
    }

}
