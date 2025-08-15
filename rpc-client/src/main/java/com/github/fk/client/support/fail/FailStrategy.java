package com.github.fk.client.support.fail;

import com.github.fk.client.proxy.RemoteInvokeContext;

public interface FailStrategy {

    Object fail(final RemoteInvokeContext context);
}
