package com.github.fk.client.core;

import com.github.fk.client.domain.ClientQueryServerChannelConfig;
import com.github.fk.client.config.ReferenceConfig;
import com.github.fk.client.proxy.RemoteInvokeService;
import com.github.fk.client.proxy.ServiceContext;
import com.github.fk.client.proxy.impl.DefaultReferenceProxy;
import com.github.fk.client.proxy.impl.DefaultServiceContext;
import com.github.fk.client.proxy.impl.RemoteInvokeServiceImpl;
import com.github.fk.client.support.fail.enums.FailTypeEnum;
import com.github.fk.client.support.filter.RpcFilter;
import com.github.fk.client.support.filter.impl.RpcFilters;
import com.github.fk.client.support.hook.DefaultClientShutdownHook;
import com.github.fk.client.support.register.ClientRegisterManager;
import com.github.fk.client.support.register.impl.DefaultClientRegisterManager;
import com.github.fk.common.config.component.RpcAddress;
import com.github.fk.common.config.component.RpcAddressBuilder;
import com.github.fk.common.constant.CallTypeEnum;
import com.github.fk.common.support.hook.ShutdownHooks;
import com.github.fk.common.support.inteceptor.RpcInterceptor;
import com.github.fk.common.support.inteceptor.impl.LoadBalances;
import com.github.fk.common.support.inteceptor.impl.RpcInterceptors;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.invoke.impl.DefaultInvokeManager;
import com.github.fk.common.support.loadBalance.ILoadBalance;
import com.github.fk.common.support.resource.ResourceManager;
import com.github.fk.common.support.resource.impl.DefaultResourceManager;
import com.github.fk.common.support.status.StatusManager;
import com.github.fk.common.support.status.enums.StatusEnum;
import com.github.fk.common.support.status.impl.DefaultStatusManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 * @param <T> 接口类型
 */
@Slf4j
public class ClientBus<T> implements ReferenceConfig<T> {

    /**
     * 服务唯一标识
     *
     * @since 0.0.6
     */
    private String serviceId;

    /**
     * 服务接口
     *
     * @since 0.0.6
     */
    private Class<T> serviceInterface;

    /**
     * 服务地址信息
     * （1）如果不为空，则直接根据地址获取
     * （2）如果为空，则采用自动发现的方式
     *
     * 如果为 subscribe 可以自动发现，然后填充这个字段信息。
     *
     * @since 0.0.6
     */
    private List<RpcAddress> rpcAddresses;


    /**
     * 调用超时时间
     *
     * @since 0.0.7
     */
    private long timeout;

    /**
     * 是否进行订阅模式
     *
     * @since 0.0.8
     */
    private boolean subscribe;
    /**
     * 是否进行泛化调用
     * @since 0.1.2
     */
    private boolean generic;
    /**
     * 客户端启动检测
     * @since 0.1.5
     */
    private boolean check;

    /**
     * 注册中心列表
     *
     * @since 0.0.8
     */
    private List<RpcAddress> registerCenterList;

    /**
     * 调用服务管理类
     *
     * @since 0.0.6
     */
    private InvokeManager invokeManager;

    /**
     * 客户端注册中心服务类
     *
     * @since 0.0.9
     */
    private ClientRegisterManager clientRegisterManager;

    /**
     * 调用方式
     * @since 0.1.0
     */
    private CallTypeEnum callType;

    /**
     * 失败策略
     * @since 0.1.1
     */
    private FailTypeEnum failType;

    /**
     * 远程调用实现
     * @since 0.1.1
     */
    private RemoteInvokeService remoteInvokeService;


    /**
     * 拦截器
     * @since 0.1.4
     */
    private RpcInterceptor rpcInterceptor;

    /**
     * 状态管理类
     * @since 0.1.3
     */
    private StatusManager statusManager;

    /**
     * 资源管理类
     */
    private ResourceManager resourceManager;



    /**
     * 新建一个客户端实例
     *
     * @param <T> 泛型
     * @return this
     */
    public static <T> ClientBus<T> newInstance() {
        return new ClientBus<>();
    }

    /**
     * rpc 过滤器
     *
     * @since 0.2.0
     */
    private RpcFilter rpcFilter;

    /**
     * 负载均衡实现
     *
     * @since 0.2.0
     */
    private ILoadBalance loadBalance;
    private ClientBus(){
        // 初始化信息
        this.rpcAddresses = new ArrayList<>();
        // 默认为 60s 超时
        this.timeout = 60 * 1000;
        this.registerCenterList =  new ArrayList<>();
        this.callType = CallTypeEnum.SYNC;
        this.failType = FailTypeEnum.FAIL_OVER;
        this.generic = false;
        this.check = true;

        // 依赖服务初始化
        this.invokeManager = new DefaultInvokeManager();
        this.remoteInvokeService = new RemoteInvokeServiceImpl();
        this.statusManager = new DefaultStatusManager();
        this.resourceManager = new DefaultResourceManager();
        this.clientRegisterManager = new DefaultClientRegisterManager(invokeManager, resourceManager);

        // 拦截器与过滤器
        this.rpcInterceptor = RpcInterceptors.none();
        this.rpcFilter = RpcFilters.none();
        this.loadBalance = LoadBalances.roundRobbin();
    }

    /**
     * 获取对应的引用实现
     * （1） 处理所有反射代理信息，各自独立启动
     * （2） 启动对应的长连接
     * @return 代理类
     */

    @Override
    public T reference() {
        ClientQueryServerChannelConfig queryConfig = new ClientQueryServerChannelConfig();
        queryConfig.check(check);
        queryConfig.serviceId(serviceId);
        queryConfig.rpcAddresses(rpcAddresses);
        queryConfig.registerCenterList(registerCenterList);
        queryConfig.subscribe(subscribe);
        // 初始化注册中心的服务
        clientRegisterManager.initServerChannelFutureList(queryConfig);
        ServiceContext<T> proxyContext = buildServiceProxyContext();
        T reference =null;
        if(!this.generic){
            DefaultReferenceProxy<T> referenceProxy = new DefaultReferenceProxy<>(proxyContext, remoteInvokeService);
            reference = referenceProxy.proxy();
            if(reference ==null){
                System.out.println("reference 为空！！！！");
            }
        }else {

        }
        proxyContext.statusManager().status(StatusEnum.ENABLE.code());
        //4. 添加客户端钩子
        // 设置状态为可用
        final DefaultClientShutdownHook rpcShutdownHook = new DefaultClientShutdownHook();
        rpcShutdownHook.statusManager(statusManager);
        rpcShutdownHook.invokeManager(invokeManager);
        rpcShutdownHook.resourceManager(resourceManager);
        rpcShutdownHook.clientRegisterManager(clientRegisterManager);
        ShutdownHooks.rpcShutdownHook(rpcShutdownHook);
        return reference;
    }

    private ServiceContext<T> buildServiceProxyContext() {
        DefaultServiceContext<T> serviceContext = new DefaultServiceContext<>();
        serviceContext.serviceId(this.serviceId);
        serviceContext.serviceInterface(this.serviceInterface);
        serviceContext.clientRegisterManager(clientRegisterManager);
        serviceContext.invokeManager(this.invokeManager);
        serviceContext.timeout(this.timeout);
        serviceContext.callType(this.callType);
        serviceContext.failType(this.failType);
        serviceContext.generic(this.generic);
        serviceContext.statusManager(this.statusManager);
        serviceContext.interceptor(this.rpcInterceptor);
        serviceContext.rpcFilter(rpcFilter);
        serviceContext.loadBalance(loadBalance);

        return serviceContext;
    }

    @Override
    public ReferenceConfig<T> serviceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    @Override
    public ReferenceConfig<T> serviceInterface(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
        return this;
    }

    @Override
    public ReferenceConfig<T> addresses(String addresses) {
        log.info("[Rpc Client] service address set into {} ", addresses);
        this.rpcAddresses = RpcAddressBuilder.of(addresses);
        return this;
    }

    @Override
    public ReferenceConfig<T> timeout(long timeoutInMills) {
        this.timeout = timeoutInMills;
        return this;
    }

    @Override
    public ReferenceConfig<T> subscribe(boolean subscribe) {
        this.subscribe = subscribe;
        return this;
    }

    @Override
    public ReferenceConfig<T> registerCenter(String addresses) {
        this.registerCenterList = RpcAddressBuilder.of(addresses);
        return this;
    }

    @Override
    public ReferenceConfig<T> generic(boolean generic) {
        this.generic = generic;
        return this;
    }

    @Override
    public ReferenceConfig<T> check(boolean check) {
        this.check = check;
        return  this;
    }

    @Override
    public ReferenceConfig<T> rpcFilter(RpcFilter rpcFilter) {
        this.rpcFilter = rpcFilter;
        return this;
    }

    @Override
    public ReferenceConfig<T> loadBalance(ILoadBalance loadBalance) {
        this.loadBalance = loadBalance;
        return this;
    }

    @Override
    public ReferenceConfig<T> rpcInterceptor(RpcInterceptor rpcInterceptor) {
        this.rpcInterceptor = rpcInterceptor;
        return this;
    }

}
