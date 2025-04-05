package com.github.fk.common.support.resource.impl;

import com.github.fk.common.api.Destroyable;
import com.github.fk.common.support.resource.ResourceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
@Slf4j
public class DefaultResourceManager implements ResourceManager {

    private List<Destroyable> destroyableList = new ArrayList<>();
    @Override
    public synchronized ResourceManager addDestroy(Destroyable destroyable) {
        log.info("[Resource] add destroyable: {}", destroyable);
        destroyableList.add(destroyable);
        return this;
    }

    @Override
    public synchronized ResourceManager destroyAll() {
        log.info("[Resource] destroyableList.size(): {}", destroyableList.size());

        // 依次销毁
        for(Destroyable destroyable : destroyableList) {
            log.info("[Resource] destroy destroyable: {}", destroyable);
            destroyable.destroy();
        }

        // 清空列表
        log.info("[Resource] clear destroyableList");
        this.destroyableList = new ArrayList<>();
        return this;
    }
 }
