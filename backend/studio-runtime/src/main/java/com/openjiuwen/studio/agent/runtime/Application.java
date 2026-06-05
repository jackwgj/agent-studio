/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Starter
 *
 */
@SpringBootApplication()
@ImportAutoConfiguration()
@ComponentScan(value = {"com.openjiuwen.studio.agent.common",
    "com.openjiuwen.studio.agent.runtime"})
@EnableScheduling
@EnableAspectJAutoProxy
@MapperScan(value = {"com.openjiuwen.studio.agent.runtime.mapper"})
@EnableFeignClients(value = {"com.openjiuwen.studio.agent.runtime", "com.openjiuwen.studio.agent.common"})
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
