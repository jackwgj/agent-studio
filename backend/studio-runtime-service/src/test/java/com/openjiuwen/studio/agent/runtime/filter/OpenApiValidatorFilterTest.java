/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.runtime.service.ObsService;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class OpenApiValidatorFilterTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsService obsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private I18nUtil i18nUtil;

    @InjectMocks
    private OpenApiValidatorFilter openApiValidatorFilter;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        openApiValidatorFilter.afterPropertiesSet();
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(openApiValidatorFilter, "redisClient", redisClient);
        ReflectionTestUtils.setField(openApiValidatorFilter, "i18nUtil", i18nUtil);
        ReflectionTestUtils.setField(openApiValidatorFilter, "obsService", obsService);
        ReflectionTestUtils.setField(openApiValidatorFilter, "isApiKeyCheckEnabled", true);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_doFilter_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                    mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<CryptoUtils> mockedStaticSccCryptoUtils =
                    mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                Map<String, Object> map = new HashMap<>();
                map.put("userId", "123");
                map.put("code", "1");
                mockedStaticJsonUtils.when(() -> JsonUtils.json2Obj(anyString(), eq(Constants.MAP_TYPE_REFERENCE)))
                    .thenReturn(map);

                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("123");
                mockedStaticSccCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("not_empty");
                mockedStaticSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("abc");

                when(redisClient.get(anyString())).thenReturn("1");
                when(i18nUtil.getMessage(anyString())).thenReturn("not_empty");
                when(obsService.getObject(anyString())).thenReturn("sss");

                HttpServletRequest request = Mockito.mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);
                when(request.getRequestURI()).thenReturn("/v1/123/agents/123/conversations/123");
                when(request.getHeader(anyString())).thenReturn("Bearer 123");
                when(request.getMethod()).thenReturn("POST");

                HttpServletResponse response = Mockito.mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

                FilterChain chain = Mockito.mock(FilterChain.class, Answers.RETURNS_DEEP_STUBS);

                // When
                openApiValidatorFilter.doFilterInternal(request, response, chain);
            }
        });
    }

    @Test
    void test_doFilter_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                    mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<CryptoUtils> mockedStaticSccCryptoUtils =
                    mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                Map<String, Object> map = new HashMap<>();
                map.put("userId", "123");
                map.put("code", "2");
                mockedStaticJsonUtils.when(() -> JsonUtils.json2Obj(anyString(), eq(Constants.MAP_TYPE_REFERENCE)))
                    .thenReturn(map);

                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId())
                    .thenReturn("not_empty");

                mockedStaticSccCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("not_empty");
                mockedStaticSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("abc");

                when(redisClient.get(anyString())).thenReturn("not_empty");
                when(obsService.getObject(anyString())).thenReturn("sss");
                when(i18nUtil.getMessage(anyString())).thenReturn("not_empty");

                HttpServletRequest request = Mockito.mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);
                HttpServletResponse response = Mockito.mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);
                when(request.getRequestURI()).thenReturn("/v1/123/agents/123/conversations/123");
                when(request.getHeader(anyString())).thenReturn("Bearer 123");
                when(request.getMethod()).thenReturn("POST");

                FilterChain chain = Mockito.mock(FilterChain.class, Answers.RETURNS_DEEP_STUBS);

                // When
                openApiValidatorFilter.doFilterInternal(request, response, chain);
            }
        });
    }

    @Test
    void test_doFilter_should_not_throw_exception5() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                    mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<CryptoUtils> mockedStaticSccCryptoUtils =
                    mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                Map<String, Object> map = new HashMap<>();
                map.put("userId", "123");
                mockedStaticJsonUtils.when(() -> JsonUtils.json2Obj(anyString(), eq(Constants.MAP_TYPE_REFERENCE)))
                    .thenReturn(map);

                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId())
                    .thenReturn("Bearer ");

                mockedStaticSccCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("Bearer ");

                when(redisClient.get(anyString())).thenReturn("Bearer ");

                when(i18nUtil.getMessage(anyString())).thenReturn("Bearer ");

                HttpServletRequest request = Mockito.mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);
                when(request.getMethod()).thenReturn("POST");
                HttpServletResponse response = Mockito.mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

                FilterChain chain = Mockito.mock(FilterChain.class, Answers.RETURNS_DEEP_STUBS);

                // When
                openApiValidatorFilter.doFilterInternal(request, response, chain);
            }
        });
    }

    @Test
    void test_doFilter_should_not_throw_exception6() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils =
                    mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<CryptoUtils> mockedStaticSccCryptoUtils =
                    mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                Map<String, Object> map = new HashMap<>();
                map.put("userId", "123");
                map.put("code", "2");
                mockedStaticJsonUtils.when(() -> JsonUtils.json2Obj(anyString(), eq(Constants.MAP_TYPE_REFERENCE)))
                    .thenReturn(map);

                mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("123");
                mockedStaticSccCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("not_empty");
                mockedStaticSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("abc");

                when(redisClient.get(anyString())).thenReturn("1");
                when(i18nUtil.getMessage(anyString())).thenReturn("not_empty");
                when(obsService.getObject(anyString())).thenReturn("sss");

                HttpServletRequest request = Mockito.mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);
                when(request.getRequestURI()).thenReturn("/v1/123/agents/123/conversations/123");
                when(request.getHeader(anyString())).thenReturn("Bearer 123");
                when(request.getMethod()).thenReturn("POST");

                HttpServletResponse response = Mockito.mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);

                FilterChain chain = Mockito.mock(FilterChain.class, Answers.RETURNS_DEEP_STUBS);

                // When
                openApiValidatorFilter.doFilterInternal(request, response, chain);
            }
        });
    }

    @Test
    void testGet() {
        assertDoesNotThrow(() -> {
            HttpServletRequest request = Mockito.mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);
            when(request.getMethod()).thenReturn("GET");
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class, Answers.RETURNS_DEEP_STUBS);
            FilterChain chain = Mockito.mock(FilterChain.class, Answers.RETURNS_DEEP_STUBS);
            // When
            openApiValidatorFilter.doFilterInternal(request, response, chain);
        });
    }
}
