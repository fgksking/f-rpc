package com.github.fk.client.proxy;

public interface RemoteInvokeService {
    /**
     * 远程调用
     * @param context 远程调用上下文
     * @return 最终调用结果
     */
    Object remoteInvoke(final RemoteInvokeContext context);
}
