package com.github.fk.remote.netty;

import com.github.fk.api.AsynRunnable;
import com.github.fk.api.Destroyable;
import com.github.fk.api.Initializable;
/**
 * 服务端接口，实现多个接口来规范（替换掉方法）
 */
public interface NettyServer extends AsynRunnable, Initializable, Destroyable {

}
