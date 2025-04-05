package com.github.fk.common.constant;

public enum CallTypeEnum {
    /**
     * 单向调用：不关心调用的结果
     */
    ONE_WAY(1),

    /**
     * 同步调用：最常用的调用方式，关心结果
     */
    SYNC(2),

    /**
     * 异步调用：性能更高的调用方式，异步获取结果
     */
    ASYNC(3),

    /**
     * 回调方式：通过 callback 处理结果信息
     * @since 0.1.0
     */
    CALLBACK(4),
    ;

    private final int code;

    CallTypeEnum(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    @Override
    public String toString() {
        return "CallTypeEnum{" +
                "code=" + code +
                '}';
    }
}
