package com.github.fk.common.config.protocol.detector;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;

public interface ProtocolDetector {

    boolean detect(ByteBuf in);
    ChannelHandler[] getHandlers();
}
