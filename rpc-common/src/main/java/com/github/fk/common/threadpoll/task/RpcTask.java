package com.github.fk.common.threadpoll.task;

/**
 * RPC任务接口
 * 用于封装RPC处理逻辑，便于在线程池中执行
 */
public interface RpcTask extends Runnable {

    /**
     * 获取任务名称
     */
    String getTaskName();

    /**
     * 获取任务优先级
     */
    int getPriority();

    /**
     * 任务执行前的处理
     */
    default void beforeExecute() {
        // 默认空实现
    }

    /**
     * 任务执行后的处理
     */
    default void afterExecute() {
        // 默认空实现
    }

    /**
     * 任务执行异常的处理
     */
    default void onException(Throwable throwable) {
        // 默认空实现
    }
}