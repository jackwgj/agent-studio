package com.openjiuwen.studio.agent.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.AutoAddResultJsonObject;
import com.openjiuwen.studio.agent.manager.dto.ModelConfig;
import com.openjiuwen.studio.agent.manager.rce.models.AskModelReq;
import com.openjiuwen.studio.agent.manager.rce.service.JiuWenService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiGenServiceTest {

    @Mock
    private AgentCommonService agentCommonService;

    @Mock
    private JiuWenService jiuWenService;

    @InjectMocks
    private AiGenService aiGenService;

    private static final String MODEL_DEPLOYMENT_ID = "deploy-1";
    private static final String MODEL_TYPE = "llm";
    private static final String MODEL = "test-model";
    private static final String QUERY = "test-query";
    private static final String WORKSPACE_ID = "ws-1";
    private static final ModelConfig CONFIG = new ModelConfig();

    @Test
    void testGeneratePrologue_Success() {
        AskModelReq req = mock(AskModelReq.class);
        when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
        when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenReturn("Hello world");

        AutoAddResultJsonObject result = aiGenService.generatePrologue(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

        assertEquals("Hello world", result.getPrologue());
    }

    @Test
    void testGeneratePrologue_BlankFirstCallSuccessOnRetry() {
        AskModelReq req = mock(AskModelReq.class);
        when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
        when(jiuWenService.callModel(any(AskModelReq.class), anyString()))
            .thenReturn("")
            .thenReturn("Retry prologue");

        AutoAddResultJsonObject result = aiGenService.generatePrologue(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

        assertEquals("Retry prologue", result.getPrologue());
        verify(jiuWenService, times(2)).callModel(any(AskModelReq.class), anyString());
    }

    @Test
    void testGeneratePrologue_AllCallsBlank_DefaultChinese() {
        try (MockedStatic<LanguageUtils> lang = mockStatic(LanguageUtils.class)) {
            lang.when(LanguageUtils::isChinese).thenReturn(true);

            AskModelReq req = mock(AskModelReq.class);
            when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
            when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenReturn("");

            AutoAddResultJsonObject result = aiGenService.generatePrologue(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

            assertEquals(CommonConstant.PROLOGUE, result.getPrologue());
            verify(jiuWenService, times(CommonConstant.MODEL_CALLS)).callModel(any(AskModelReq.class), anyString());
        }
    }

    @Test
    void testGeneratePrologue_AllCallsBlank_DefaultEnglish() {
        try (MockedStatic<LanguageUtils> lang = mockStatic(LanguageUtils.class)) {
            lang.when(LanguageUtils::isChinese).thenReturn(false);

            AskModelReq req = mock(AskModelReq.class);
            when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
            when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenReturn("");

            AutoAddResultJsonObject result = aiGenService.generatePrologue(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

            assertEquals(CommonConstant.PROLOGUE_EN, result.getPrologue());
        }
    }

    @Test
    void testGeneratePrologue_ExceptionThrown() {
        AskModelReq req = mock(AskModelReq.class);
        when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
        when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenThrow(new RuntimeException("call failed"));

        AgentStudioException ex = assertThrows(AgentStudioException.class,
            () -> aiGenService.generatePrologue(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID));

        assertEquals(StudioError.CALL_MODEL_ERROR, ex.getErrorCode());
    }

    @Test
    void testGeneratePrologue_PrologueTrimmed() {
        AskModelReq req = mock(AskModelReq.class);
        when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
        when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenReturn("  Hello world  ");

        AutoAddResultJsonObject result = aiGenService.generatePrologue(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

        assertEquals("Hello world", result.getPrologue());
    }

    @Test
    void testRecommendedQuestions_Success() {
        try (MockedStatic<JsonUtils> jsonUtils = mockStatic(JsonUtils.class)) {
            AskModelReq req = mock(AskModelReq.class);
            when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
            when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenReturn("[\"Q1\",\"Q2\"]");
            List<String> questions = Arrays.asList("Q1", "Q2");
            jsonUtils.when(() -> JsonUtils.json2Obj(anyString(), any(TypeReference.class)))
                .thenReturn(questions);

            AutoAddResultJsonObject result = aiGenService.recommendedQuestions(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

            assertEquals(2, result.getQuestions().size());
            assertTrue(result.getQuestions().contains("Q1"));
            assertTrue(result.getQuestions().contains("Q2"));
        }
    }

    @Test
    void testRecommendedQuestions_EmptyList() {
        try (MockedStatic<JsonUtils> jsonUtils = mockStatic(JsonUtils.class)) {
            AskModelReq req = mock(AskModelReq.class);
            when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
            when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenReturn("[]");
            jsonUtils.when(() -> JsonUtils.json2Obj(anyString(), any(TypeReference.class)))
                .thenReturn(new ArrayList<>());

            AutoAddResultJsonObject result = aiGenService.recommendedQuestions(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

            assertTrue(result.getQuestions().isEmpty());
            verify(jiuWenService, times(CommonConstant.MODEL_CALLS)).callModel(any(AskModelReq.class), anyString());
        }
    }

    @Test
    void testRecommendedQuestions_TruncatedToMax() {
        try (MockedStatic<JsonUtils> jsonUtils = mockStatic(JsonUtils.class)) {
            AskModelReq req = mock(AskModelReq.class);
            when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
            when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenReturn("[\"Q1\",\"Q2\",\"Q3\",\"Q4\",\"Q5\"]");
            List<String> questions = Arrays.asList("Q1", "Q2", "Q3", "Q4", "Q5");
            jsonUtils.when(() -> JsonUtils.json2Obj(anyString(), any(TypeReference.class)))
                .thenReturn(questions);

            AutoAddResultJsonObject result = aiGenService.recommendedQuestions(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

            assertEquals(CommonConstant.MAX_QUESTION_NUMS, result.getQuestions().size());
            assertEquals("Q1", result.getQuestions().get(0));
            assertEquals("Q2", result.getQuestions().get(1));
            assertEquals("Q3", result.getQuestions().get(2));
        }
    }

    @Test
    void testRecommendedQuestions_ExceptionThrown() {
        AskModelReq req = mock(AskModelReq.class);
        when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
        when(jiuWenService.callModel(any(AskModelReq.class), anyString())).thenThrow(new RuntimeException("call failed"));

        AgentStudioException ex = assertThrows(AgentStudioException.class,
            () -> aiGenService.recommendedQuestions(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID));

        assertEquals(StudioError.CALL_MODEL_ERROR, ex.getErrorCode());
    }

    @Test
    void testRecommendedQuestions_BlankFirstCallSuccessOnRetry() {
        try (MockedStatic<JsonUtils> jsonUtils = mockStatic(JsonUtils.class)) {
            AskModelReq req = mock(AskModelReq.class);
            when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString())).thenReturn(req);
            when(jiuWenService.callModel(any(AskModelReq.class), anyString()))
                .thenReturn("")
                .thenReturn("[\"Q1\"]");
            jsonUtils.when(() -> JsonUtils.json2Obj(eq(""), any(TypeReference.class)))
                .thenReturn(new ArrayList<>());
            jsonUtils.when(() -> JsonUtils.json2Obj(eq("[\"Q1\"]"), any(TypeReference.class)))
                .thenReturn(Arrays.asList("Q1"));

            AutoAddResultJsonObject result = aiGenService.recommendedQuestions(MODEL_DEPLOYMENT_ID, MODEL_TYPE, MODEL, CONFIG, QUERY, WORKSPACE_ID);

            assertEquals(1, result.getQuestions().size());
            assertEquals("Q1", result.getQuestions().get(0));
            verify(jiuWenService, times(2)).callModel(any(AskModelReq.class), anyString());
        }
    }
}
