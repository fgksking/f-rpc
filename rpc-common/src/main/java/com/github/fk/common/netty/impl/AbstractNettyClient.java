package com.github.fk.common.netty.impl;

import com.github.fk.common.netty.NettyClient;
import io.netty.channel.ChannelHandler;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public abstract class AbstractNettyClient<V> implements NettyClient<V> {
    protected int port;

    protected String ip;

    protected ChannelHandler channelHandler;
    //抽象类覆写了接口，子类就不需要强制覆写

    @Override
    public void destroy() {

    }

    @Override
    public void init() {

    }
}
