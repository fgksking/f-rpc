package com.github.fk.common.exception;

public class RpcRuntimeException extends RuntimeException{

    private static final long serialVersionUID = -2521814477982789832L;

    public RpcRuntimeException() {
    }

    public RpcRuntimeException(String message) {
        super(message);
    }

    public RpcRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    public RpcRuntimeException(Throwable cause) {
        super(cause);
    }
}
