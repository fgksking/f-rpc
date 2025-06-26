package com.github.fk.common.netty.handler;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;

/**
*@Author: feng
*@CreateTime:
*@Description: 
*@Version: 1.0
*/
public class HttpDispatchHandler extends SimpleChannelInboundHandler<HttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest httpRequest) throws Exception {
        boolean keepAlive = HttpUtil.isKeepAlive(httpRequest) && httpRequest.protocolVersion().isKeepAliveDefault();
        FullHttpResponse response;
        //解析消息，提取方法信息

        //反射调用方法（服务端）
    }
}
