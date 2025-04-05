package com.github.fk.common.support.loadBalance.impl;

import com.github.fk.common.support.loadBalance.IServer;

public class Server implements IServer {
    private String url;

    private int weight;
    public static Server newInstance() {
        return new Server();
    }
    @Override
    public String url() {
        return url;
    }

    public Server url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public int weight() {
        return weight;
    }

    public Server weight(int weight) {
        this.weight = weight;
        return this;
    }

    @Override
    public String toString() {
        return "Server{" +
                "url='" + url + '\'' +
                ", weight=" + weight +
                '}';
    }
}
