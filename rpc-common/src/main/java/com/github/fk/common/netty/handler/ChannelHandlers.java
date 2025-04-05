package com.github.fk.common.netty.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.github.fk.common.config.component.RpcAddress;
import com.github.fk.common.netty.impl.DefaultNettyClient;
import com.github.fk.common.rpc.domain.RpcChannelFuture;
import com.github.fk.common.rpc.domain.impl.DefaultRpcChannelFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
@Slf4j
public class ChannelHandlers {
    private ChannelHandlers(){}

    /**
     * 包含默认 object 编码解码的 handler
     * @param channelHandlers 用户自定义 handler
     * @return channel handler
     * @since 0.0.8
     */
    public static ChannelHandler objectCodecHandler(final ChannelHandler ... channelHandlers) {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        // 解码 bytes=>resp
                        .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)))
                        // request=>bytes
                        .addLast(new ObjectEncoder())
                        .addLast(channelHandlers);
            }
        };
    }

    /**
     *   获取每个注册中心处理过的channel future
     * @param rpcAddressList
     * @param handlerFactory
     * @return
     */
    public static List<RpcChannelFuture> channelFutureList(final List<RpcAddress> rpcAddressList, final ChannelHandlerFactory handlerFactory){
        log.info("初始化与注册中心的连接");
        List<RpcChannelFuture> resultList = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(rpcAddressList)) {
            for(RpcAddress rpcAddress : rpcAddressList) {
                final ChannelHandler channelHandler = handlerFactory.handler();
                log.info("注册中心地址 ={}",rpcAddress);
                // 循环中每次都需要一个新的 handler
                DefaultRpcChannelFuture future = DefaultRpcChannelFuture.newInstance();
                DefaultNettyClient nettyClient = DefaultNettyClient.newInstance(rpcAddress.address(), rpcAddress.port(), channelHandler);
                ChannelFuture channelFuture = nettyClient.call();

                future.channelFuture(channelFuture).address(rpcAddress)
                        .weight(rpcAddress.weight()).destroyable(nettyClient);
                resultList.add(future);
            }
        }

        return resultList;

    }
    public static RpcChannelFuture channelFuture(final RpcAddress rpcAddress, final ChannelHandlerFactory handlerFactory) {
        final ChannelHandler channelHandler = handlerFactory.handler();

        // 循环中每次都需要一个新的 handler
        DefaultRpcChannelFuture future = DefaultRpcChannelFuture.newInstance();
        DefaultNettyClient nettyClient = DefaultNettyClient.newInstance(rpcAddress.address(), rpcAddress.port(), channelHandler);
        ChannelFuture channelFuture = nettyClient.call();

        future.channelFuture(channelFuture)
                .address(rpcAddress)
                .weight(rpcAddress.weight())
                .destroyable(nettyClient);

        return future;
    }
}
