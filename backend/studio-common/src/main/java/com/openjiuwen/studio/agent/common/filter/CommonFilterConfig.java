/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.filter;

import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * languagefilter注册类
 *
 */

@Configuration
public class CommonFilterConfig {

    @Bean
    public FilterRegistrationBean<RequestHeaderHolderUtils> requestLanguageFilter() {
        FilterRegistrationBean<RequestHeaderHolderUtils> filterRegistrationBean
            = new FilterRegistrationBean<RequestHeaderHolderUtils>();
        filterRegistrationBean.setFilter(new RequestHeaderHolderUtils());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(Integer.MIN_VALUE); // order的数值越小 则优先级越高
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnProperty(name = "tenant.log.filter.enabled", havingValue = "true")
    public FilterRegistrationBean<AccessLogFilter> accessLogFilter(I18nUtil i18nUtil) {
        FilterRegistrationBean<AccessLogFilter> filterRegistrationBean
            = new FilterRegistrationBean<>();
        AccessLogFilter filter = new AccessLogFilter();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(Integer.MIN_VALUE);
        return filterRegistrationBean;
    }
}
