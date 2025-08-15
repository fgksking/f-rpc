package com.github.fk.register.simple.client;

import com.github.fk.register.domain.entry.ServiceEntry;
import io.netty.channel.Channel;


import java.util.Collection;

/**
 * 客户端注册服务类
 */
public interface RegisterClientService {
    void subscribe(final ServiceEntry serviceEntry,
                   final Channel clientChannel);


    void unSubscribe(final ServiceEntry serviceEntry,
                     final Channel clientChannel);

    void registerNotify(final String serviceId, final ServiceEntry serviceEntry);


    void unRegisterNotify(final String serviceId, final ServiceEntry serviceEntry);

    Collection<Channel> channels();
}
