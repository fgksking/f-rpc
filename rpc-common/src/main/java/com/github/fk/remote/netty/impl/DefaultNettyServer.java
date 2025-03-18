package com.github.fk.remote.netty.impl;

import com.github.fk.remote.netty.NettyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class DefaultNettyServer extends AbstractNettyServer{

    /**
     *  连接的通道
     */
    private ChannelFuture channelFuture;
    /**
     * boss 线程池
     *
     */
    private EventLoopGroup bossGroup;

    /**
     * worker 线程池
     *
     */
    private EventLoopGroup workerGroup;

    public DefaultNettyServer( int port, ChannelHandler channelHandler) {
        super( port, channelHandler);
    }
    public static NettyServer newInstance(int port,ChannelHandler channelHandler){
        return new DefaultNettyServer(port,channelHandler);
    }
    @Override
    public void run() {
        log.info("服务端启动！！ 监听窗口："+ port);
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(workerGroup, bossGroup)
                    .channel(NioServerSocketChannel.class)
                    // 打印日志
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(channelHandler)
                    // 这个参数影响的是还没有被accept 取出的连接
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 这个参数只是过一段时间内客户端没有响应，服务端会发送一个 ack 包，以判断客户端是否还活着。
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，开始接收进来的链接
            channelFuture = serverBootstrap.bind(port).syncUninterruptibly();
            log.info("[Netty Server] 启动完成，监听【" + port + "】端口");

        } catch (Exception e) {
            log.error("[Netty Server] 服务启动异常", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        try {
            log.info("服务端正在关闭");
            this.channelFuture.channel().close();
            log.info("通道关闭");
        } catch (Exception e) {
            log.error("通道关闭失败");
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


}
