package com.github.fk.common.threadpoll.manager;

import com.github.fk.common.config.application.ApplicationConfig;
import com.github.fk.common.support.status.enums.Endpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

//默认线程池，处理消息的接收与发送
//使异步执行，封装成任务
@Slf4j
public class DefaultExecutorRepository implements ExecutorRepository {

    // 服务端线程池 - 处理服务端业务逻辑
    private volatile ExecutorService serverExecutor;

    // consumer端线程池 - 处理客户端业务逻辑
    private volatile ExecutorService consumerExecutor;

    // 定时任务线程池
    private volatile ScheduledExecutorService scheduledExecutor;

    private final ConcurrentMap<String, ExecutorService> data = new ConcurrentHashMap<>();

    private final Object LOCK = new Object();

    private ApplicationConfig applicationConfig;

    public DefaultExecutorRepository(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
        initExecutors();
    }

    /**
     * 初始化线程池
     */
    private void initExecutors() {
        // 服务端线程池 - 处理RPC请求
        serverExecutor = new ThreadPoolExecutor(
                10, 50, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new NamedThreadFactory("rpc-server-executor"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        // 客户端线程池 - 处理RPC响应
        consumerExecutor = new ThreadPoolExecutor(
                5, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new NamedThreadFactory("rpc-consumer-executor"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        // 定时任务线程池
        scheduledExecutor = Executors.newScheduledThreadPool(2, new NamedThreadFactory("rpc-scheduled-executor"));

        // 注册到map中
        data.put(Endpoint.SERVER.name(), serverExecutor);
        data.put(Endpoint.CONSUMER.name(), consumerExecutor);
    }

    @Override
    public ExecutorService getExecutor(Endpoint endpoint) {
        return data.get(endpoint.name());
    }

    /**
     * 获取服务端线程池
     */
    public ExecutorService getServerExecutor() {
        return serverExecutor;
    }

    /**
     * 获取客户端线程池
     */
    public ExecutorService getConsumerExecutor() {
        return consumerExecutor;
    }

    /**
     * 获取定时任务线程池
     */
    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    @Override
    public void shutdown() {
        log.info("开始关闭线程池...");

        if (serverExecutor != null && !serverExecutor.isShutdown()) {
            serverExecutor.shutdown();
            try {
                if (!serverExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    serverExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                serverExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (consumerExecutor != null && !consumerExecutor.isShutdown()) {
            consumerExecutor.shutdown();
            try {
                if (!consumerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    consumerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                consumerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (scheduledExecutor != null && !scheduledExecutor.isShutdown()) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("线程池关闭完成");
    }

    /**
     * 命名线程工厂
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
