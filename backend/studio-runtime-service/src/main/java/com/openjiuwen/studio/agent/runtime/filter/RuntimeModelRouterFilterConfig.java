/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RuntimeModelRouterFilterConfig {
    @Value("${integration.auth.enabled:true}")
    private boolean iamAuthEnable;

    @Bean("runtimeModelRouterFilter")
    public FilterRegistrationBean<RuntimeContextFilter> createRuntimeContextFilter() {
        FilterRegistrationBean<RuntimeContextFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new RuntimeContextFilter(iamAuthEnable));
        filterRegistrationBean.addUrlPatterns("/v1/*");
        filterRegistrationBean.addUrlPatterns("/v2/*");
        filterRegistrationBean.setOrder(Integer.MIN_VALUE + 1); // order的数值越小 则优先级越高
        return filterRegistrationBean;
    }
}
