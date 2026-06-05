/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * 功能描述
 *
 */
public class ResponseUtilsTest {
    @Test
    public void testAddAndGetHeader() {
        // 测试添加一个头参数
        ResponseUtils.addHeader("Content-Type", "application/json");
        HttpHeaders headers = ResponseUtils.getHeaders();
        assertNotNull(headers);
        assertEquals("application/json", headers.getFirst("Content-Type"));
    }

    @Test
    public void testAddMultipleHeaders() {
        // 测试添加多个不同的头参数
        ResponseUtils.addHeader("Content-Type", "application/json");
        ResponseUtils.addHeader("Authorization", "Bearer token");
        HttpHeaders headers = ResponseUtils.getHeaders();
        assertNotNull(headers);
        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertEquals("Bearer token", headers.getFirst("Authorization"));
    }

    @Test
    public void testSetAndGetResponseCode() {
        // 测试设置和获取状态码
        ResponseUtils.setResponseCode(HttpStatus.OK);
        HttpStatus status = ResponseUtils.getResponseCode();
        assertEquals(HttpStatus.OK, status);
    }

    @Test
    public void testSetResponseCodeMultipleTimes() {
        // 测试多次设置状态码
        ResponseUtils.setResponseCode(HttpStatus.OK);
        ResponseUtils.setResponseCode(HttpStatus.NOT_FOUND);
        HttpStatus status = ResponseUtils.getResponseCode();
        assertEquals(HttpStatus.NOT_FOUND, status);
    }

    @Test
    public void testConcurrentSetStatusCode() throws InterruptedException {
        // 创建多个线程设置状态码
        Thread thread1 = new Thread(() -> {
            ResponseUtils.setResponseCode(HttpStatus.OK);
        });

        Thread thread2 = new Thread(() -> {
            ResponseUtils.setResponseCode(HttpStatus.NOT_FOUND);
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // 获取状态码
        HttpStatus status = ResponseUtils.getResponseCode();

        // 验证状态码是否为最后一个设置的值
        assertEquals(HttpStatus.NOT_FOUND, status);
    }
}
