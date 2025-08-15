package com.github.fk.common.support.loadBalance.impl;

import com.github.fk.common.support.loadBalance.ILoadBalance;
import com.github.fk.common.support.loadBalance.ILoadBalanceContext;
import com.github.fk.common.support.loadBalance.IServer;

import java.util.List;

public abstract class AbstractLoadBalance implements ILoadBalance {

    @Override
    public IServer select(ILoadBalanceContext context) {
        List<IServer> servers = context.servers();

        if(servers.size() <= 1) {
            return servers.get(0);
        }
        return doSelect(context);
    }

    protected abstract IServer doSelect(final ILoadBalanceContext context);
}
