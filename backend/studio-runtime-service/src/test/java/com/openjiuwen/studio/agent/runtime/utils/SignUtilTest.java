/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.runtime.model.security.ScasTextIdentifyRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.SortedMap;
import java.util.TreeMap;

@MockitoSettings(strictness = Strictness.LENIENT)
class SignUtilTest {
    private AutoCloseable mockitoCloseable;

    private static MockedStatic<SpringBeanUtils> springBeanUtilsMockedStatic;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        springBeanUtilsMockedStatic = Mockito.mockStatic(SpringBeanUtils.class);
        springBeanUtilsMockedStatic.when(() -> SpringBeanUtils.getProperty(anyString(), anyString()))
            .thenReturn("HmacSHA256");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
        springBeanUtilsMockedStatic.close();
    }

    @Test
    void test_signGet_should_equal_result1() throws Exception {
        ScasTextIdentifyRequest scasTextIdentifyRequest = new ScasTextIdentifyRequest();
        scasTextIdentifyRequest.setTaskID("not_empty");

        String result = SignUtil.signGet("POST", "/scas/v1/textIndentify", null,
            JsonUtils.toJson(scasTextIdentifyRequest),
            Long.toString(System.currentTimeMillis()), "scastest2",
            "xxxxxxxxxx");

        // Then
        assertNotNull(result);
    }

    @Test
    void testToString() {
        SortedMap<String, String> params = new TreeMap<>();
        params.put("key1", "value1");
        SignUtil.toString(params);
    }
}
