package com.github.fk.common.support.invoke;

import com.github.fk.common.rpc.domain.RpcResponse;

public interface InvokeManager {
    /**
     * 添加请求信息
     * @param seqId 序列号
     * @param timeoutMills 超时时间
     * @return this
     * @since 0.0.6
     */
    InvokeManager addRequest(final String seqId,
                             final long timeoutMills);

    InvokeManager addResponse(final String seqId,final RpcResponse rpcResponse );

    RpcResponse getResponse(final String seqId);

    /**
     * 是否包含请求待处理
     * @return
     */
    boolean remainsRequest();
    /**
     * 移除请求和响应
     * @param seqId 唯一标识
     * @return this
     */
    InvokeManager removeReqAndResp(final String seqId);
}
