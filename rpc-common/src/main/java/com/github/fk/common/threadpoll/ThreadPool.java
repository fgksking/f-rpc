package com.github.fk.common.threadpoll;

import java.util.concurrent.Executor;

public interface ThreadPool {
    Executor getExecutor();
}
