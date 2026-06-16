package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.InvokeResp;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PeEvalResult;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationResult;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationMethodType;
import com.openjiuwen.studio.prompt.engineering.service.model.ModelServiceCollection;
import com.openjiuwen.studio.prompt.engineering.task.dao.JobInstance;
import com.openjiuwen.studio.prompt.engineering.vo.ResultNumCount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationMethodServiceTest {

    @Mock
    private ModelServiceCollection modelServiceCollection;

    @InjectMocks
    private EvaluationMethodService evaluationMethodService;

    private JobInstance jobInstance;

    @BeforeEach
    void setUp() {
        jobInstance = new JobInstance();
        jobInstance.setWorkspaceId("test-workspace");
    }

    @Test
    void testEvaluate_EmptyPromptAnswer_ReturnsZero() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MATCHING);
        Integer result = evaluationMethodService.evaluate(jobInstance, "", "standard", task);
        assertEquals(0, result);
    }

    @Test
    void testEvaluate_NullPromptAnswer_ReturnsZero() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MATCHING);
        Integer result = evaluationMethodService.evaluate(jobInstance, null, "standard", task);
        assertEquals(0, result);
    }

    @Test
    void testEvaluate_EmptyStandardAnswer_ReturnsZero() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MATCHING);
        Integer result = evaluationMethodService.evaluate(jobInstance, "prompt", "", task);
        assertEquals(0, result);
    }

    @Test
    void testEvaluate_NullStandardAnswer_ReturnsZero() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MATCHING);
        Integer result = evaluationMethodService.evaluate(jobInstance, "prompt", null, task);
        assertEquals(0, result);
    }

    @Test
    void testEvaluate_Matching_ExactMatch_Returns100() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MATCHING);
        Integer result = evaluationMethodService.evaluate(jobInstance, "hello", "hello", task);
        assertEquals(100, result);
    }

    @Test
    void testEvaluate_Matching_NotMatch_Returns0() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MATCHING);
        Integer result = evaluationMethodService.evaluate(jobInstance, "hello", "world", task);
        assertEquals(0, result);
    }

    @Test
    void testEvaluate_RegularMatching_Match_Returns100() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.REGULAR_MATCHING);
        task.setRegularExpression("^\\d+$");
        Integer result = evaluationMethodService.evaluate(jobInstance, "12345", "standard", task);
        assertEquals(100, result);
    }

    @Test
    void testEvaluate_RegularMatching_NotMatch_Returns0() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.REGULAR_MATCHING);
        task.setRegularExpression("^\\d+$");
        Integer result = evaluationMethodService.evaluate(jobInstance, "abcde", "standard", task);
        assertEquals(0, result);
    }

    @Test
    void testEvaluate_Similarity_SameText_Returns100() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.SIMILARITY);
        Integer result = evaluationMethodService.evaluate(jobInstance, "hello world", "hello world", task);
        assertEquals(100, result);
    }

    @Test
    void testEvaluate_Similarity_DifferentText_ReturnsLowScore() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.SIMILARITY);
        Integer result = evaluationMethodService.evaluate(jobInstance, "hello world", "foo bar baz", task);
        assertTrue(result < 100);
    }

    @Test
    void testEvaluate_Similarity_PartialOverlap_ReturnsMediumScore() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.SIMILARITY);
        Integer result = evaluationMethodService.evaluate(jobInstance, "hello world test", "hello world demo", task);
        assertTrue(result > 0 && result < 100);
    }

    @Test
    void testEvaluate_Model_NumericResult_ReturnsScore() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MODEL);
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setModelSource("test-source");
        task.setEvalModelConfig(modelConfig);

        InvokeResp invokeResp = new InvokeResp().setResult("85");

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            requestContextMock.when(RequestContextUtils::getRequestProjectId).thenReturn("test-project");
            when(modelServiceCollection.callModel(anyString(), any(), anyString(), anyString())).thenReturn(invokeResp);

            Integer result = evaluationMethodService.evaluate(jobInstance, "prompt answer", "standard answer", task);
            assertEquals(85, result);
        }
    }

    @Test
    void testEvaluate_Model_NonNumericResult_Returns0() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MODEL);
        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setModelSource("test-source");
        task.setEvalModelConfig(modelConfig);

        InvokeResp invokeResp = new InvokeResp().setResult("not a number");

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            requestContextMock.when(RequestContextUtils::getRequestProjectId).thenReturn("test-project");
            when(modelServiceCollection.callModel(anyString(), any(), anyString(), anyString())).thenReturn(invokeResp);

            Integer result = evaluationMethodService.evaluate(jobInstance, "prompt answer", "standard answer", task);
            assertEquals(0, result);
        }
    }

    @Test
    void testEvaluate_Model_EmptyModelSource_UsesDefault() {
        PeEvaluationTask task = buildTask(EvaluationMethodType.MODEL);
        ModelConfig modelConfig = new ModelConfig();
        task.setEvalModelConfig(modelConfig);

        InvokeResp invokeResp = new InvokeResp().setResult("90");

        try (MockedStatic<RequestContextUtils> requestContextMock = mockStatic(RequestContextUtils.class)) {
            requestContextMock.when(RequestContextUtils::getRequestAuthToken).thenReturn("test-token");
            requestContextMock.when(RequestContextUtils::getRequestProjectId).thenReturn("test-project");
            when(modelServiceCollection.callModel(anyString(), any(), anyString(), anyString())).thenReturn(invokeResp);

            Integer result = evaluationMethodService.evaluate(jobInstance, "prompt answer", "standard answer", task);
            assertEquals(90, result);
        }
    }

    @Test
    void testEvaluate_NullMethod_ThrowsNPE() {
        PeEvaluationTask task = PeEvaluationTask.builder()
            .id("test-id")
            .method(null)
            .build();
        assertThrows(NullPointerException.class,
            () -> evaluationMethodService.evaluate(jobInstance, "prompt", "standard", task));
    }

    @Test
    void testGraspPromptScore_SingleResult_ReturnsScore() {
        PeEvalResult evalResult = new PeEvalResult().setScore(80);
        PeEvaluationResult peResult = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult)
            .build();

        Map<String, Integer> scoreMap = evaluationMethodService.graspPromptScore(List.of(peResult));
        assertEquals(80, scoreMap.get("prompt-1"));
    }

    @Test
    void testGraspPromptScore_MultipleResultsSamePrompt_ReturnsAverage() {
        PeEvalResult evalResult1 = new PeEvalResult().setScore(80);
        PeEvalResult evalResult2 = new PeEvalResult().setScore(60);
        PeEvaluationResult peResult1 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult1)
            .build();
        PeEvaluationResult peResult2 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult2)
            .build();

        Map<String, Integer> scoreMap = evaluationMethodService.graspPromptScore(List.of(peResult1, peResult2));
        assertEquals(70, scoreMap.get("prompt-1"));
    }

    @Test
    void testGraspPromptScore_MultiplePrompts_ReturnsPerPromptAverage() {
        PeEvalResult evalResult1 = new PeEvalResult().setScore(80);
        PeEvalResult evalResult2 = new PeEvalResult().setScore(60);
        PeEvalResult evalResult3 = new PeEvalResult().setScore(90);
        PeEvaluationResult peResult1 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult1)
            .build();
        PeEvaluationResult peResult2 = PeEvaluationResult.builder()
            .promptId("prompt-2")
            .evalResult(evalResult2)
            .build();
        PeEvaluationResult peResult3 = PeEvaluationResult.builder()
            .promptId("prompt-2")
            .evalResult(evalResult3)
            .build();

        Map<String, Integer> scoreMap = evaluationMethodService.graspPromptScore(List.of(peResult1, peResult2, peResult3));
        assertEquals(80, scoreMap.get("prompt-1"));
        assertEquals(75, scoreMap.get("prompt-2"));
    }

    @Test
    void testGraspPromptScore_EmptyList_ReturnsEmptyMap() {
        Map<String, Integer> scoreMap = evaluationMethodService.graspPromptScore(List.of());
        assertTrue(scoreMap.isEmpty());
    }

    @Test
    void testGraspPromptScore_ThreeResultsSamePrompt_ReturnsAverage() {
        PeEvalResult evalResult1 = new PeEvalResult().setScore(100);
        PeEvalResult evalResult2 = new PeEvalResult().setScore(50);
        PeEvalResult evalResult3 = new PeEvalResult().setScore(75);
        PeEvaluationResult peResult1 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult1)
            .build();
        PeEvaluationResult peResult2 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult2)
            .build();
        PeEvaluationResult peResult3 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult3)
            .build();

        Map<String, Integer> scoreMap = evaluationMethodService.graspPromptScore(List.of(peResult1, peResult2, peResult3));
        assertEquals(75, scoreMap.get("prompt-1"));
    }

    @Test
    void testGraspPromptScore_NullEvalResult_ThrowsException() {
        PeEvaluationResult peResult = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(null)
            .build();

        assertThrows(AgentStudioException.class, () ->
            evaluationMethodService.graspPromptScore(List.of(peResult)));
    }



    @Test
    void testGraspPromptScore_NullPromptId_StillWorks() {
        PeEvalResult evalResult = new PeEvalResult().setScore(80);
        PeEvaluationResult peResult = PeEvaluationResult.builder()
            .promptId(null)
            .evalResult(evalResult)
            .build();

        Map<String, Integer> scoreMap = evaluationMethodService.graspPromptScore(List.of(peResult));
        assertEquals(80, scoreMap.get(null));
    }

    @Test
    void testParsePromptResult_SingleResult_ReturnsCorrectData() {
        PeEvalResult evalResult = new PeEvalResult().setScore(80).setToken(100).setTime(1.5);
        PeEvaluationResult peResult = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult)
            .build();

        Map<String, ResultNumCount> resultMap = evaluationMethodService.parsePromptResult(List.of(peResult));
        ResultNumCount count = resultMap.get("prompt-1");
        assertNotNull(count);
        assertEquals(80, count.getScore());
        assertEquals(100, count.getToken());
        assertEquals(1.5, count.getTime(), 0.001);
        assertEquals(0, count.getCorrect());
    }

    @Test
    void testParsePromptResult_PerfectScore_IncrementCorrect() {
        PeEvalResult evalResult = new PeEvalResult().setScore(100).setToken(50).setTime(2.0);
        PeEvaluationResult peResult = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult)
            .build();

        Map<String, ResultNumCount> resultMap = evaluationMethodService.parsePromptResult(List.of(peResult));
        ResultNumCount count = resultMap.get("prompt-1");
        assertEquals(1, count.getCorrect());
    }

    @Test
    void testParsePromptResult_MultipleResultsSamePrompt_ReturnsAverage() {
        PeEvalResult evalResult1 = new PeEvalResult().setScore(80).setToken(100).setTime(1.5);
        PeEvalResult evalResult2 = new PeEvalResult().setScore(60).setToken(200).setTime(2.5);
        PeEvaluationResult peResult1 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult1)
            .build();
        PeEvaluationResult peResult2 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult2)
            .build();

        Map<String, ResultNumCount> resultMap = evaluationMethodService.parsePromptResult(List.of(peResult1, peResult2));
        ResultNumCount count = resultMap.get("prompt-1");
        assertEquals(70, count.getScore());
        assertEquals(150, count.getToken());
        assertEquals(2.0, count.getTime(), 0.001);
        assertEquals(0, count.getCorrect());
    }

    @Test
    void testParsePromptResult_MultiplePrompts_ReturnsPerPromptData() {
        PeEvalResult evalResult1 = new PeEvalResult().setScore(100).setToken(50).setTime(1.0);
        PeEvalResult evalResult2 = new PeEvalResult().setScore(50).setToken(100).setTime(2.0);
        PeEvaluationResult peResult1 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult1)
            .build();
        PeEvaluationResult peResult2 = PeEvaluationResult.builder()
            .promptId("prompt-2")
            .evalResult(evalResult2)
            .build();

        Map<String, ResultNumCount> resultMap = evaluationMethodService.parsePromptResult(List.of(peResult1, peResult2));
        assertEquals(2, resultMap.size());
        assertEquals(1, resultMap.get("prompt-1").getCorrect());
        assertEquals(0, resultMap.get("prompt-2").getCorrect());
    }

    @Test
    void testParsePromptResult_ThreeResultsSamePrompt_OnePerfect_ReturnsCorrectCount() {
        PeEvalResult evalResult1 = new PeEvalResult().setScore(100).setToken(50).setTime(1.0);
        PeEvalResult evalResult2 = new PeEvalResult().setScore(80).setToken(100).setTime(2.0);
        PeEvalResult evalResult3 = new PeEvalResult().setScore(100).setToken(75).setTime(1.5);
        PeEvaluationResult peResult1 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult1)
            .build();
        PeEvaluationResult peResult2 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult2)
            .build();
        PeEvaluationResult peResult3 = PeEvaluationResult.builder()
            .promptId("prompt-1")
            .evalResult(evalResult3)
            .build();

        Map<String, ResultNumCount> resultMap = evaluationMethodService.parsePromptResult(List.of(peResult1, peResult2, peResult3));
        ResultNumCount count = resultMap.get("prompt-1");
        assertEquals(93, count.getScore());
        assertEquals(75, count.getToken());
        assertEquals(1.5, count.getTime(), 0.001);
        assertEquals(2, count.getCorrect());
    }

    @Test
    void testParsePromptResult_EmptyList_ReturnsEmptyMap() {
        Map<String, ResultNumCount> resultMap = evaluationMethodService.parsePromptResult(List.of());
        assertTrue(resultMap.isEmpty());
    }

    private PeEvaluationTask buildTask(EvaluationMethodType methodType) {
        return PeEvaluationTask.builder()
            .id("test-task-id")
            .method(methodType)
            .regularExpression(null)
            .evalModelConfig(null)
            .build();
    }
}
