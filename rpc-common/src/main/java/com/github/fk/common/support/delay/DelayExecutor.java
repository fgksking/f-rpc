package com.github.fk.common.support.delay;

public interface DelayExecutor {

    void delay(long delayInMills, Runnable runnable);
}
