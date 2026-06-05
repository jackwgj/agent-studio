/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {
    HttpClientAutoConfiguration.class // 关闭spring自带的http配置
})
@EnableRetry
@EnableScheduling
@EnableFeignClients(value = {"com.openjiuwen", "com.openjiuwen.studio.agent.space"})
@ComponentScan(basePackages = {"com.openjiuwen.studio.agent.space"})
@MapperScan(value = {"com.openjiuwen.studio.agent.space.dao"})
@ConfigurationPropertiesScan(basePackages = {"com.openjiuwen.studio.agent.space.app"})
@EnableTransactionManagement
@ServletComponentScan(basePackages = {"com.openjiuwen.studio.agent.space"})
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class AgentSpaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentSpaceApplication.class, args);
    }
}
