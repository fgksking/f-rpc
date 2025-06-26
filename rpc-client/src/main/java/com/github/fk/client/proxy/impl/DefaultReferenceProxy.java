package com.github.fk.client.proxy.impl;

import cn.hutool.core.util.IdUtil;
import com.github.fk.client.proxy.ReferenceProxy;
import com.github.fk.client.proxy.RemoteInvokeService;
import com.github.fk.client.proxy.ServiceContext;
import com.github.fk.common.exception.RpcRuntimeException;
import com.github.fk.common.rpc.domain.impl.DefaultRpcRequest;
import com.github.fk.common.support.status.enums.StatusEnum;
import com.github.fk.common.utils.ReflectMethodUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
@Slf4j
public class DefaultReferenceProxy<T> implements ReferenceProxy<T> {

    private final ServiceContext<T> proxyContext;

    private final RemoteInvokeService remoteInvokeService;

    public DefaultReferenceProxy(ServiceContext<T> proxyContext, RemoteInvokeService remoteInvokeService) {
        this.proxyContext = proxyContext;
        this.remoteInvokeService = remoteInvokeService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T proxy() {
        try {
            final Class<T> interfaceClass = proxyContext.serviceInterface();
            // 1. 校验接口有效性
            if (interfaceClass == null || !interfaceClass.isInterface()) {
                throw new IllegalStateException("Service interface must be a non-null interface");
            }
            // 2. 校验服务状态
            StatusEnum.assertEnable(proxyContext.statusManager().status());
            // 3. 安全获取类加载器
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = interfaceClass.getClassLoader();
            }
            // 4. 创建代理
            return (T) Proxy.newProxyInstance(
                    classLoader,
                    new Class[]{interfaceClass},
                    this
            );
           /* ClassLoader classLoader = interfaceClass.getClassLoader();
            Class<?>[] interfaces = new Class[]{interfaceClass};
            return (T) Proxy.newProxyInstance(classLoader, interfaces, this);*/
        } catch (Exception e) {
            throw new RpcRuntimeException("Failed to create proxy for " +
                    proxyContext.serviceId(), e);
        }
    }

    /**
     *
     * 反射调用
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //构造反射上下文
        final String traceId = IdUtil.randomUUID();
        final int statusCode = proxyContext.statusManager().status();
 //       StatusEnum.assertEnable(statusCode);
        final long createTime = System.currentTimeMillis();
        //拦截器

        //构造请求参数
        DefaultRpcRequest rpcRequest = new DefaultRpcRequest();
        rpcRequest.serviceId(proxyContext.serviceId());
        rpcRequest.createTime(createTime);
        rpcRequest.paramValues(args);
        rpcRequest.paramTypeNames(ReflectMethodUtil.getParamTypeNames(method));
        rpcRequest.methodName(method.getName());
        rpcRequest.returnType(method.getReturnType());
        rpcRequest.timeout(proxyContext.timeout());
        rpcRequest.callType(proxyContext.callType());
        //调用远程调用
        DefaultRemoteInvokeContext<T> context = new DefaultRemoteInvokeContext<>();
        context.request(rpcRequest);
        context.traceId(traceId);
        context.retryTimes(2);
        context.serviceProxyContext(proxyContext);
        context.remoteInvokeService(remoteInvokeService);
        log.info("remoteInvoke    ");
        Object result = remoteInvokeService.remoteInvoke(context);
        //拦截器结束
        //加多一层可以方便处理前后文

        return result;
    }
}
