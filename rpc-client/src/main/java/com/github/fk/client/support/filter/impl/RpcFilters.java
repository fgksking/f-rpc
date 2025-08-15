package com.github.fk.client.support.filter.impl;

import com.github.fk.client.support.filter.RpcFilter;

public class RpcFilters {
    public static RpcFilter none() {
        return new NoneRpcFilter();
    }
}
