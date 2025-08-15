package com.github.fk.server.support.hook;

import com.github.fk.common.support.hook.AbstractShutdownHook;
import com.github.fk.common.support.invoke.InvokeManager;
import com.github.fk.common.support.resource.ResourceManager;
import com.github.fk.common.support.status.StatusManager;
import com.github.fk.common.support.status.enums.StatusEnum;
import com.github.fk.server.support.register.ServerRegisterManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultServerShutdownHook extends AbstractShutdownHook {

    private StatusManager statusManager;
    private InvokeManager invokeManager;
    private ResourceManager resourceManager;
    private ServerRegisterManager serverRegisterManager;
    private long waitMillsForRemainRequest = 60 * 1000;

    /**
     * 新建对象
     * @return this
     */
    public static DefaultServerShutdownHook newInstance() {
        return new DefaultServerShutdownHook();
    }

    public StatusManager statusManager() {
        return statusManager;
    }

    public DefaultServerShutdownHook statusManager(StatusManager statusManager) {
        this.statusManager = statusManager;
        return this;
    }

    public InvokeManager invokeManager() {
        return invokeManager;
    }

    public DefaultServerShutdownHook invokeManager(InvokeManager invokeManager) {
        this.invokeManager = invokeManager;
        return this;
    }

    public ResourceManager resourceManager() {
        return resourceManager;
    }

    public DefaultServerShutdownHook resourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        return this;
    }

    public ServerRegisterManager serverRegisterManager() {
        return serverRegisterManager;
    }

    public DefaultServerShutdownHook serverRegisterManager(ServerRegisterManager serverRegisterManager) {
        this.serverRegisterManager = serverRegisterManager;
        return this;
    }

    public DefaultServerShutdownHook waitMillsForRemainRequest(long waitMillsForRemainRequest) {
        this.waitMillsForRemainRequest = waitMillsForRemainRequest;
        return this;
    }

    /**
     * 设置 status
     *  关闭时是否包含请求
     *  等待或强制关闭
     *  关闭所有线程资源
     */
    @Override
    protected void doHook() {
        statusManager.status(StatusEnum.WAIT_SHUTDOWN.code());
        log.info("[Server Shutdown] set status to wait for shutdown.");

        // 取消注册（通知注册中心）
        log.info("[Server Shutdown] serverRegisterManager start destroy all link to registerCenter.");
        serverRegisterManager.unRegisterAll();
        // 清空相关的 channel 引用
        serverRegisterManager.clearRegisterChannel();
        log.info("[Server Shutdown] serverRegisterManager finish destroy all link to registerCenter.");

        long startMills = System.currentTimeMillis();
        while (invokeManager.remainsRequest()) {
            long currentMills = System.currentTimeMillis();
            long costMills = currentMills - startMills;
            if(costMills >= waitMillsForRemainRequest) {
                log.warn("[Server Shutdown] still remains request, but timeout, break.");
                break;
            }

            log.info("[Server Shutdown] still remains request, wait for a while.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.info("[Server Shutdown] resourceManager start destroy all resources.");
                this.resourceManager.destroyAll();
                log.info("[Server Shutdown] resourceManager finish destroy all resources.");
                // 设置状态为关闭成功
                statusManager.status(StatusEnum.SHUTDOWN_SUCCESS.code());
                log.info("[Server Shutdown] set status to shutdown success.");
            }
        }

    }
}
