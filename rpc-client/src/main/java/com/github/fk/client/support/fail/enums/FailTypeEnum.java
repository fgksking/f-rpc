package com.github.fk.client.support.fail.enums;

public enum FailTypeEnum {
    FAIL_FAST(1),
    /**
     * 失败重试
     * 选择另外一个 channel 进行重试
     * @since 0.1.1
     */
    FAIL_OVER(2),;
    private final int code;

    FailTypeEnum(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    @Override
    public String toString() {
        return "FailTypeEnum{" +
                "code=" + code +
                '}';
    }
}
