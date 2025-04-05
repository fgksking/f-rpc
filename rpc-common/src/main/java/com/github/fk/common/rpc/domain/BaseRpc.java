package com.github.fk.common.rpc.domain;

import java.io.Serializable;

public interface BaseRpc extends Serializable {
    String seqId();

    BaseRpc seqId(final  String traceId);
}
