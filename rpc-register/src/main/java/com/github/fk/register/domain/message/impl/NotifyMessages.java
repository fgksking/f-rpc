package com.github.fk.register.domain.message.impl;

import cn.hutool.core.lang.UUID;
import com.github.fk.common.utils.ArgUtil;
import com.github.fk.register.domain.message.NotifyMessage;
import com.github.fk.register.domain.message.NotifyMessageHeader;

public class NotifyMessages {
    private NotifyMessages(){}

    /**
     * 初始化消息信息
     * @param type 类型
     * @param body 消息体
     * @return 注册消息
     * @since 0.0.8
     */
    public static NotifyMessage of(final String type,
                                   final Object body) {
        String seqId = UUID.randomUUID().toString();
        return of(type, seqId, body);
    }

    /**
     * 初始化消息信息
     * @param type 类型
     * @param seqId 消息标识
     * @param body 消息体
     * @return 注册消息
     * @since 0.0.8
     */
    public static NotifyMessage of(final String type,
                                   final String seqId,
                                   final Object body) {
        DefaultNotifyMessage registerMessage = new DefaultNotifyMessage();
        DefaultNotifyMessageHeader messageHeader = new DefaultNotifyMessageHeader();
        messageHeader.type(type);

        registerMessage.seqId(seqId);
        registerMessage.header(messageHeader);
        registerMessage.body(body);
        return registerMessage;
    }

    /**
     * 获取消息的类型
     * @param notifyMessage 注册消息
     * @return 消息类型
     * @since 0.0.8
     */
    public static String type(final NotifyMessage notifyMessage) {
        NotifyMessageHeader header = header(notifyMessage);
        return header.type();
    }

    /**
     * 获取消息头
     * @param notifyMessage 消息
     * @return 消息头
     * @since 0.0.8
     */
    private static NotifyMessageHeader header(final NotifyMessage notifyMessage) {
        ArgUtil.notNull(notifyMessage, "registerMessage");
        NotifyMessageHeader messageHeader = notifyMessage.header();
        ArgUtil.notNull(messageHeader, "messageHeader");
        return messageHeader;
    }

}
