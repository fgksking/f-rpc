package com.github.fk.register.domain.message.impl;

import com.github.fk.register.domain.message.NotifyMessage;
import com.github.fk.register.domain.message.NotifyMessageHeader;

public class DefaultNotifyMessage implements NotifyMessage {
    /**
     * 唯一序列号标识
     * @since 0.0.8
     */
    private String seqId;

    /**
     * 头信息
     * @since 0.0.8
     */
    private NotifyMessageHeader header;

    /**
     * 消息信息体
     * @since 0.0.8
     */
    private Object body;

    @Override
    public String seqId() {
        return seqId;
    }

    @Override
    public DefaultNotifyMessage seqId(String seqId) {
        this.seqId = seqId;
        return this;
    }

    @Override
    public NotifyMessageHeader header() {
        return header;
    }

    public DefaultNotifyMessage header(NotifyMessageHeader header) {
        this.header = header;
        return this;
    }

    @Override
    public Object body() {
        return body;
    }

    public DefaultNotifyMessage body(Object body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultRegisterMessage{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }
}
