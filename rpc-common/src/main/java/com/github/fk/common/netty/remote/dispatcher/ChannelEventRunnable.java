package com.github.fk.common.netty.remote.dispatcher;

import com.github.fk.common.support.status.enums.Endpoint;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelEventRunnable implements Runnable{
    private final Object message;


    private final ChannelHandler channelHandler;

    private final Endpoint endpoint;

    public ChannelEventRunnable(Object message, ChannelHandler channelHandler, Endpoint endpoint) {
        this.message = message;
        this.channelHandler = channelHandler;
        this.endpoint = endpoint;
    }

    @Override
    public void run() {

    }
}
