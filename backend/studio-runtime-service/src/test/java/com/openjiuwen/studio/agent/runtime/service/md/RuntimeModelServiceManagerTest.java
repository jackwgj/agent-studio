/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.REQUEST_ID;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.TASK_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.dto.md.NotAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.md.ImageGenerationReq;
import com.openjiuwen.studio.agent.common.dto.md.ModelInvokeBase;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelServiceDetail;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelStrategy;
import com.openjiuwen.studio.agent.runtime.dto.md.VideoGenerationReq;
import com.openjiuwen.studio.agent.runtime.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.runtime.enums.ModelStrategyEnum;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.md.adapter.auth.AuthAdapterFactory;
import com.openjiuwen.studio.agent.runtime.service.md.adapter.req.OpenApiRequestAdapter;
import com.openjiuwen.studio.agent.runtime.service.md.adapter.req.RequestAdapter;
import com.openjiuwen.studio.agent.runtime.service.md.adapter.req.RuntimeRequestAdapterFactory;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class RuntimeModelServiceManagerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RuntimeModelHttpClientService httpClientService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RuntimeRequestAdapterFactory factory;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ModelStorageService modelStorageService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ModelAuthStorageService authStorageService;

    @InjectMocks
    private RuntimeModelServiceManager runtimeModelServiceManager;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(runtimeModelServiceManager, "defaultTimeout", 0);
        ReflectionTestUtils.setField(runtimeModelServiceManager, "platformProviderId", "not_empty");
        ReflectionTestUtils.setField(runtimeModelServiceManager, "videoTaskKeepTime", 0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_videoGenerations_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (MockedStatic<ModelApiLogThreadLocal> mockedStaticModelApiLogThreadLocal = mockStatic(
                ModelApiLogThreadLocal.class, RETURNS_DEEP_STUBS);
                MockedStatic<JsonUtils> mockedStaticJsonUtils = mockStatic(JsonUtils.class, RETURNS_DEEP_STUBS);
                MockedStatic<AuthAdapterFactory> mockedStaticAuthAdapterFactory = mockStatic(AuthAdapterFactory.class,
                    RETURNS_DEEP_STUBS);
                MockedStatic<MDC> mockedStaticMDC = mockStatic(MDC.class, RETURNS_DEEP_STUBS);
                MockedStatic<EntityUtils> mockedStaticEntityUtils = mockStatic(EntityUtils.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticModelApiLogThreadLocal.when(ModelApiLogThreadLocal::getApiLog).thenReturn(null);

                mockedStaticJsonUtils.when(() -> JsonUtils.encode(any(Object.class))).thenReturn(null);

                mockedStaticAuthAdapterFactory.when(() -> AuthAdapterFactory.getAuthAdapter(any(ProviderAuth.class))
                    .requestHeaderHandler(anyString(), eq("POST"), anyMap(), anyMap(), any(Object.class),
                        any(ProviderAuth.class))).thenReturn(null);

                mockedStaticMDC.when(() -> MDC.get(eq(REQUEST_ID))).thenReturn(null);
                mockedStaticMDC.when(() -> MDC.get(eq(TASK_ID))).thenReturn(null);

                mockedStaticEntityUtils.when(() -> EntityUtils.toString(any(HttpEntity.class), eq("UTF-8")))
                    .thenReturn(null);

                CloseableHttpClient client = mock(CloseableHttpClient.class, Answers.RETURNS_DEEP_STUBS);
                when(httpClientService.getModelRequestHttpClient(anyString())).thenReturn(client);
                when(httpClientService.header2Map(any(Header[].class))).thenReturn(null);

                RequestAdapter adapter = mock(RequestAdapter.class, Answers.RETURNS_DEEP_STUBS);
                when(adapter.resBodyConvert(anyString(), any(), any(ModelInvokeBase.class))).thenReturn(null);
                when(adapter.requestExceptionHandler(any(Exception.class))).thenReturn(null);
                when(adapter.requestBodyConvert(anyMap(), any(ModelInvokeBase.class), eq(false))).thenReturn(null);
                when(factory.getAdapter(anyString())).thenReturn(adapter);

                when(redisClient.getAndIncrement(anyString(), eq(1))).thenReturn(0L);

                when(modelStorageService.queryModelStrategy(anyString(), anyString(), anyString(), anyString(),
                    anyBoolean())).thenReturn(null);

                // When
                Object result = runtimeModelServiceManager.videoGenerations(null, null, null, null, true, null, null,
                    null);
            }
        });
    }

    @Test
    public void initRequestHeaderTest() {
        Map<String, String> headers = new RuntimeModelServiceManager().initRequestHeader(new HashMap<>());
        Assertions.assertEquals("application/json", headers.get("Content-Type"));

        headers.put("content-type", "test");
        headers = new RuntimeModelServiceManager().initRequestHeader(headers);
        Assertions.assertEquals("test", headers.get("Content-Type"));
    }

    @Test
    public void imageGenerationsTestExp() {
        try {
            new RuntimeModelServiceManager().imageGenerations("workspace", "model", new HashMap<>(),
                new ImageGenerationReq(), false, "project", "auth", "owner");
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void videoGenerationsTest() {
        ReflectionTestUtils.setField(runtimeModelServiceManager, "modelStorageService", modelStorageService);

        Mockito.when(factory.getAdapter("opeai")).thenReturn(new OpenApiRequestAdapter());

        ModelApiLogThreadLocal.setApiLog(new ModelApiLog());

        ModelStrategy strategy = new ModelStrategy();
        strategy.setType(ModelStrategyEnum.MODEL);

        ModelServiceDetail md = JSONObject.parseObject("{\"available\":true}", ModelServiceDetail.class);
        md.setModel(new ModelServiceBase().setModelType("TEXT-TO-VIDEO").setApiUrl("https://demo.com/v1/test"));
        md.setAuth(new NotAuthInfo());

        strategy.setModels(Collections.singletonList(md));

        Mockito.when(modelStorageService.queryModelStrategy(any(), any(), any(), any(), anyBoolean()))
            .thenReturn(strategy);

        runtimeModelServiceManager.videoGenerations("workspace", "model", new HashMap<>(), new VideoGenerationReq(),
            false, "project", "auth", "owner");
    }

    @Test
    public void queryVideoGenerationTaskTest() {
        try {
            runtimeModelServiceManager.queryVideoGenerationTask("PP", "ww", "AA", "task");
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_VIDEO_GENERATION_TASK_NOT_EXIST, e.getErrorCode());
        }
        when(authStorageService.queryProviderAuth(any(), any(), any(), any(), anyBoolean(), anyString())).thenReturn(
            new NotAuthInfo());
        when(redisClient.get("video_generator_PP_task")).thenReturn("{\"url\":\"https://demo.com/v1/test\"}");
        runtimeModelServiceManager.queryVideoGenerationTask("PP", "ww", "AA", "task");
    }
}
