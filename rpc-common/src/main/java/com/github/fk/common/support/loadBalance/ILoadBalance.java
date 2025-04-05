package com.github.fk.common.support.loadBalance;

public interface ILoadBalance {
     IServer select(ILoadBalanceContext context);

}
