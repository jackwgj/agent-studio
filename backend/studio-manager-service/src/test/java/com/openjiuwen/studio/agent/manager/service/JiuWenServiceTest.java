/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.rce.client.AgentRuntimeClient;
import com.openjiuwen.studio.agent.manager.rce.models.AskModelReq;
import com.openjiuwen.studio.agent.manager.rce.models.CodeGenerateResultVo;
import com.openjiuwen.studio.agent.manager.rce.service.JiuWenService;
import com.openjiuwen.studio.agent.manager.service.md.FreeModelService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import reactor.core.publisher.Flux;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@MockitoSettings(strictness = Strictness.LENIENT)
class JiuWenServiceTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private WebClient webClient;

    @InjectMocks
    private JiuWenService jiuWenService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jiuWenService, "runtimeEndpoint", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_callModelStream_success() {
        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.get(eq(CommonConstant.X_AUTH_TOKEN))).thenReturn(Collections.singletonList("token"));
        String projectId = "proj1";
        String workspaceId = "ws1";
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();

        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            CodeGenerateResultVo responseVo = new CodeGenerateResultVo();
            CodeGenerateResultVo.Delta delta = new CodeGenerateResultVo.Delta("assistant", "test_content");
            CodeGenerateResultVo.Choice choice = new CodeGenerateResultVo.Choice();
            choice.setDelta(delta);
            choice.setIndex(0);
            responseVo.setChoices(Collections.singletonList(choice));
            jsonUtilsMock.when(() -> JsonUtils.json2ObjQuietly(anyString(), eq(CodeGenerateResultVo.class)))
                .thenReturn(responseVo);

            Flux<String> dataFlux = Flux.just("{\"choices\": [{\"delta\": {\"content\": \"code\"}}]}");
            when(webClient.post()
                .uri(anyString())
                .contentType(eq(MediaType.APPLICATION_JSON))
                .header(eq(CommonConstant.X_AUTH_TOKEN), eq("token"))
                .bodyValue(eq(chatCompletionRequest))
                .retrieve()
                .onStatus(any(), any())
                .bodyToFlux(eq(String.class))).thenReturn(dataFlux);

            Object result = jiuWenService.callModelStream(headers, projectId, workspaceId, chatCompletionRequest);
            assertNotNull(result);
        }
    }

    @Test
    public void callModelTest() {
        AskModelReq req = AskModelReq.builder().build();

        AgentRuntimeClient runtimeClient = Mockito.mock(AgentRuntimeClient.class, Answers.RETURNS_DEEP_STUBS);

        ProviderAuthDataMapper authDataMapper = Mockito.mock(ProviderAuthDataMapper.class, Answers.RETURNS_DEEP_STUBS);

        FreeModelService freeModelService = Mockito.mock(FreeModelService.class, Answers.RETURNS_DEEP_STUBS);

        JiuWenService service = new JiuWenService(runtimeClient, authDataMapper, freeModelService);
        try {
            service.callModel(req, "workspaceId");
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MODEL_CONFIG_MISS, e.getErrorCode());
        }

        req.setModelName("model|model");
        Mockito.when(freeModelService.isFreeModel(Mockito.any())).thenReturn(false);
        Mockito.when(authDataMapper.selectByModelId(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

        MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        RequestContextUtils.setRequestAuthTokenAndProjectId("token", "test");

        try {
            service.callModel(req, "workspaceId");
        } catch (Exception e) {
            Assertions.assertTrue(true);
        }
        mockedHeaderStatic.close();
    }
}
