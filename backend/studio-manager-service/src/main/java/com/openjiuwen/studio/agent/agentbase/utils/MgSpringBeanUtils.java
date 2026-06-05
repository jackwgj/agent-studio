/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.utils;

/**
 * spring bean工具类
 */

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class MgSpringBeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MgSpringBeanUtils.applicationContext = applicationContext;
    }

    /**
     * 根据bean的类型和名称获取bean的实例，当存在多个相同类型的bean时，可以通过名称指定
     *
     * @param beanName bean的名称
     * @param requiredType bean的类型
     * @return bean的实例，如果找不到则返回null
     */
    public static <T> T getBean(String beanName, Class<T> requiredType) {
        return applicationContext.getBean(beanName, requiredType);
    }

    /**
     * 根据bean的类型获取bean的实例，如果存在多个相同类型的bean，则抛出异常
     *
     * @param requiredType bean的类型
     * @return bean的实例
     */
    public static <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    /**
     * 获取配置数据
     *
     * @param key 配置key
     * @param defValue 默认值
     * @return
     */
    public static String getProperty(String key, String defValue) {
        return applicationContext.getEnvironment().getProperty(key, defValue);
    }
}
