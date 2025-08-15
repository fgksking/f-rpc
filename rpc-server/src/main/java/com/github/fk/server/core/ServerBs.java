package com.github.fk.server.core;

import com.github.fk.common.config.component.RpcAddress;
import com.github.fk.common.config.component.RpcAddressBuilder;
import com.github.fk.common.config.protocol.ProtocolConfig;
import com.github.fk.common.exception.RpcRuntimeException;
import com.github.fk.common.netty.NettyServer;
import com.github.fk.common.netty.handler.ChannelHandlers;
import com.github.fk.common.netty.impl.DefaultNettyClient;
import com.github.fk.common.netty.impl.DefaultNettyServer;
import com.github.fk.common.support.delay.DelayExecutor;
import com.github.fk.common.support.delay.DelayQueueExecutor;
import com.github.fk.common.support.hook.ShutdownHooks;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.invoke.impl.DefaultInvokeManager;
import com.github.fk.common.support.resource.ResourceManager;
import com.github.fk.common.support.resource.impl.DefaultResourceManager;
import com.github.fk.common.support.status.StatusManager;
import com.github.fk.common.support.status.enums.StatusEnum;
import com.github.fk.common.support.status.impl.DefaultStatusManager;
import com.github.fk.common.threadpoll.manager.DefaultExecutorRepository;
import com.github.fk.common.threadpoll.manager.ExecutorRepository;
import com.github.fk.common.utils.ArgUtil;
import com.github.fk.common.utils.ipUtils;
import com.github.fk.register.domain.entry.ServiceEntry;
import com.github.fk.register.domain.entry.impl.ServiceEntryBuilder;
import com.github.fk.server.config.DefaultServiceConfig;
import com.github.fk.server.config.ServiceConfig;
import com.github.fk.server.handler.RpcServerHandler;
import com.github.fk.server.handler.RpcServerRegisterHandler;
import com.github.fk.server.register.ServiceRegistry;
import com.github.fk.server.service.impl.DefaultServiceFactory;
import com.github.fk.server.support.hook.DefaultServerShutdownHook;
import com.github.fk.server.support.register.DefaultServerRegisterLocalManager;
import com.github.fk.server.support.register.ServerRegisterManager;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Slf4j
public class ServerBs implements ServiceRegistry {

    private static final ServerBs INSTANCE = new ServerBs();

    /**
     * rpc 服务端端口号
     *
     * @since 0.0.6
     */
    private int rpcPort;

    /**
     * 协议配置
     * （1）默认只实现 tcp
     * （2）后期可以拓展实现 web-service/http/https 等等。
     *
     * @since 0.0.6
     */
    private ProtocolConfig protocolConfig;

    /**
     * 服务配置列表
     *
     * @since 0.0.6
     */
    private List<ServiceConfig> serviceConfigList;

    /**
     * 注册中心地址列表
     *
     * @since 0.0.8
     */
    private List<RpcAddress> registerCenterList;

    /**
     * 状态管理类
     * 
     * @since 0.1.3
     */
    private StatusManager statusManager;

    /**
     * 资源管理类
     * 
     * @since 0.1.3
     */
    private ResourceManager resourceManager;

    /**
     * 调用管理类
     * 
     * @since 0.1.3
     */
    private InvokeManager invokeManager;

    /**
     * 延迟执行器
     * 
     * @since 0.1.7
     */
    private DelayExecutor delayExecutor;

    private ServerBs() {
        this.serviceConfigList = new ArrayList<>();
        this.rpcPort = 9527;
        this.registerCenterList = new ArrayList<>();
        this.statusManager = new DefaultStatusManager();
        this.resourceManager = new DefaultResourceManager();
        this.invokeManager = new DefaultInvokeManager();
        this.serverRegisterManager = new DefaultServerRegisterLocalManager();
        serverRegisterManager.port(rpcPort);
        delayExecutor = new DelayQueueExecutor();
        executorRepository = new DefaultExecutorRepository(null);
    }

    public static ServerBs getInstance() {
        return INSTANCE;
    }

    /**
     * 服务注册中心管理
     * 
     * @since 0.1.8
     */
    private ServerRegisterManager serverRegisterManager;


    private ExecutorRepository executorRepository;

    /**
     * （1）主要用于后期服务调用
     * (2) id 唯一
     * （3） 根据RpcRequest获取对应方法
     * 
     * @param serviceId   方法id
     * @param serviceImpl 接口实现类
     * @return
     */
    @Override
    public synchronized ServerBs register(String serviceId, Object serviceImpl) {
        ArgUtil.notEmpty(serviceId, "serviceId");
        ArgUtil.notNull(serviceImpl, "serviceImpl");
        ServiceConfig serviceConfig = new DefaultServiceConfig();
        serviceConfig.id(serviceId)
                .reference(serviceImpl)
                .register(true)
                .delay(0);

        addServiceConfig(serviceConfig);

        return this;
    }

    private void addServiceConfig(ServiceConfig serviceConfig) {
        final String id = serviceConfig.id();
        for (ServiceConfig config : serviceConfigList) {
            if (config.id().equals(id)) {
                log.error("serviceConfig id has been registered, please check for id: {}", id);
                throw new RpcRuntimeException();
            }
        }
        this.serviceConfigList.add(serviceConfig);
    }

    @Override
    public ServiceRegistry port(int port) {
        this.rpcPort = port;
        serverRegisterManager.port(rpcPort);
        return this;
    }

    @Override
    public ServiceRegistry register(ServiceConfig serviceConfig) {
        ArgUtil.notNull(serviceConfig, "serviceConfig");
        ArgUtil.notNull(serviceConfig.reference(), "serviceConfig.reference");
        ArgUtil.notNull(serviceConfig.id(), "serviceConfig.id");
        // 构建对应的其他信息
        addServiceConfig(serviceConfig);
        return this;
    }

    @Override
    public ServiceRegistry expose() {
        // 1. 注册所有服务信息
        DefaultServiceFactory.getInstance()
                .registerServicesLocal(serviceConfigList);
        // 2. 启动 netty server 信息
        final ChannelHandler channelHandler = ChannelHandlers
                .objectCodecHandler(new RpcServerHandler(invokeManager, statusManager,executorRepository));
        NettyServer nettyServer = DefaultNettyServer.newInstance(rpcPort, channelHandler);
        nettyServer.asyn();
        log.info("server service start finish.");
        this.resourceManager.addDestroy(nettyServer);

        // 3. 注册到配置中心
        this.registerServiceCenter();
        log.info("server service register finish.");

        return this;
    }

    /**
     * 注册到服务中心，注册到配置中心列表
     *
     */
    private void registerServiceCenter() {
        for (RpcAddress rpcAddress : registerCenterList) {
            RpcServerRegisterHandler rpcServerRegisterHandler = new RpcServerRegisterHandler(serverRegisterManager);
            ChannelHandler registerHandler = ChannelHandlers.objectCodecHandler(rpcServerRegisterHandler);
            log.info("[Rpc Server] start register to {}:{}", rpcAddress.address(),
                    rpcAddress.port());
            // TODO: 针对配置中心可以进一步细化，比如某一个 ip 变更，做对应的销毁，但是没有必要，一般配置中心变动的可能性较小。
            DefaultNettyClient nettyClient = DefaultNettyClient.newInstance(rpcAddress.address(), rpcAddress.port(),
                    registerHandler);
            ChannelFuture channelFuture = nettyClient.call();
            resourceManager.addDestroy(nettyClient);

            // 添加到服务端管理中
            serverRegisterManager.addRegisterChannel(rpcAddress, channelFuture.channel());
        }
        // 注册到配置中心
        // 初期简单点，直接循环调用即可
        // 循环服务信息
        for (final ServiceConfig config : this.serviceConfigList) {
            boolean register = config.register();
            final String serviceId = config.id();
            if (!register) {
                log.info("[Rpc Server] serviceId: {} register config is false.",
                        serviceId);
                continue;
            }

            // 兼容小于 0 的情况
            long delayMills = config.delay();
            if (delayMills <= 0) {
                delayMills = 0;
            }

            // ps: 这里也可以把不延迟的同步执行。
            // 统一写可以保证逻辑的一致性
            delayExecutor.delay(delayMills, new Runnable() {
                @Override
                public void run() {
                    log.info("[Rpc Server] serviceId: {} delay init start.", serviceId);
                    // 服务端通知到注册中心
                    final String hostIp = ipUtils.getLocalIp();
                    log.info("服务端地址 ： {}", hostIp);
                    ServiceEntry serviceEntry = ServiceEntryBuilder.of(config.id(), "127.0.0.1", rpcPort);
                    log.info("注册到注册中心的元数据为= {}", serviceEntry);
                    serverRegisterManager.register(serviceEntry);

                    // 4. 添加服务端钩子函数
                    statusManager.status(StatusEnum.ENABLE.code());
                    log.info("[Rpc Server] serviceId: {} delay init end.", serviceId);
                }
            });
        }

        // 统一添加钩子函数
        final DefaultServerShutdownHook rpcShutdownHook = new DefaultServerShutdownHook();
        rpcShutdownHook.statusManager(statusManager);
        rpcShutdownHook.invokeManager(invokeManager);
        rpcShutdownHook.resourceManager(resourceManager);
        rpcShutdownHook.serverRegisterManager(serverRegisterManager);
        // 添加线程池关闭钩子
        ShutdownHooks.rpcShutdownHook(() -> {
            log.info("开始关闭服务端线程池...");
            executorRepository.shutdown();
        });
        ShutdownHooks.rpcShutdownHook(rpcShutdownHook);

    }

    /**
     * 添加注册中心地址
     * 
     * @param addresses
     * @return
     */
    @Override
    public ServiceRegistry registerCenter(String addresses) {
        this.registerCenterList = RpcAddressBuilder.of(addresses);
        return this;
    }
}
