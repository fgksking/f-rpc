package com.github.fk.client.support.register.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.fk.client.domain.ClientQueryServerChannelConfig;
import com.github.fk.client.handler.RpcClientHandler;
import com.github.fk.client.handler.RpcClientRegisterHandler;
import com.github.fk.client.support.register.ClientRegisterManager;
import com.github.fk.common.config.component.RpcAddress;
import com.github.fk.common.netty.handler.ChannelHandlerFactory;
import com.github.fk.common.netty.handler.ChannelHandlers;
import com.github.fk.common.rpc.domain.RpcChannelFuture;
import com.github.fk.common.rpc.domain.RpcResponse;
import com.github.fk.common.rpc.domain.impl.RpcResponses;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.resource.ResourceManager;
import com.github.fk.common.support.status.enums.Endpoint;
import com.github.fk.common.threadpoll.manager.ExecutorRepository;
import com.github.fk.common.utils.ArgUtil;
import com.github.fk.register.domain.entry.ServiceEntry;
import com.github.fk.register.domain.entry.impl.ServiceEntryBuilder;
import com.github.fk.register.domain.message.NotifyMessage;
import com.github.fk.register.domain.message.body.RegisterCenterAddNotifyBody;
import com.github.fk.register.domain.message.body.RegisterCenterRemoveNotifyBody;
import com.github.fk.register.domain.message.impl.NotifyMessages;
import com.github.fk.register.simple.constant.MessageTypeConst;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * 客户端默认中心实现类
 */
@Slf4j
public class DefaultClientRegisterManager implements ClientRegisterManager {
    private final InvokeManager invokeManager;

    /**
     * 资源管理类
     */
    private final ResourceManager resourceManager;

    /**
     * 注册中心超时时间
     *
     */
    private final long registerCenterTimeOut;

    /**
     * 查询配置信息
     */
    private final Map<String, ClientQueryServerChannelConfig> queryConfigMap;

    /**
     * 服务端 channel 列表信息
     */
    private final Map<String, List<RpcChannelFuture>> serverChannelFutureMap;

    /**
     * 注册中心对应的 channel 信息
     */
    private final Map<String, RpcChannelFuture> registerCenterChannelMap;

    /**
     * 关注的服务标识集合
     */
    private final Set<String> subscribeServerIdSet;


    //线程池处理消息
    private final Executor executor;


    /**
     * 单线程定时处理任务 服务变更等任务
     */
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    public DefaultClientRegisterManager(InvokeManager invokeManager, ResourceManager resourceManager) {
        this.invokeManager = invokeManager;
        this.resourceManager = resourceManager;

        this.registerCenterTimeOut = 60 * 1000;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.queryConfigMap = new ConcurrentHashMap<>();
        this.serverChannelFutureMap = new ConcurrentHashMap<>();
        this.registerCenterChannelMap = new ConcurrentHashMap<>();
        this.subscribeServerIdSet = new HashSet<>();

        final Runnable runnable = new LocalChannelFutureThread();
        EXECUTOR_SERVICE.schedule(runnable, 60, TimeUnit.SECONDS);
    }

    private class LocalChannelFutureThread implements Runnable {
        @Override
        public void run() {
            log.info("开始定时更新本地的服务端连接信息");
            for (Map.Entry<String, List<RpcChannelFuture>> entry : serverChannelFutureMap.entrySet()) {
                String serviceId = entry.getKey();
                log.info("开始定时更新本地的服务端 {} 连接信息", serviceId);
                ClientQueryServerChannelConfig config = queryConfigMap.get(serviceId);
                if (config == null) {
                    log.warn("serviceId: {} 对应的查询配置为空，忽略查询。", serviceId);
                    continue;
                }
                final List<RpcAddress> rpcAddresses = config.rpcAddresses();
                boolean subscribe = config.subscribe();
                if (CollectionUtil.isNotEmpty(rpcAddresses) || !subscribe) {
                    log.warn("serviceId: {} 对应的配置固定，无需定时更新。", serviceId);
                    continue;
                }
                // 查询对应的配置信息
                List<RpcChannelFuture> resultFutureList = new ArrayList<>();
                List<RpcChannelFuture> oldFutureList = entry.getValue();
                List<RpcAddress> lastRpcAddressList = getRpcAddresses(config);
                // 2.1 以前没有，现在有的
                // 2.2 以前现在都有的
                for (RpcAddress newAddress : lastRpcAddressList) {
                    for (RpcChannelFuture oldFuture : oldFutureList) {
                        RpcAddress oldAddress = oldFuture.address();
                        // 以前现在都有的
                        if (oldAddress.equals(newAddress)) {
                            resultFutureList.add(oldFuture);
                        } else {
                            // 新增的
                            RpcChannelFuture future = ChannelHandlers.channelFuture(newAddress,
                                    new ChannelHandlerFactory() {
                                        @Override
                                        public ChannelHandler handler() {
                                            final ChannelHandler channelHandler = new RpcClientHandler(invokeManager,executor);
                                            return ChannelHandlers.objectCodecHandler(channelHandler);
                                        }
                                    });

                            resourceManager.addDestroy(future.destroyable());
                            resultFutureList.add(oldFuture);
                        }
                    }
                }
                // 2.3 以前有，现在没有的
                // 不用处理，这里直接以最新的为准，没有添加。
                serverChannelFutureMap.put(serviceId, resultFutureList);
                log.info("完成定时更新本地的服务端 {} 连接信息", serviceId);
            }
        }
    }

    @Override
    public void initServerChannelFutureList(ClientQueryServerChannelConfig config) {
        List<RpcAddress> rpcAddresses = getRpcAddresses(config);

        List<RpcChannelFuture> channelFutureList = this.initChannelFutureList(rpcAddresses, config);
        for (RpcChannelFuture channelFuture : channelFutureList) {
            this.resourceManager.addDestroy(channelFuture.destroyable());
        }

        // 存放到本地 map 中，用于本地定时更新
        String serviceId = config.serviceId();
        queryConfigMap.put(serviceId, config);
        log.info("把serviceId={} 存入到map", serviceId);
        serverChannelFutureMap.put(serviceId, channelFutureList);

    }

    private List<RpcChannelFuture> initChannelFutureList(List<RpcAddress> rpcAddressList,
            ClientQueryServerChannelConfig config) {
        final boolean check = config.check();
        // 检测可用性
        if (check) {
            // 1. 列表为空
            if (CollectionUtil.isEmpty(rpcAddressList)) {
                log.error("[Rpc Client] rpc address list is empty!");
                throw new RuntimeException();
            }

            // 2. 初始化这里应该返回的是 服务端的channel的信息，
            return ChannelHandlers.channelFutureList(rpcAddressList, new ChannelHandlerFactory() {
                @Override
                public ChannelHandler handler() {
                    final ChannelHandler channelHandler = new RpcClientHandler(invokeManager,executor);
                    return ChannelHandlers.objectCodecHandler(channelHandler);
                }
            });
        }

        // 如果不检测可用性
        List<RpcChannelFuture> resultList = new ArrayList<>();
        if (CollectionUtil.isEmpty(rpcAddressList)) {
            log.warn("[Rpc Client] rpc address list is empty, without check init.");
            return resultList;
        }
        // 如果异常，则捕获
        for (RpcAddress rpcAddress : rpcAddressList) {
            try {
                RpcChannelFuture future = ChannelHandlers.channelFuture(rpcAddress, new ChannelHandlerFactory() {
                    @Override
                    public ChannelHandler handler() {
                        final ChannelHandler channelHandler = new RpcClientHandler(invokeManager,executor);
                        return ChannelHandlers.objectCodecHandler(channelHandler);
                    }
                });
                resultList.add(future);
            } catch (Exception exception) {
                log.error("[Rpc Client] rpc address init failed, without check init. {}", rpcAddress, exception);
            }
        }

        return resultList;
    }

    @Override
    public List<RpcChannelFuture> queryServerChannelFutures(String serviceId) {
        log.info("serviceId = {}", serviceId);
        return serverChannelFutureMap.get(serviceId);
    }

    // 销毁时关闭订阅
    @Override
    public void unSubscribeServerAll() {
        log.info("开始取消订阅所有服务端信息");

        // TODO: 后续可以优化，添加对应的批处理。
        if (CollectionUtil.isEmpty(subscribeServerIdSet)) {
            log.info("订阅服务器的集合为空，忽略处理");
        } else {
            for (String serviceId : subscribeServerIdSet) {
                for (RpcChannelFuture rpcChannelFuture : this.registerCenterChannelMap.values()) {
                    ServiceEntry serviceEntry = ServiceEntryBuilder.of(serviceId);
                    NotifyMessage subscribeMessage = NotifyMessages.of(MessageTypeConst.CLIENT_UN_SUBSCRIBE_REQ,
                            serviceEntry);
                    // 这里可以直接 oneway
                    ChannelFuture future = rpcChannelFuture.channelFuture();
                    future.channel().writeAndFlush(subscribeMessage);
                    log.info("客户端 unSubscribe serviceId {} for register center address {}", serviceId,
                            rpcChannelFuture.address());
                }
            }
        }

        log.info("完成取消订阅所有服务端信息");
    }

    /**
     * 服务端注册通知
     * 
     * @param serviceEntry 服务端信息
     */
    @Override
    public void serverRegisterNotify(ServiceEntry serviceEntry) {
        log.info("客户端收到服务端注册通知{}", serviceEntry);
        String serviceId = serviceEntry.serviceId();
        // 是否早已包含
        boolean contains = containsServerEntry(serviceEntry);
        if (contains) {
            log.warn("已经包含当前服务 {}, 跳过处理。", serviceEntry);
            return;
        }
        // 新增的
        RpcAddress newAddress = new RpcAddress(serviceEntry.ip(), serviceEntry.port());
        RpcChannelFuture future = ChannelHandlers.channelFuture(newAddress, new ChannelHandlerFactory() {
            @Override
            public ChannelHandler handler() {
                final ChannelHandler channelHandler = new RpcClientHandler(invokeManager,executor);
                return ChannelHandlers.objectCodecHandler(channelHandler);
            }
        });
        resourceManager.addDestroy(future.destroyable());
        List<RpcChannelFuture> futureList = serverChannelFutureMap.get(serviceId);
        futureList.add(future);
        serverChannelFutureMap.put(serviceId, futureList);
    }

    /**
     * 是否包含服务
     * 
     * @param serviceEntry
     * @return
     */
    private boolean containsServerEntry(ServiceEntry serviceEntry) {
        String serviceId = serviceEntry.serviceId();

        List<RpcChannelFuture> futureList = serverChannelFutureMap.get(serviceId);
        if (CollectionUtil.isEmpty(futureList)) {
            return false;
        }

        // 服务可能部署了多台实例，去比对是否一致
        for (RpcChannelFuture rpcChannelFuture : futureList) {
            RpcAddress rpcAddress = rpcChannelFuture.address();
            if (isTheSameIpPort(rpcAddress, serviceEntry)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTheSameIpPort(RpcAddress rpcAddress, ServiceEntry serviceEntry) {
        String ipPort = rpcAddress.address() + ":" + rpcAddress.port();
        String ipPortEntry = serviceEntry.ip() + ":" + serviceEntry.port();
        return ipPort.equals(ipPortEntry);
    }

    @Override
    public void serverUnRegisterNotify(ServiceEntry serviceEntry) {
        log.info("客户端接收到服务端注销通知 :{}", serviceEntry);
        // 拿到 serviceId 对应的ChannelFuture
        String serviceId = serviceEntry.serviceId();

        List<RpcChannelFuture> futureList = serverChannelFutureMap.get(serviceId);
        if (CollectionUtil.isEmpty(futureList)) {
            log.warn("serviceId: {} 对应的服务列表为空，无需处理。", serviceId);
            return;
        }
        // 遍历移除其中一台服务的地址
        Iterator<RpcChannelFuture> iterator = futureList.listIterator();
        while (iterator.hasNext()) {
            RpcChannelFuture rpcChannelFuture = iterator.next();
            RpcAddress rpcAddress = rpcChannelFuture.address();

            // 是否相同
            boolean isTheSame = isTheSameIpPort(rpcAddress, serviceEntry);
            if (isTheSame) {
                log.info("移除服务端 {} 对应的 channel 信息", serviceEntry);
                iterator.remove();
            }
        }

        // 更新
        serverChannelFutureMap.put(serviceId, futureList);
    }

    @Override
    public void addRegisterChannel(RegisterCenterAddNotifyBody body, Channel channel) {
        log.info("接收到注册中心新增机器的通知 {}", body);
        String ip = body.ip();
        int port = body.port();

        boolean contains = containsIpPort(ip, port);
        if (contains) {
            log.info("当前注册中心的服务信息已包含，不用处理。");
            return;
        }

        // 重新创建
        RpcAddress rpcAddress = new RpcAddress(ip, port);
        final String key = buildKey(rpcAddress);
        RpcChannelFuture newChannel = createNewRegisterChannel(rpcAddress);
        registerCenterChannelMap.put(key, newChannel);

        // 推送所有关注的服务标识
        for (String serviceId : subscribeServerIdSet) {
            subscribeServer(serviceId, newChannel);
        }
    }

    private boolean containsIpPort(String ip, int port) {
        Collection<RpcChannelFuture> futures = registerCenterChannelMap.values();
        for (RpcChannelFuture future : futures) {
            if (isTheSameIpPort(future, ip, port)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTheSameIpPort(RpcChannelFuture future, String ip, int port) {
        RpcAddress address = future.address();
        return address.address().equals(ip) && address.port() == port;
    }

    @Override
    public void removeRegisterChannel(RegisterCenterRemoveNotifyBody body) {
        String ip = body.ip();
        int port = body.port();
        for (Map.Entry<String, RpcChannelFuture> entry : registerCenterChannelMap.entrySet()) {
            RpcChannelFuture future = entry.getValue();

            if (isTheSameIpPort(future, ip, port)) {
                log.info("开始移除注册中心注销的机器 ip: {}, port: {}", ip, port);
                String key = entry.getKey();
                future.destroyable().destroy();
                registerCenterChannelMap.remove(key);
                log.info("完成移除注册中心注销的机器 ip: {}, port: {}", ip, port);
            }
        }
    }

    /**
     * 获取rpc 地址信息列表
     * 
     * @param config
     * @return
     */
    private List<RpcAddress> getRpcAddresses(ClientQueryServerChannelConfig config) {
        final List<RpcAddress> rpcAddresses = config.rpcAddresses();
        final List<RpcAddress> registerCenterList = config.registerCenterList();
        final String serviceId = config.serviceId();

        // 0. 快速返回
        if (CollectionUtil.isNotEmpty(rpcAddresses)) {
            return rpcAddresses;
        }
        // 服务地址为空
        // 1. 检查注册中心地址
        registerCenterParamCheck(config);
        // 查询注册中心服务
        return this.queryServerAddressList(serviceId, registerCenterList, config);
    }

    /**
     * 查询服务端地址信息列表
     * （1）registerCenterTimeOut 超时时间内部直接指定
     * 
     * @param serviceId          服务唯一标识
     * @param registerCenterList 注册中心列表
     * @param config             配置信息
     * @return 服务端地址信息列表
     * @since 0.0.9
     */
    private List<RpcAddress> queryServerAddressList(String serviceId, List<RpcAddress> registerCenterList,
            ClientQueryServerChannelConfig config) {
        ArgUtil.notEmpty(serviceId, "serviceId");
        ArgUtil.notEmpty(registerCenterList, "registerCenterList");
        // 2. 查询服务信息
        List<ServiceEntry> serviceEntries = lookUpServiceEntryList(serviceId,
                registerCenterList, config);
        log.info("[Client] register center serviceEntries: {}", serviceEntries);

        List<RpcAddress> rpcAddresses = new ArrayList<>();
        for (ServiceEntry serviceEntry : serviceEntries) {
            rpcAddresses.add(new RpcAddress(serviceEntry.ip(), serviceEntry.port(), serviceEntry.weight()));
        }
        return rpcAddresses;
    }

    private List<ServiceEntry> lookUpServiceEntryList(String serviceId, List<RpcAddress> registerCenterList,
            ClientQueryServerChannelConfig config) {
        // 1. 连接到注册中心(仅仅只是获取连接，此时的服务端并未响应)
        List<RpcChannelFuture> channelFutureList = connectRegisterCenter(registerCenterList);
        boolean subscribe = config.subscribe();
        if (subscribe) {
            // 如果是自动服务发现的话，发送服务订阅请求
            this.subscribeServer(serviceId);
        }
        // 感觉只需要遍历一次就够了，因为一般来说多个注册中心连接的服务都是一样的，只需返回一个即可
        // 如果都要去查询一遍 ，还要去重操作
        for (RpcChannelFuture rpcChannelFuture : channelFutureList) {
            try {
                ChannelFuture channelFuture = rpcChannelFuture.channelFuture();
                // . 发送查询服务请求
                ServiceEntry serviceEntry = ServiceEntryBuilder.of(serviceId);
                NotifyMessage lookUpMessage = NotifyMessages.of(MessageTypeConst.CLIENT_LOOK_UP_SERVER_REQ,
                        serviceEntry);
                log.info("发送查询服务请求 = {}", lookUpMessage);
                final String seqId = lookUpMessage.seqId();
                invokeManager.addRequest(seqId, registerCenterTimeOut);
                channelFuture.channel().writeAndFlush(lookUpMessage);
                // 等待查询结果 TODO 封装结果
                RpcResponse rpcResponse = invokeManager.getResponse(seqId);
                return (List<ServiceEntry>) RpcResponses.getResult(rpcResponse);
            } catch (Exception exception) {
                log.warn("注册中心查询异常，继续尝试其他服务器。", exception);
            }
        }

        throw new RuntimeException("服务端发现失败，请检查注册中心服务是否正常。");
    }

    @Override
    public void subscribeServer(String serviceId) {
        Collection<RpcChannelFuture> registerChannelFutureList = registerCenterChannelMap.values();
        if (CollectionUtil.isEmpty(registerChannelFutureList)) {
            log.warn("注册中心连接列表为空，忽略处理。");
            return;
        }
        if (subscribeServerIdSet.contains(serviceId)) {
            log.info("serviceId: {} 对应的 subscribe 已经设置，本次忽略处理", serviceId);
        } else {
            for (RpcChannelFuture rpcChannelFuture : registerChannelFutureList) {
                // 发送订阅请求
                subscribeServer(serviceId, rpcChannelFuture);
            }

            // 设置
            subscribeServerIdSet.add(serviceId);
        }
    }

    private void subscribeServer(String serviceId, RpcChannelFuture rpcChannelFuture) {
        log.info("开始推送关注，serviceId: {}, rpcAddress: {}", serviceId, rpcChannelFuture.address());
        ServiceEntry serviceEntry = ServiceEntryBuilder.of(serviceId);
        NotifyMessage subscribeMessage = NotifyMessages.of(MessageTypeConst.CLIENT_SUBSCRIBE_REQ, serviceEntry);
        // 这里可以直接 oneway
        ChannelFuture future = rpcChannelFuture.channelFuture();
        // 写入订阅的请求
        future.channel().writeAndFlush(subscribeMessage);
        log.info("客户端 subscribe serviceId {} for register center address {}", serviceId,
                rpcChannelFuture.address());
    }

    /**
     * 查询所有注册中心地址， 获取连接
     * 
     * @param registerCenterList
     * @return
     */
    private List<RpcChannelFuture> connectRegisterCenter(final List<RpcAddress> registerCenterList) {
        List<RpcChannelFuture> rpcChannelFutures = new ArrayList<>();
        for (RpcAddress rpcAddress : registerCenterList) {
            String key = buildKey(rpcAddress);
            log.info("方法connectRegisterCenter的key={}", key);
            RpcChannelFuture oldChannel = registerCenterChannelMap.get(key);
            if (oldChannel != null) {
                rpcChannelFutures.add(oldChannel);
            } else {
                RpcChannelFuture newChannel = createNewRegisterChannel(rpcAddress);
                rpcChannelFutures.add(newChannel);
            }
        }
        return rpcChannelFutures;
    }

    /**
     * 创建新的注册中心连接
     * 
     * @param rpcAddress
     * @return
     */
    private RpcChannelFuture createNewRegisterChannel(RpcAddress rpcAddress) {
        final ChannelHandler channelHandler = new RpcClientRegisterHandler(invokeManager, this);
        log.info("创建新的注册中心连接");
        RpcChannelFuture channelFuture = ChannelHandlers.channelFuture(rpcAddress, new ChannelHandlerFactory() {
            @Override
            public ChannelHandler handler() {
                return ChannelHandlers.objectCodecHandler(channelHandler);
            }
        });
        resourceManager.addDestroy(channelFuture.destroyable());
        final String key = buildKey(rpcAddress);
        // 放入便于复用
        registerCenterChannelMap.put(key, channelFuture);

        return channelFuture;
    }

    private String buildKey(RpcAddress rpcAddress) {
        return rpcAddress.address() + ":" + rpcAddress.port();
    }

    /**
     * 参数检查，如果没有自动发现，说明服务不可用
     * 
     * @param config
     */
    private void registerCenterParamCheck(ClientQueryServerChannelConfig config) {
        final boolean subscribe = config.subscribe();
        final String serviceId = config.serviceId();
        if (!subscribe) {
            log.error("[Rpc Client] no available services found for serviceId:{}", serviceId);
            throw new RuntimeException();
        }
    }

}
