
package com.openjiuwen.studio.agent.space.app.filter.simple;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * filter注册类
 */
@Configuration
public class FilterConfig {
    @Bean
    @ConditionalOnProperty(name = "env.type", havingValue = "simple")
    public FilterRegistrationBean<SimpleAuthFilter> simpleAuthFilter() {
        FilterRegistrationBean<SimpleAuthFilter> filterRegistrationBean =
            new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new SimpleAuthFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(Integer.MIN_VALUE); // order的数值越小 则优先级越高
        return filterRegistrationBean;
    }

    @Bean
    @ConditionalOnProperty(name = "env.type", havingValue = "simple")
    public FilterRegistrationBean<SidToTokenFilter> filter() {
        FilterRegistrationBean<SidToTokenFilter> filterRegistrationBean =
            new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new SidToTokenFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(Integer.MIN_VALUE); // order的数值越小 则优先级越高
        return filterRegistrationBean;
    }
}
