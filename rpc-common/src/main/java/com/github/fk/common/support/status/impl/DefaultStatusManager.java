package com.github.fk.common.support.status.impl;

import com.github.fk.common.support.status.StatusManager;
import com.github.fk.common.support.status.enums.StatusEnum;

public class DefaultStatusManager implements StatusManager {
    private volatile int status = StatusEnum.ENABLE.code();

    @Override
    public int status() {
        return status;
    }

    @Override
    public DefaultStatusManager status(int status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultStatusManager{" +
                "status=" + status +
                '}';
    }
}
