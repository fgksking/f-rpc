package com.github.fk.common.support.hook;

public abstract class AbstractShutdownHook implements RpcShutdownHook{
    @Override
    public void hook() {
        System.out.println("{ShoutDownHook start}");
        this.doHook();
        System.out.println("{ShoutDownHook end}");
    }

    protected abstract void doHook();
}
