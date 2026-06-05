/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.entity.AsrCustomShortResponse;
import com.openjiuwen.studio.agent.runtime.entity.Audio2TextReq;
import com.openjiuwen.studio.agent.runtime.entity.StsTextResp;
import com.openjiuwen.studio.agent.runtime.entity.SttConfig;
import com.openjiuwen.studio.agent.runtime.model.RequestResult;
import com.openjiuwen.studio.agent.runtime.properties.SisProperties;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@MockitoSettings(strictness = Strictness.LENIENT)
class SpeechToTextCustomizationTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SisProperties sisProperties;


    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OkHttpUtils okHttpUtils;

    @InjectMocks
    private SpeechToTextCustomization speechToTextCustomization;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(speechToTextCustomization, "sisClientUrl", "string");
        ReflectionTestUtils.setField(speechToTextCustomization, "iamUrl", "string");
        ReflectionTestUtils.setField(speechToTextCustomization, "sisProperties", sisProperties);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_convertSpeechToText_should_return_not_null_when_condition() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
             MockedStatic<com.alibaba.fastjson.JSON> mockedStaticJSON1 = mockStatic(com.alibaba.fastjson.JSON.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticJSON.when(() -> JSON.toJSONString(any(Audio2TextReq.class))).thenReturn("string");

            AsrCustomShortResponse asrCustomShortResponse = new AsrCustomShortResponse();
            AsrCustomShortResponse.Result result =
                    Mockito.mock(AsrCustomShortResponse.Result.class, Answers.RETURNS_DEEP_STUBS);
            asrCustomShortResponse.setResult(result);
            mockedStaticJSON1.when(() -> com.alibaba.fastjson.JSON.parseObject(anyString(), eq(AsrCustomShortResponse.class))).thenReturn(asrCustomShortResponse);

            when(sisProperties.getProjectId()).thenReturn("string");

            RequestResult requestResult = new RequestResult();
            requestResult.setResponse("string");
            requestResult.setSuccess(true);
            when(okHttpUtils.call(anyString(), eq(OkHttpUtils.Method.POST), anyMap(), anyString())).thenReturn(requestResult);


            Audio2TextReq req = new Audio2TextReq();
            req.setData("string");
            SttConfig config = new SttConfig();
            req.setConfig(config);

            // When
            StsTextResp result1 = speechToTextCustomization.convertSpeechToText(req);

            // Then
            assertNotNull(result1);
        }
    }


    @Test
    void test_convertSpeechToText_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
                 MockedStatic<com.alibaba.fastjson.JSON> mockedStaticJSON1 = mockStatic(com.alibaba.fastjson.JSON.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticJSON.when(() -> JSON.toJSONString(any(Audio2TextReq.class))).thenReturn(null);

                mockedStaticJSON1.when(() -> com.alibaba.fastjson.JSON.parseObject(anyString(), eq(AsrCustomShortResponse.class))).thenReturn(null);

                when(sisProperties.getProjectId()).thenReturn(null);

                when(okHttpUtils.call(anyString(), eq(OkHttpUtils.Method.POST), anyMap(), anyString())).thenReturn(null);

                // When
                StsTextResp result = speechToTextCustomization.convertSpeechToText(null);
            }
        });
    }

    @Test
    void test_convertSpeechToText_should_throw_exception_when_code_400() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
             MockedStatic<com.alibaba.fastjson.JSON> mockedStaticJSON1 =
                     mockStatic(com.alibaba.fastjson.JSON.class, RETURNS_DEEP_STUBS)) {

            // Given
            mockedStaticJSON.when(() -> JSON.toJSONString(any(Audio2TextReq.class))).thenReturn("string");

            AsrCustomShortResponse asrCustomShortResponse = new AsrCustomShortResponse();
            AsrCustomShortResponse.Result result =
                    Mockito.mock(AsrCustomShortResponse.Result.class, Answers.RETURNS_DEEP_STUBS);
            asrCustomShortResponse.setResult(result);
            mockedStaticJSON1
                    .when(() -> com.alibaba.fastjson.JSON.parseObject(anyString(), eq(AsrCustomShortResponse.class)))
                    .thenReturn(asrCustomShortResponse);

            when(sisProperties.getProjectId()).thenReturn("string");

            // 设置 requestResult 为失败状态，错误码为 400
            RequestResult requestResult = new RequestResult();
            requestResult.setSuccess(false);
            requestResult.setCode(400);
            requestResult.setResponse("string");

            when(okHttpUtils.call(anyString(), eq(OkHttpUtils.Method.POST), anyMap(), anyString()))
                    .thenReturn(requestResult);

            Audio2TextReq req = new Audio2TextReq();
            req.setData("string");
            SttConfig config = new SttConfig();
            req.setConfig(config);

            // When & Then
            AgentStudioException exception = assertThrows(AgentStudioException.class, () -> {
                speechToTextCustomization.convertSpeechToText(req);
            });

            // 验证异常是否为期望的错误码
            assertEquals(StudioError.SIS_VOICE_DURATION_EXCEEDED, exception.getErrorCode());
        }
    }

    @Test
    void test_sendDone_should_void_when_condition() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<SseEmitter> mockedStaticSseEmitter = mockStatic(SseEmitter.class, RETURNS_DEEP_STUBS)) {
                // Given
                SseEventBuilder sseEventBuilder = Mockito.mock(SseEventBuilder.class, Answers.RETURNS_DEEP_STUBS);
                mockedStaticSseEmitter.when(() -> SseEmitter.event().name(eq("done")).data(eq("[Done]"))).thenReturn(sseEventBuilder);

                SseEmitter sseEmitter = new SseEmitter();

                // When
                speechToTextCustomization.sendDone(sseEmitter);
            }
        });
    }
}