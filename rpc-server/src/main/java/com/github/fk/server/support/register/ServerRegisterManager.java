package com.github.fk.server.support.register;

import com.github.fk.common.config.component.RpcAddress;
import com.github.fk.register.domain.entry.ServiceEntry;
import io.netty.channel.Channel;

public interface ServerRegisterManager {

    ServerRegisterManager port(final int port);

    void register(final ServiceEntry serviceEntry);


    ServiceEntry unRegister(final String serviceId);
    void unRegisterAll();

    /**
     * 添加注册中心的 channel
     * @param rpcAddress 地址
     * @param channel channel
     */
    void addRegisterChannel(RpcAddress rpcAddress, Channel channel);

    /**
     * 移除注册中心的 channel
     * @param rpcAddress 地址
     */
    void removeRegisterChannel(RpcAddress rpcAddress);

    /**
     * 清空注册中心的 channel
     */
    void clearRegisterChannel();


}
