package com.github.fk.common.rpc.domain;

import com.github.fk.common.api.Destroyable;
import com.github.fk.common.config.component.RpcAddress;
import com.github.fk.common.support.loadBalance.IServer;
import io.netty.channel.ChannelFuture;

public interface RpcChannelFuture extends IServer {
    int weight();


    String url();
    /**
     * channel future 信息
     * @return ChannelFuture
     * @since 0.0.9
     */
    ChannelFuture channelFuture();

    /**
     * 对应的地址信息
     * @return 地址信息
     * @since 0.0.9
     */
    RpcAddress address();

    /**
     * 可销毁的对象
     * @return 可销毁的信息
     * @since 0.1.3
     */
    Destroyable destroyable();
}
