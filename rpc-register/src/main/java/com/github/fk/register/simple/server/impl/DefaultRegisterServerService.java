package com.github.fk.register.simple.server.impl;

import com.github.fk.common.utils.ArgUtil;
import com.github.fk.register.domain.entry.ServiceEntry;
import com.github.fk.register.simple.server.RegisterServerService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class DefaultRegisterServerService implements RegisterServerService {
    /**
     * 存放对应的 map 信息
     */
    private final Map<String, Set<ServiceEntry>> map;

    /**
     * 服务端对应的 channel 信息
     */
    private final Map<ServiceEntry, Channel> serviceEntryChannelMap;

    public DefaultRegisterServerService(){
        map = new ConcurrentHashMap<>();
        serviceEntryChannelMap = new ConcurrentHashMap<>();
    }


    @Override
    public List<ServiceEntry> register(ServiceEntry serviceEntry, Channel channel) {
        paramCheck(serviceEntry);

        final String serviceId = serviceEntry.serviceId();
        Set<ServiceEntry> serviceEntrySet = map.get(serviceId);
        if(serviceEntrySet ==null) {
            serviceEntrySet = new HashSet<>();
        }

        log.info("[Register Server] add service: {}", serviceEntry);
        serviceEntrySet.add(serviceEntry);
        map.put(serviceId, serviceEntrySet);

        serviceEntryChannelMap.put(serviceEntry, channel);
        // 返回更新后的结果
        return new ArrayList<>(serviceEntrySet);
    }

    @Override
    public List<ServiceEntry> unRegister(ServiceEntry serviceEntry) {
        paramCheck(serviceEntry);

        final String serviceId = serviceEntry.serviceId();
        Set<ServiceEntry> serviceEntrySet = map.get(serviceId);

        if(serviceEntrySet ==null) {
            // 服务列表为空
            log.info("[Register Server] remove service set is empty. entry: {}", serviceEntry);
            return new ArrayList<>();
        }

        serviceEntrySet.remove(serviceEntry);
        log.info("[Register Server] remove service: {}", serviceEntry);
        map.put(serviceId, serviceEntrySet);

        serviceEntryChannelMap.remove(serviceEntry);

        // 返回更新后的结果
        return new ArrayList<>(serviceEntrySet);
    }

    @Override
    public List<ServiceEntry> lookUp(String serviceId) {
        ArgUtil.notEmpty(serviceId, "serviceId");

        log.info("[Register Server] start lookUp serviceId: {}", serviceId);
        Set<ServiceEntry> serviceEntrySet = map.get(serviceId);
        log.info("[Register Server] end lookUp serviceId: {}, list: {}", serviceId,
                serviceEntrySet);
        return new ArrayList<>(serviceEntrySet);
    }

    @Override
    public Collection<Channel> channels() {
        return serviceEntryChannelMap.values();
    }


    @Override
    public Collection<ServiceEntry> serviceEntries() {
        return serviceEntryChannelMap.keySet();
    }

    @Override
    public Collection<ServiceEntry> serviceEntries(String ipPort) {
        Collection<ServiceEntry> serviceEntries = serviceEntries();

        Set<ServiceEntry> set = new HashSet<>();
        for(ServiceEntry serviceEntry : serviceEntries) {
            String ip = serviceEntry.ip();
            int port = serviceEntry.port();
            String key = ip+":"+port;

            if(key.equals(ipPort)) {
                set.add(serviceEntry);
            }
        }
        return set;
    }

    /**
     * 参数校验
     * @param serviceEntry 服务明细
     * @since 0.0.8
     */
    private void paramCheck(final ServiceEntry serviceEntry) {
        final String serviceId = serviceEntry.serviceId();
        ArgUtil.notEmpty(serviceId, "serviceId");
    }
}
