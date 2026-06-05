package com.openjiuwen.studio.agent.space.common.utils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 获取SpringContext中的Bean
 *
 */
@Slf4j
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext sContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        sContext = context;
    }

    /**
     * 获取容器中的实例
     *
     * @param name 注入在spring容器中的Bean的name，默认为类名称首字母小写
     * @param clazz 获取的实例类的Class
     * @return 容器中初始化实例 or null
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        if (null != sContext) {
            return sContext.getBean(name, clazz);
        }

        return null;
    }

    /**
     * 获取容器中的实例
     *
     * @param klass 获取的实例类的Class
     * @return 容器中初始化实例 or null
     */
    public static <T> T getBean(Class<T> klass) {
        return sContext.getBean(klass);
    }

    /**
     * 获取容器中的实例
     *
     * @param name 注入在spring容器中的Bean的name
     * @return 容器中初始化实例 or null
     */
    public static Object getBean(String name) {
        return sContext.getBean(name);
    }

    public static ApplicationContext getContext() {
        return sContext;
    }
}
