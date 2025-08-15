package com.github.fk.common.support.status;

public interface StatusManager {

    int status();
    StatusManager status(final int code);
}
