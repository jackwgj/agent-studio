/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.config;

import com.github.pagehelper.PageInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 功能描述 mybatis plugin config
 *
 */
@Configuration
public class MybatisConfiguration {
    /**
     * PageHelper plugin config
     *
     * @return PageInterceptor instance
     */
    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("reasonable", Boolean.TRUE.toString());
        pageInterceptor.setProperties(properties);

        return pageInterceptor;
    }
}
