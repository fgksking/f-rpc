package com.github.fk.client.support.calltype.impl;

import com.github.fk.client.support.calltype.CallTypeStrategy;
import com.github.fk.common.constant.CallTypeEnum;

public class CallTypeStrategyFactory {

    private CallTypeStrategyFactory() {
    }

    public static CallTypeStrategy callTypeStrategy(final CallTypeEnum callTypeEnum) {
        switch (callTypeEnum) {
            case SYNC:
                return SyncCallTypeStrategy.getInstance();
            case ONE_WAY:
                return OneWayCallTypeStrategy.getInstance();
            default:
                throw new UnsupportedOperationException("Not support call type : " + callTypeEnum);
        }
    }
}
