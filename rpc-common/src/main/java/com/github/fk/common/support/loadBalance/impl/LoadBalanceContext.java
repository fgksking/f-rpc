package com.github.fk.common.support.loadBalance.impl;

import com.github.fk.common.support.loadBalance.ILoadBalanceContext;
import com.github.fk.common.support.loadBalance.IServer;

import java.util.List;

public class LoadBalanceContext implements ILoadBalanceContext {
    private String hashKey;

    /**
     * 服务端列表
     * @since 0.0.3
     */
    private List<IServer> servers;
    public static LoadBalanceContext newInstance() {
        return new LoadBalanceContext();
    }

    @Override
    public String hashKey() {
        return hashKey;
    }

    public LoadBalanceContext hashKey(String hashKey) {
        this.hashKey = hashKey;
        return this;
    }

    @Override
    public List<IServer> servers() {
        return servers;
    }

    public LoadBalanceContext servers(List<IServer> servers) {
        this.servers = servers;
        return this;
    }

    @Override
    public String toString() {
        return "LoadBalanceContext{" +
                "hashKey='" + hashKey + '\'' +
                ", servers=" + servers +
                '}';
    }

}
