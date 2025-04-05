package com.github.fk.server.service.impl;

import com.github.fk.common.exception.RpcRuntimeException;
import com.github.fk.common.utils.ArgUtil;
import com.github.fk.common.utils.ReflectMethodUtil;
import com.github.fk.server.config.ServiceConfig;
import com.github.fk.server.service.ServiceFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultServiceFactory implements ServiceFactory {

    private static final DefaultServiceFactory INSTANCE = new DefaultServiceFactory();
    private DefaultServiceFactory(){}
    public static DefaultServiceFactory getInstance(){
        return INSTANCE;
    }

    /**
     * 服务map 缓存 ,*(id , class)
     */
    private Map<String,Object> serviceMap;

    /**
     * 对应method 信息
     *  *（serviceId:methodName:param1@param2 , method）
     */
    private Map<String, Method> methodMap;

    @Override
    public ServiceFactory registerServicesLocal(List<ServiceConfig> serviceConfigList) {
        ArgUtil.notEmpty(serviceConfigList, "serviceConfigList");
        serviceMap = new HashMap<>(serviceConfigList.size());
        // 这里只是预估，一般为2个服务。
        methodMap = new HashMap<>(serviceConfigList.size()*2);
        for(ServiceConfig serviceConfig : serviceConfigList) {
            serviceMap.put(serviceConfig.id(), serviceConfig.reference());
        }
        for (Map.Entry<String, Object> entry : serviceMap.entrySet()) {
            String serviceId = entry.getKey();
            Object reference = entry.getValue();
            //获取所有method
            Method[] methods = reference.getClass().getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                if(ReflectMethodUtil.isIgnoreMethod(methodName)){
                    continue;
                }
                List<String> paramTypeNames = ReflectMethodUtil.getParamTypeNames(method);
                String key = buildMethodKey(serviceId, methodName, paramTypeNames);
                methodMap.put(key, method);
            }
        }
        return this;
    }

    private String buildMethodKey(String serviceId, String methodName, List<String> paramTypeNames) {
        StringBuilder keyBuilder = new StringBuilder();
        // 拼接 serviceId
        keyBuilder.append(serviceId).append(":");
        // 拼接 methodName
        keyBuilder.append(methodName);
        // 如果有参数类型，拼接参数部分
        if (paramTypeNames != null && !paramTypeNames.isEmpty()) {
            keyBuilder.append(":");
            // 使用 Stream 或循环拼接参数类型，并用 @ 分隔
            String paramPart = String.join("@", paramTypeNames);
            keyBuilder.append(paramPart);
        }
        return keyBuilder.toString();

    }


    @Override
    public Object invoke(String serviceId, String methodName, List<String> paramTypeNames, Object[] paramValues) {
        //参数校验
        ArgUtil.notEmpty(serviceId, "serviceId");
        ArgUtil.notEmpty(methodName, "methodName");

        // 提供 cache，可以根据前三个值快速定位对应的 method
        // 根据 method 进行反射处理。
        // 对于 paramTypes 进行 string 连接处理。
        final Object reference = serviceMap.get(serviceId);
        final String methodKey = buildMethodKey(serviceId, methodName, paramTypeNames);
        final Method method = methodMap.get(methodKey);

        try {
            return method.invoke(reference, paramValues);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RpcRuntimeException(e);
        }
    }
}
