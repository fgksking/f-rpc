package com.github.fk.remote.netty.impl;

import com.github.fk.remote.netty.NettyServer;
import io.netty.channel.ChannelHandler;

/**
 * 抽象出共有的属性
 */
public abstract class AbstractNettyServer implements NettyServer {

    protected String ip;
    protected int port;

    protected ChannelHandler channelHandler;

    public AbstractNettyServer(String ip, int port,ChannelHandler channelHandler) {
        this.ip = ip;
        this.port = port;
        this.channelHandler = channelHandler;
    }

}
