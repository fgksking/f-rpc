package com.github.fk.common.threadpoll.manager;

import com.github.fk.common.support.status.enums.Endpoint;

import java.util.concurrent.ExecutorService;

public interface ExecutorRepository {

    ExecutorService getExecutor(Endpoint endpoint);
    void shutdown();
}
