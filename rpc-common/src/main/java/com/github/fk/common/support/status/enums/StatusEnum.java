package com.github.fk.common.support.status.enums;

import com.github.fk.common.exception.ShutdownException;

public enum StatusEnum {
    INIT(0),


    ENABLE(1),


    WAIT_SHUTDOWN(2),

    SHUTDOWN_SUCCESS(3),

    SHUTDOWN_TIMEOUT(4),
    ;


    /**
     * 编码信息
     */
    private final int code;

    StatusEnum(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }


    /**
     * 断言可用
     * @param statusCode 状态码
     * @see .ShutdownException 关闭异常
     * @since 0.1.4
     */
    public static void assertEnable(final int statusCode) {
        if(StatusEnum.ENABLE.code() != statusCode) {
            throw new ShutdownException("Status is not enable to send request, may be during shutdown.");
        }
    }
}
