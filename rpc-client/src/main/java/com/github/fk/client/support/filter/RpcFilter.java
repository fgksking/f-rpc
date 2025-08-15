package com.github.fk.client.support.filter;

import com.github.fk.client.proxy.RemoteInvokeContext;

public interface RpcFilter {
    void filter(final RemoteInvokeContext context);
}
