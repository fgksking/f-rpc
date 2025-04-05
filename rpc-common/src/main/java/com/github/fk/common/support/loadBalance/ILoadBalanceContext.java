package com.github.fk.common.support.loadBalance;

import java.util.List;

public interface ILoadBalanceContext {

    List<IServer> servers();
    String hashKey();
}
