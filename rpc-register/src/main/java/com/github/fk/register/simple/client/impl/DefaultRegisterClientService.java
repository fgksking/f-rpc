package com.github.fk.register.simple.client.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.fk.common.utils.ArgUtil;
import com.github.fk.register.domain.entry.ServiceEntry;
import com.github.fk.register.domain.message.NotifyMessage;
import com.github.fk.register.domain.message.impl.NotifyMessages;
import com.github.fk.register.simple.client.RegisterClientService;
import com.github.fk.register.simple.constant.MessageTypeConst;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class DefaultRegisterClientService implements RegisterClientService {
    /**
     * 服务信息-客户端列表 map
     * key : serviceId
     * value 对应的客户端列表信息
     * 客户端使用定期拉取的方式：
     *（1）传入 host 信息，返回对应的 service 列表。
     *（2）根据 service 列表，变化时注册中心服务端定期推送给客户端。
     *
     * 只是在第一次采用拉取的方式，后面全部采用推送的方式。
     *  1）只有变更的时候，才会进行推送，保证实时性。
     *  （2）客户端启动时拉取，作为保底措施。避免客户端不在线等情况。
     */
    private final Map<String, Set<Channel>> serviceClientChannelMap;

    public DefaultRegisterClientService() {
        this.serviceClientChannelMap = new ConcurrentHashMap<>();
    }
    /**
     * 监听服务信息
     * （1）监听之后，如果有任何相关的机器信息发生变化，则进行推送。
     * （2）内置的信息，需要传送 ip 信息到注册中心。
     */
    @Override
    public void subscribe(ServiceEntry clientEntry, Channel clientChannel) {
        paramCheck(clientEntry);

        final String serviceId = clientEntry.serviceId();
        Set<Channel> channelSet = serviceClientChannelMap.get(serviceId);
        if (channelSet ==null) {
            channelSet = new HashSet<>();
        }
        channelSet.add(clientChannel);
        serviceClientChannelMap.put(serviceId, channelSet);
    }

    private void paramCheck(ServiceEntry serviceEntry) {
        ArgUtil.notNull(serviceEntry, "serverEntry");
        ArgUtil.notEmpty(serviceEntry.serviceId(), "serverEntry.serviceId");
    }
    /**
     * 获取所有的客户端列表
     * @param serviceId 服务标识
     * @return 客户端列表标识
     */
    private List<Channel> clientChannelList(String serviceId) {
        ArgUtil.notEmpty(serviceId, "serviceId");

        //TODO 为空？
        Set<Channel> clientSet = serviceClientChannelMap.get(serviceId);
        return new ArrayList<>(clientSet);
    }

    @Override
    public void unSubscribe(ServiceEntry clientEntry, Channel clientChannel) {
        paramCheck(clientEntry);

        final String serviceId = clientEntry.serviceId();
        Set<Channel> channelSet = serviceClientChannelMap.get(serviceId);

        if (channelSet ==null || channelSet.isEmpty()) {
            // 服务列表为空
            log.info("[unSubscribe Client] remove host set is empty. entry: {}", clientEntry);
            return;
        }

        channelSet.remove(clientChannel);
        serviceClientChannelMap.put(serviceId, channelSet);
    }

    @Override
    public void registerNotify(String serviceId, ServiceEntry serviceEntry) {
        if(serviceId ==null){
            log.error("serviceId为空");
            return;
        }

        List<Channel> clientChannelList = clientChannelList(serviceId);
        if (CollectionUtil.isEmpty(clientChannelList)) {
            log.info("[Register] notify clients is empty for service: {}",
                    serviceId);
            return;
        }

        // 循环通知
        for(Channel channel : clientChannelList) {
            NotifyMessage notifyMessage = NotifyMessages.of(MessageTypeConst.SERVER_REGISTER_NOTIFY_CLIENT_REQ, serviceEntry);
            channel.writeAndFlush(notifyMessage);
        }
    }

    @Override
    public void unRegisterNotify(String serviceId, ServiceEntry serviceEntry) {
        ArgUtil.notEmpty(serviceId, "serviceId");

        List<Channel> clientChannelList = clientChannelList(serviceId);
        if (CollectionUtil.isEmpty(clientChannelList)) {
            log.info("[UnRegister] notify clients is empty for service: {}",
                    serviceId);
            return;
        }

        // 循环通知
        for(Channel channel : clientChannelList) {
            NotifyMessage notifyMessage = NotifyMessages.of(MessageTypeConst.SERVER_UNREGISTER_NOTIFY_CLIENT_REQ, serviceEntry);
            channel.writeAndFlush(notifyMessage);
        }
    }

    @Override
    public Collection<Channel> channels() {
        Set<Channel> resultSet = new HashSet<>();

        Collection<Set<Channel>> channelCollection =  serviceClientChannelMap.values();
        for(Set<Channel> set : channelCollection) {
            if(CollectionUtil.isNotEmpty(set)) {
                resultSet.addAll(set);
            }
        }
        return resultSet;
    }

}
