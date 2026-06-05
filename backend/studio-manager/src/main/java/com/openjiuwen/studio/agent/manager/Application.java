/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager;

import com.openjiuwen.studio.prompt.engineering.utils.WebMvcConfig;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Starter
 *
 */
@SpringBootApplication
@ComponentScan(value = {"com.openjiuwen.studio.agent.manager", "com.openjiuwen.studio",
    "com.openjiuwen.studio.agent.common", "com.openjiuwen.studio.prompt.engineering", "com.openjiuwen.studio.common"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class)})
@MapperScan(value = {
    "com.openjiuwen.studio.agent.manager.mapper", "com.openjiuwen.studio.agent.agentbase.mapper",
    "com.openjiuwen.studio.prompt.engineering.mapper", "com.openjiuwen.studio.common.service.mapper"
})
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(value = {"com.openjiuwen.studio.agent.manager", "com.openjiuwen.studio.agent.common","com.openjiuwen.studio.agent.agentbase.client"})
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    /**
     * springboot main
     *
     * @param args params
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        log.info("Swagger-UI can be opened at: swagger-ui");
    }
}
