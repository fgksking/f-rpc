package com.github.fk.common.support.loadBalance.impl;

import com.github.fk.common.support.loadBalance.ILoadBalanceContext;
import com.github.fk.common.support.loadBalance.IServer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class RandomLoadBalance extends AbstractLoadBalance{
    private final AtomicLong indexHolder = new AtomicLong();
    @Override
    protected IServer doSelect(ILoadBalanceContext context) {
        List<IServer> servers = context.servers();

        long index = indexHolder.getAndIncrement();
        int actual = (int) (index % servers.size());
        return servers.get(actual);
    }
}
