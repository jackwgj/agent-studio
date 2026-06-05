/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * 对应空实现的测试，当实现接口时，将对应的Service删除，否则Assertions.assertNull会失败
 */
@Transactional
@Slf4j
class EmptyImplementTest extends BaseTest {

    @Test
    void test() {
        call(null);
    }

    @SneakyThrows
    private void call(Object o) {
        if (Objects.isNull(o)) {
            return;
        }
        Method[] methods = o.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                Assertions.assertNull(method.invoke(o, new Object[method.getParameterCount()]));
                log.info("invoke method: {}", method.getName());
            }
        }
    }
}
