package com.github.fk.common.support.delay;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.DelayQueue;
@Slf4j
public class DelayQueueExecutor implements DelayExecutor{

    private final DelayQueue<DelayElem> delayQueue;

    public DelayQueueExecutor() {
        delayQueue = new DelayQueue<>();

        // 读取线程提前开始
        ReadThread readThread = new ReadThread(delayQueue);
        readThread.start();
    }

    @Override
    public void delay(long delayInMills, Runnable runnable) {
        log.info("开始添加延迟 {}ms 的可运行对象", delayInMills);
        DelayElem delayElem = new DelayElem(delayInMills, runnable);
        delayQueue.add(delayElem);
        log.info("完成添加延迟 {}ms 的可运行对象", delayInMills);
    }

    /**
     * 读取线程
     */
    private static class ReadThread extends Thread {
        private final DelayQueue<DelayElem> delayQueue;
        private ReadThread(DelayQueue<DelayElem> delayQueue) {
            this.delayQueue = delayQueue;
        }

        @Override
        public void run() {
            while (true){
                try {
                    DelayElem element = delayQueue.take();
                    long delayInMills = element.delayMills();
                    log.info("开始获取延迟 {}ms 的可运行对象", delayInMills);
                    Runnable runnable = element.msg();
                    runnable.run();
                    log.info("完成获取延迟 {}ms 的可运行对象", delayInMills);
                } catch (InterruptedException e) {
                    log.error("延迟任务执行遇到异常", e);
                }
            }
        }
    }
}
