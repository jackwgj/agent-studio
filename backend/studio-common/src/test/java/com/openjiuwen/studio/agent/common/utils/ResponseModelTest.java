/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * 功能描述
 *
 */
public class ResponseModelTest {
    @Test
    void failMsg_Response() {
        // 测试正常情况
        ResponseEntity<Object> response = ResponseModel.failMsg("Error Message");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void failed_ResponseEntity_DefaultError() {
        // 测试无参数情况
        ResponseEntity<Map<String, String>> response = ResponseModel.failed();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void failed_ResponseEntity_CustomError() {
        // 测试带参数情况
        ResponseEntity<Map<String, String>> response = ResponseModel.failed("404", "Not Found");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertEquals("404", body.get("status"));
        assertEquals("Not Found", body.get("message"));
    }

    @Test
    void num2HttpStatus_InvalidCode() {
        // 测试无效状态码：传入不在HttpStatus定义范围内的状态码
        HttpStatus expected = HttpStatus.NOT_FOUND;
        HttpStatus result = ResponseModel.num2HttpStatus("999");
        assertEquals(expected, result);
    }

    @Test
    void num2HttpStatus_MinAndMaxValues() {
        // 测试边界值：传入HttpStatus定义的最小和最大值
        Assertions.assertEquals(HttpStatus.CONTINUE, ResponseModel.num2HttpStatus("100"));
        Assertions.assertEquals(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED, ResponseModel.num2HttpStatus("511"));
    }
}
