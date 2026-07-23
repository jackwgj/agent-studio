/*
 * 企业数字人智能平台 · SSO 集成
 * JWT 认证过滤器配置：仅当 auth.provider=jwt 时启用，替换华为 IAM/SAML。
 * 过滤器 Order=Integer.MIN_VALUE，早于 RuntimeContextFilter(Integer.MIN_VALUE+1)，
 * 确保模型调用前请求上下文已就绪。
 */

package com.openjiuwen.studio.agent.runtime.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 认证过滤器配置：仅当 auth.provider=jwt 时注册 JwtAuthFilter。
 */
@Configuration
@ConditionalOnProperty(name = "auth.provider", havingValue = "jwt")
public class JwtAuthConfig {

    @Value("${auth.jwt-secret:}")
    private String jwtSecret;

    @Value("${auth.jwt-issuer:}")
    private String jwtIssuer;

    @Value("${auth.jwt-enforce:false}")
    private boolean enforce;

    @Value("${poc.user.default-user-id:testUser}")
    private String fallbackUserId;

    @Value("${poc.user.default-project-id:0}")
    private String fallbackProjectId;

    @Value("${poc.user.default-domain-id:0}")
    private String fallbackDomainId;

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration() {
        JwtAuthFilter filter = new JwtAuthFilter(jwtSecret, jwtIssuer, enforce,
                fallbackUserId, fallbackProjectId, fallbackDomainId);
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.addUrlPatterns("/*");
        bean.setOrder(Integer.MIN_VALUE);
        bean.setName("jwtAuthFilter");
        return bean;
    }
}
