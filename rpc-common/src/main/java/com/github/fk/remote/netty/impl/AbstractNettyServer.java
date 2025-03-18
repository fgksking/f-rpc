package com.github.fk.remote.netty.impl;

import com.github.fk.remote.netty.NettyServer;
import io.netty.channel.ChannelHandler;

import java.util.concurrent.Executors;

/**
 * 抽象出共有的属性
 */
public abstract class AbstractNettyServer implements NettyServer {

    protected int port;

    protected ChannelHandler channelHandler;

    public AbstractNettyServer( int port,ChannelHandler channelHandler) {
        this.port = port;
        this.channelHandler = channelHandler;
    }

    @Override
    public void asyn() {
        Executors.newSingleThreadExecutor().submit(this);
    }

    //在run接口初始化，通过single线程池执行一次run方法
    @Override
    public void init() {

    }
}
