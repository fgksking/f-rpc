package com.github.fk.client.support.fail.impl;

import com.github.fk.client.support.fail.FailStrategy;
import com.github.fk.client.support.fail.enums.FailTypeEnum;

public class FailStrategyFactory {

    public static FailStrategy failStrategy(final FailTypeEnum failTypeEnum) {
        switch (failTypeEnum) {
            case FAIL_FAST:
                return new FailFastStrategy();
            case FAIL_OVER:
                return new FailOverStrategy();
            default:
                throw new UnsupportedOperationException("not support fail type " + failTypeEnum);
        }
    }

}
