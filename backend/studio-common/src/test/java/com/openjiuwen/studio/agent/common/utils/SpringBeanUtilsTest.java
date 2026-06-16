/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringBeanUtilsTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Environment environment;

    @BeforeEach
    void setUp() {
        SpringBeanUtils springBeanUtils = new SpringBeanUtils();
        springBeanUtils.setApplicationContext(applicationContext);
    }

    @Test
    void testGetBeanByNameAndType() {
        when(applicationContext.getBean("myBean", String.class)).thenReturn("hello");
        String result = SpringBeanUtils.getBean("myBean", String.class);
        assertEquals("hello", result);
    }

    @Test
    void testGetBeanByType() {
        when(applicationContext.getBean(String.class)).thenReturn("world");
        String result = SpringBeanUtils.getBean(String.class);
        assertEquals("world", result);
    }

    @Test
    void testGetProperty() {
        when(applicationContext.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("key", "default")).thenReturn("value");
        String result = SpringBeanUtils.getProperty("key", "default");
        assertEquals("value", result);
    }

    @Test
    void testGetProperty_DefaultValue() {
        when(applicationContext.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("missing", "fallback")).thenReturn("fallback");
        String result = SpringBeanUtils.getProperty("missing", "fallback");
        assertEquals("fallback", result);
    }
}
