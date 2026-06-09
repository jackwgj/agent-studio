/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml.filter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 认证过滤器配置类 - 专门用于注册认证相关的过滤器
 */
@Configuration
@ConditionalOnProperty(name = "saml.enabled", havingValue = "true")
@Slf4j
public class AuthFilterConfig {

    @PostConstruct
    public void init() {
        log.info("=== AuthFilterConfig init finish  ===");
    }

    /**
     * 注册SAML认证过滤器为最高优先级
     * 用于拦截未认证请求并重定向到SAML IDP
     */
    @Bean
    public FilterRegistrationBean<SamlAuthFilter> samlAuthFilterRegistration(SamlAuthFilter samlAuthFilter) {
        FilterRegistrationBean<SamlAuthFilter> registrationBean = new FilterRegistrationBean<>();
        log.info("=== SamlAuthFilterConfig init finish  ===");
        registrationBean.setFilter(samlAuthFilter);
        registrationBean.addUrlPatterns("/*");

        registrationBean.setOrder(Integer.MIN_VALUE);

        registrationBean.setName("samlAuthFilter");

        return registrationBean;
    }

    /**
     * 注册会话过滤器为次高优先级
     * 用于会话校验、刷新和设置x-auth-token头
     */
    @Bean
    public FilterRegistrationBean<SessionFilter> sessionFilterRegistration(SessionFilter sessionFilter) {
        FilterRegistrationBean<SessionFilter> registrationBean = new FilterRegistrationBean<>();
        log.info("=== SessionFilterConfig init finish  ===");
        registrationBean.setFilter(sessionFilter);
        registrationBean.addUrlPatterns("/*");

        // 在SAML认证过滤器之后执行
        registrationBean.setOrder(Integer.MIN_VALUE + 1);

        registrationBean.setName("sessionFilter");

        return registrationBean;
    }
}
