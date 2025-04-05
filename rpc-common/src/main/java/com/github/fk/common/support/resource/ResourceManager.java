package com.github.fk.common.support.resource;

import com.github.fk.common.api.Destroyable;

public interface ResourceManager {
    /**
     * 新增可销毁的资源信息
     * @param destroyable 可销毁的资源信息
     * @return this
     */
    ResourceManager addDestroy(final Destroyable destroyable);

    /**
     * 销毁所有资源
     * （1）销毁所有的列表资源
     * （2）清空可销毁的列表
     * @return this
     */
    ResourceManager destroyAll();
}
