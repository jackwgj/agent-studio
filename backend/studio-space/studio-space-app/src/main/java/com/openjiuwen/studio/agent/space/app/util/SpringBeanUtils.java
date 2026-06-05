
package com.openjiuwen.studio.agent.space.app.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 功能描述
 */
@Component
@Order(100)
public class SpringBeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

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
     * @param key      配置key
     * @param defValue 默认值
     * @return 属性值
     */
    public static String getProperty(String key, String defValue) {
        return applicationContext.getEnvironment().getProperty(key, defValue);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.applicationContext = applicationContext;
    }
}
