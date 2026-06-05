/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.auth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class IamAuthAdapterTest {
    @InjectMocks
    private IamAuthAdapter iamAuthAdapter;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_requestHeaderHandler_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("not_empty");

            Map<String, String> headers = new HashMap<>();

            ProviderAuth auth = mock(ProviderAuth.class, Answers.RETURNS_DEEP_STUBS);

            // When
            Map<String, String> result = iamAuthAdapter.requestHeaderHandler("not_empty", "not_empty", new HashMap<>(),
                headers, "not_empty", auth);

            // Then
            assertNotNull(result);
            Assertions.assertEquals("not_empty", result.get("X-Auth-Token"));
        }
    }
}
