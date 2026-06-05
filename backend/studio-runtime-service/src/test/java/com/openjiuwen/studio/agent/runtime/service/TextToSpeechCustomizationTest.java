/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.entity.Text2AudioReq;
import com.openjiuwen.studio.agent.common.entity.TtsConfig;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class TextToSpeechCustomizationTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SisProperties sisProperties;


    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OkHttpUtils okHttpUtils;

    @InjectMocks
    private TextToSpeechCustomization textToSpeechCustomization;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(textToSpeechCustomization, "sisClientUrl", "string");
        ReflectionTestUtils.setField(textToSpeechCustomization, "iamUrl", "string");
        ReflectionTestUtils.setField(textToSpeechCustomization, "sisProperties", sisProperties);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_convertTextToSpeech_should_return_not_null_when_condition() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
             MockedStatic<com.alibaba.fastjson.JSON> mockedStaticJSON1 = mockStatic(com.alibaba.fastjson.JSON.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticJSON.when(() -> JSON.toJSONString(any(Text2AudioReq.class))).thenReturn("string");

            JSONObject jSONObject = new JSONObject();
            mockedStaticJSON1.when(() -> com.alibaba.fastjson.JSON.parseObject(anyString())).thenReturn(jSONObject);


            RequestResult requestResult = new RequestResult();
            requestResult.setResponse("string");
            when(okHttpUtils.call(anyString(), eq(OkHttpUtils.Method.POST), anyMap(), anyString())).thenReturn(requestResult);

            when(sisProperties.getProjectId()).thenReturn("string");

            Text2AudioReq req = new Text2AudioReq();
            req.setText("string");
            TtsConfig config = new TtsConfig();
            config.setProperty("string");
            req.setConfig(config);

            // When
            JSONObject result = textToSpeechCustomization.convertTextToSpeech(req);

            // Then
            assertNull(result);
        }
    }

    @Test
    void test_convertTextToSpeech_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
                 MockedStatic<com.alibaba.fastjson.JSON> mockedStaticJSON1 = mockStatic(com.alibaba.fastjson.JSON.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticJSON.when(() -> JSON.toJSONString(any(Text2AudioReq.class))).thenReturn(null);

                mockedStaticJSON1.when(() -> com.alibaba.fastjson.JSON.parseObject(anyString())).thenReturn(null);

                when(okHttpUtils.call(anyString(), eq(OkHttpUtils.Method.POST), anyMap(), anyString())).thenReturn(null);

                when(sisProperties.getProjectId()).thenReturn(null);

                // When
                JSONObject result = textToSpeechCustomization.convertTextToSpeech(null);
            }
        });
    }

}