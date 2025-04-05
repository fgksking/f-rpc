package com.github.fk.register.domain.message;

import com.github.fk.common.rpc.domain.BaseRpc;

public interface NotifyMessage extends BaseRpc {
    /**
     * 头信息
     * @return 头信息
     * @since 0.0.8
     */
    NotifyMessageHeader header();

    /**
     * 消息信息体
     * @return 消息信息体
     * @since 0.0.8
     */
    Object body();
}
