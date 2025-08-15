package com.github.fk.client.support.hook;

import com.github.fk.client.support.register.ClientRegisterManager;
import com.github.fk.common.support.hook.AbstractShutdownHook;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.resource.ResourceManager;
import com.github.fk.common.support.status.StatusManager;
import com.github.fk.common.support.status.enums.StatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class DefaultClientShutdownHook extends AbstractShutdownHook {
    private StatusManager statusManager;

    /**
     * 调用管理类
     */
    private InvokeManager invokeManager;

    /**
     * 资源管理类
     */
    private ResourceManager resourceManager;

    /**
     * 客户端注册中心管理类
     */
    private ClientRegisterManager clientRegisterManager;
    public static DefaultClientShutdownHook newInstance() {
        return new DefaultClientShutdownHook();
    }

    public DefaultClientShutdownHook clientRegisterManager(ClientRegisterManager clientRegisterManager) {
        this.clientRegisterManager = clientRegisterManager;
        return this;
    }

    public DefaultClientShutdownHook statusManager(StatusManager statusManager) {
        this.statusManager = statusManager;
        return this;
    }

    public DefaultClientShutdownHook invokeManager(InvokeManager invokeManager) {
        this.invokeManager = invokeManager;
        return this;
    }

    public DefaultClientShutdownHook resourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        return this;
    }

    public DefaultClientShutdownHook waitMillsForRemainRequest(long waitMillsForRemainRequest) {
        this.waitMillsForRemainRequest = waitMillsForRemainRequest;
        return this;
    }
    /**
     * 为剩余的请求等待时间
     */
    private long waitMillsForRemainRequest = 60 * 1000;

    @Override
    protected void doHook() {
        statusManager.status(StatusEnum.WAIT_SHUTDOWN.code());
        log.info("[Shutdown] set status to wait for shutdown.");

        // 通知取消所有服务端的订阅
        clientRegisterManager.unSubscribeServerAll();
        long startMills = System.currentTimeMillis();
        while (invokeManager.remainsRequest()) {
            long currentMills = System.currentTimeMillis();
            long costMills = currentMills - startMills;
            if(costMills >= waitMillsForRemainRequest) {
                log.warn("[Shutdown] still remains request, but timeout, break.");
                break;
            }

            log.info("[Shutdown] still remains request, wait for a while.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // 销毁所有资源
        log.info("[Shutdown] resourceManager start destroy all resources.");
        this.resourceManager.destroyAll();
        log.info("[Shutdown] resourceManager finish destroy all resources.");

        // 设置状态为关闭成功
        statusManager.status(StatusEnum.SHUTDOWN_SUCCESS.code());
        log.info("[Shutdown] set status to shutdown success.");
    }
}
