package com.github.fk.common.support.inteceptor.impl;

import com.github.fk.common.support.loadBalance.ILoadBalance;
import com.github.fk.common.support.loadBalance.ILoadBalanceContext;
import com.github.fk.common.support.loadBalance.IServer;
import com.github.fk.common.support.loadBalance.impl.RandomLoadBalance;

public class LoadBalances {

    public static ILoadBalance roundRobbin(){
        return new RandomLoadBalance();
    }
}
