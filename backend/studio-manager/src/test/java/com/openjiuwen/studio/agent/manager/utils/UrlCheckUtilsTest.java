/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.UrlCheckUtils;

import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

/**
 * 功能描述
 *
 */
public class UrlCheckUtilsTest {
    private UrlCheckUtils urlCheckUtils;

    @BeforeEach
    public void setUp() throws Exception {
        urlCheckUtils = new UrlCheckUtils();
        ReflectionTestUtils.setField(urlCheckUtils, "enableUrlCheck", true);
        ReflectionTestUtils.setField(urlCheckUtils, "toolUrlBlackListStr", "string");
        ReflectionTestUtils.setField(urlCheckUtils, "toolUrlWhiteListStr", "string");
        ReflectionTestUtils.setField(urlCheckUtils, "toolUrlBlackList", new ArrayList<>());
        ReflectionTestUtils.setField(urlCheckUtils, "toolUrlWhiteList", new ArrayList<>());
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void test_check_url_should_void_when_test_data_combination() throws Exception {
        try (MockedStatic<Strings> mockedStaticStringUtils = mockStatic(Strings.class, RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticStringUtils.when(() -> Strings.CS.equals(anyString(), anyString())).thenReturn(true);

            assertThrows(AgentStudioException.class, () -> {
                urlCheckUtils.checkUrl("string", "string");
            });
        }
    }
}
