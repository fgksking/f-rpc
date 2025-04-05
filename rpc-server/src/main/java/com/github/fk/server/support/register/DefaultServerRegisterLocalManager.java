package com.github.fk.server.support.register;

import cn.hutool.core.collection.CollectionUtil;
import com.github.fk.common.config.component.RpcAddress;
import com.github.fk.common.utils.ArgUtil;
import com.github.fk.common.utils.ipUtils;
import com.github.fk.register.domain.entry.ServiceEntry;
import com.github.fk.register.domain.message.NotifyMessage;
import com.github.fk.register.domain.message.body.ServerHeartbeatBody;
import com.github.fk.register.domain.message.impl.NotifyMessages;
import com.github.fk.register.simple.constant.MessageTypeConst;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 默认的服务端注册管理实现
 */
@Slf4j
public class DefaultServerRegisterLocalManager implements ServerRegisterManager{

    /**
     * 存放对应的 map 信息 （服务id ，服务提供者元信息）
     */
    private final Map<String, ServiceEntry> serviceEntryMap;
    private int port;
    /**
     * 注册中心的 channel 列表
     */
    private final Map<String, Channel> registerCenterChannelMap;

    /**
     * 心跳机制
     */
    private final ScheduledExecutorService heartbeatExecutor;

    public DefaultServerRegisterLocalManager() {
        this.serviceEntryMap = new HashMap<>();
        this.registerCenterChannelMap = new HashMap<>();

        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(new HeartbeatThread(), 5, 2, TimeUnit.SECONDS);
    }
    class HeartbeatThread implements Runnable{
        @Override
        public void run() {
            //1. 每 2 秒钟，向 channel 发送请求信息
            log.debug(("[HEARTBEAT] 开始定时执行心跳"));
            String currentIp = ipUtils.getLocalIp();
            ServerHeartbeatBody body = new ServerHeartbeatBody();
            body.ip(currentIp);
            body.port(port);
            body.time(System.currentTimeMillis());

            NotifyMessage notifyMessage = NotifyMessages.of(MessageTypeConst.SERVER_HEARTBEAT_REQ, body);
            for(Channel channel : registerCenterChannelMap.values()) {
                channel.writeAndFlush(notifyMessage);
            }
            log.debug("[HEARTBEAT] 完成定时执行心跳");
        }
    }
    @Override
    public ServerRegisterManager port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public void register(ServiceEntry serviceEntry) {
        final String serviceId = serviceEntry.serviceId();
        serviceEntryMap.put(serviceId, serviceEntry);

        // 通知注册中心
        final NotifyMessage notifyMessage = buildRegisterMessage(serviceEntry);
        for(Channel channel : registerCenterChannelMap.values()) {
            log.info("[Rpc Server] register to service center: {}", notifyMessage);
            channel.writeAndFlush(notifyMessage);
        }
    }
    /**
     * 构建注册信息配置
     *
     * @param serviceEntry 配置信息
     * @return 注册信息
     */
    private NotifyMessage buildRegisterMessage(ServiceEntry serviceEntry) {
        return NotifyMessages.of(MessageTypeConst.SERVER_REGISTER_REQ,
                serviceEntry);
    }

    @Override
    public ServiceEntry unRegister(String serviceId) {
        ServiceEntry serviceEntry = serviceEntryMap.remove(serviceId);
        log.warn("[Rpc Server] 移除服务 {} 对应的服务对象为 {}", serviceId, serviceEntry);
        if(serviceEntry == null) {
            log.warn("[Rpc Server] serviceId: {} 对应的服务信息不存在", serviceId);
            return serviceEntry;
        }

        // 通知注册中心
        final NotifyMessage notifyMessage = buildUnRegisterMessage(serviceEntry);
        for(Channel channel : registerCenterChannelMap.values()) {
            log.info("[Rpc Server] unRegister to service center: {}", notifyMessage);
            channel.writeAndFlush(notifyMessage);
        }

        return serviceEntry;
    }

    private NotifyMessage buildUnRegisterMessage(ServiceEntry serviceEntry) {
        return NotifyMessages.of(MessageTypeConst.SERVER_UN_REGISTER_REQ,
                serviceEntry);
    }

    @Override
    public void unRegisterAll() {
        Collection<ServiceEntry> serviceEntrySet = serviceEntryMap.values();

        if(CollectionUtil.isEmpty(serviceEntrySet)) {
            // 服务列表为空
            log.warn("[Rpc Server] remove service set is empty.");
            return;
        }

        for(ServiceEntry serviceEntry : serviceEntrySet) {
            this.unRegister(serviceEntry.serviceId());
        }
    }

    @Override
    public void addRegisterChannel(RpcAddress rpcAddress, Channel channel) {
        String key = buildKey(rpcAddress);
        boolean contains = registerCenterChannelMap.containsKey(key);
        if(contains) {
            log.info("[Rpc Server] 已包含添加注册中心地址：{}，不做重复添加", rpcAddress);
            return;
        }
        registerCenterChannelMap.put(key, channel);
        log.info("[Rpc Server] 添加注册中心地址：{} 对应的 channel 信息", rpcAddress);
        // 针对这一个心的机器，重新注册服务
        Collection<ServiceEntry> serviceEntries = serviceEntryMap.values();
        if(CollectionUtil.isEmpty(serviceEntries)) {
            log.info("[Rpc Server] 对应的服务列表为空，无需通知注册中心。");
            return;
        }
        for(ServiceEntry serviceEntry : serviceEntries) {
            final NotifyMessage notifyMessage = buildRegisterMessage(serviceEntry);
            log.info("[Rpc Server] register to new register center: {}", notifyMessage);
            channel.writeAndFlush(notifyMessage);
        }
    }

    private String buildKey(RpcAddress rpcAddress) {
        String key = rpcAddress.address()+":"+rpcAddress.port();

        return key;
    }

    /**
     * 注册中心发消息，本地移除channel
     * @param rpcAddress 地址
     */
    @Override
    public void removeRegisterChannel(RpcAddress rpcAddress) {
        ArgUtil.notNull(rpcAddress, "rpcAddress");

        String key = buildKey(rpcAddress);
        Channel channel = registerCenterChannelMap.remove(key);
        log.info("[Rpc Server] 移除注册中心地址：{}", rpcAddress);
    }

    @Override
    public void clearRegisterChannel() {
        log.info("[Rpc Server] 开始清空对应的注册中心 channel future");
        this.registerCenterChannelMap.clear();
        log.info("[Rpc Server] 完成清空对应的注册中心 channel future");
    }
}
