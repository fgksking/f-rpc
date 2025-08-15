package com.github.fk.common.netty.handler;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerFactory {

    ChannelHandler handler();
}
