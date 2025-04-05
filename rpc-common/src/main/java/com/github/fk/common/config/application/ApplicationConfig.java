package com.github.fk.common.config.application;

public interface ApplicationConfig {

    /**
     * @return 应用名称
     *
     */
    String name();
    /**
     * @return 环境名称
     * dev test pre_prod prod
     *
     */
    String env();
}
