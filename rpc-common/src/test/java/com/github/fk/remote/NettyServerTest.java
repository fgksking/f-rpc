package com.github.fk.remote;

import com.github.fk.remote.netty.impl.DefaultNettyServer;
import org.junit.Test;

public class NettyServerTest {
    @Test
    public void runOnce(){
        DefaultNettyServer server = new DefaultNettyServer(8080,null);
        server.asyn();
        server.asyn();
        server.asyn();
        System.out.println("runOnce");
    }

    public static void main(String[] args) {
        DefaultNettyServer server = new DefaultNettyServer(8080,null);
        server.asyn();
        server.asyn();
        server.asyn();
        System.out.println("runOnce");
    }
}
