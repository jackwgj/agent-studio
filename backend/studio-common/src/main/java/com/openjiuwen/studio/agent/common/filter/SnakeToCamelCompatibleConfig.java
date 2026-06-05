/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局 Query 参数 下划线 转 驼峰 配置
 * 优先级最低，最后执行，不影响任何过滤器
 */
@Configuration
public class SnakeToCamelCompatibleConfig {

    @Bean
    public FilterRegistrationBean<SnakeToCamelCompatibleFilter> snakeToCamelFilter() {
        FilterRegistrationBean<SnakeToCamelCompatibleFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SnakeToCamelCompatibleFilter());
        registration.addUrlPatterns("/*");
        registration.setName("snakeToCamelCompatibleFilter");

        // 🔥 最低优先级，最后执行
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }

    public static class SnakeToCamelCompatibleFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
            HttpServletRequest req = (HttpServletRequest) request;

            // 包装请求：保留原有参数 + 额外添加驼峰参数
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(req) {
                private final Map<String, String[]> combinedParams = combineParams(req.getParameterMap());

                // 同时保留下划线 + 驼峰
                private Map<String, String[]> combineParams(Map<String, String[]> original) {
                    Map<String, String[]> result = new HashMap<>(original);

                    for (Map.Entry<String, String[]> entry : original.entrySet()) {
                        String key = entry.getKey();
                        if (key.contains("_")) {
                            String camelKey = toCamelCase(key);
                            result.computeIfAbsent(camelKey, k -> entry.getValue());
                        }
                    }
                    return result;
                }

                @Override
                public String getParameter(String name) {
                    String[] values = combinedParams.get(name);
                    return values != null && values.length > 0 ? values[0] : super.getParameter(name);
                }

                @Override
                public String[] getParameterValues(String name) {
                    return combinedParams.getOrDefault(name, super.getParameterValues(name));
                }

                @Override
                public Map<String, String[]> getParameterMap() {
                    return combinedParams;
                }

                @Override
                public Enumeration<String> getParameterNames() {
                    return Collections.enumeration(combinedParams.keySet());
                }
            };

            try {
                chain.doFilter(wrapper, response);
            } catch (Exception ignored) {}
        }

        // 下划线转驼峰
        private String toCamelCase(String str) {
            if (str == null || !str.contains("_")) {
                return str;
            }
            StringBuilder sb = new StringBuilder();
            boolean nextUpper = false;
            for (char c : str.toCharArray()) {
                if (c == '_') {
                    nextUpper = true;
                } else {
                    sb.append(nextUpper ? Character.toUpperCase(c) : c);
                    nextUpper = false;
                }
            }
            return sb.toString();
        }
    }
}
