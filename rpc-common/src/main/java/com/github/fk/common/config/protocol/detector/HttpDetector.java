package com.github.fk.common.config.protocol.detector;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;

/**
*@Author: feng
*@CreateTime: 2025-06-20
*@Description: 
*@Version: 1.0
*/
public class HttpDetector implements ProtocolDetector{

    private static final String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH"};

    @Override
    public boolean detect(ByteBuf in) {
        if(in.readableBytes() < 8){
            return false;
        }
        for (String method : HTTP_METHODS) {
            if(startWih(in,method)){
                return true;
            }
        }
        return false;
    }

    private boolean startWih(ByteBuf in, String method) {
        for(int i=0;i<method.length();i++){
            if (in.getByte(i) != (byte)method.charAt(i)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public ChannelHandler[] getHandlers() {
        return new ChannelHandler[0];
    }
}
