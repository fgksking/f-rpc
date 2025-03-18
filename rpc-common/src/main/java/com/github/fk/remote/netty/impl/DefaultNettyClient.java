package com.github.fk.remote.netty.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultNettyClient extends AbstractNettyClient<ChannelFuture>{

    private EventLoopGroup workerGroup;

    private ChannelFuture channelFuture;


    public DefaultNettyClient(int port, String ip, ChannelHandler channelHandler) {
        super(port, ip, channelHandler);
        workerGroup = new NioEventLoopGroup();
    }

    @Override
    public ChannelFuture call() throws Exception {
        // 启动服务端
        log.info("[Netty Client] 开始启动客户端");

       // workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            channelFuture = bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(channelHandler)
                    .connect(ip, port)
                    .syncUninterruptibly();
            log.info("[Netty Client] 启动客户端完成，监听地址 {}:{}", ip, port);
        } catch (Exception e) {
            log.error("[Netty Client] 端启动遇到异常", e);
            throw new RuntimeException(e);
        }
        // 不要关闭线程池！！！
        return channelFuture;
    }

    @Override
    public void destroy() {
        try {
            channelFuture.channel().close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
        }

    }

    @Override
    public void init() {

    }
    public static DefaultNettyClient newInstance(String ip,int port,ChannelHandler channelHandler){
        return new DefaultNettyClient(port,ip,channelHandler);
    }

}
