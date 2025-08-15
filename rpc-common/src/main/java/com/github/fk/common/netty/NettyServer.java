package com.github.fk.common.netty;

import com.github.fk.common.api.AsynRunnable;
import com.github.fk.common.api.Destroyable;
import com.github.fk.common.api.Initializable;
/**
 * 服务端接口，实现多个接口来规范（替换掉方法）
 */
public interface NettyServer extends AsynRunnable, Initializable, Destroyable,Runnable {

}
