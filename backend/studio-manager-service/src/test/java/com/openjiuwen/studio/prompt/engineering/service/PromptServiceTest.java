/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.FileInfoVo;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptPageInfo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptVo;
import com.openjiuwen.studio.prompt.engineering.dto.QueryCondition;
import com.openjiuwen.studio.prompt.engineering.dto.SmallPromptDto;
import com.openjiuwen.studio.prompt.engineering.dto.UpdateOrDeletePromptCheckDto;
import com.openjiuwen.studio.prompt.engineering.dto.VariableDto;
import com.openjiuwen.studio.prompt.engineering.entity.CandidateVariable;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.entity.PeVariable;
import com.openjiuwen.studio.prompt.engineering.enums.SourceType;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeVariableMapper;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PromptService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PromptServiceTest {

    @InjectMocks
    private PromptService promptService;

    @Mock
    private PeTaskMapper peTaskMapper;

    @Mock
    private PePromptMapper pePromptMapper;

    @Mock
    private PeVariableMapper peVariableMapper;

    @Mock
    private EvaluationMethodService evaluationMethodService;

    @Mock
    private PeEvaluationTaskMapper peEvaluationTaskMapper;

    @Mock
    private PromptObsService promptObsService;

    // ========== getFileInfoVoByFileId ==========

    @Test
    void getFileInfoVoByFileId() {
        String fildId = "";
        FileInfoVo vo = promptService.getFileInfoVoByFileId(fildId);
        assertNull(vo.getFileId());
        fildId = "file";
        FileInfoVo fileInfoVo = promptService.getFileInfoVoByFileId(fildId);
        assertNotNull(fileInfoVo);
    }

    @Test
    void testGetFileInfoVoByFileId_WithFileId() {
        when(promptObsService.getObsTempUrl(anyString(), anyLong())).thenReturn("https://obs.example.com/temp");

        var result = promptService.getFileInfoVoByFileId("file-001");
        assertNotNull(result);
        assertEquals("file-001", result.getFileId());
        assertEquals("https://obs.example.com/temp", result.getTempUrl());
    }

    // ========== deletePrompt ==========

    @Test
    void testDeletePrompt_Success() {
        String projectId = "project1";
        String promptId = "prompt-001";
        String workspaceId = "workspace1";

        PePrompt pePrompt = new PePrompt();
        pePrompt.setId(promptId);
        pePrompt.setTaskId("task-001");
        pePrompt.setVariables("[]");
        when(pePromptMapper.queryByPromptId(promptId, workspaceId)).thenReturn(pePrompt);
        when(peEvaluationTaskMapper.queryByTaskIdAndStatus("task-001", workspaceId))
            .thenReturn(Collections.emptyList());

        String result = promptService.deletePrompt(projectId, promptId, workspaceId);

        verify(pePromptMapper).deleteByPromptId(promptId, workspaceId);
    }

    @Test
    void testDeletePrompt_NotExist() {
        String projectId = "project1";
        String promptId = "prompt-999";
        String workspaceId = "workspace1";

        when(pePromptMapper.queryByPromptId(promptId, workspaceId)).thenReturn(null);

        assertThrows(AgentStudioException.class,
            () -> promptService.deletePrompt(projectId, promptId, workspaceId));
    }

    @Test
    void testDeletePrompt_InUse() {
        String projectId = "project1";
        String promptId = "prompt-001";
        String workspaceId = "workspace1";

        PePrompt pePrompt = new PePrompt();
        pePrompt.setId(promptId);
        pePrompt.setTaskId("task-001");
        pePrompt.setVariables("[]");
        when(pePromptMapper.queryByPromptId(promptId, workspaceId)).thenReturn(pePrompt);

        PePrompt evalPrompt = new PePrompt();
        evalPrompt.setId(promptId);
        PeEvaluationTask evalTask = new PeEvaluationTask();
        evalTask.setPrompts(List.of(evalPrompt));
        when(peEvaluationTaskMapper.queryByTaskIdAndStatus("task-001", workspaceId))
            .thenReturn(List.of(evalTask));

        assertThrows(AgentStudioException.class,
            () -> promptService.deletePrompt(projectId, promptId, workspaceId));
    }

    @Test
    void testDeletePrompt_WithCandidateVariables() {
        String projectId = "project1";
        String promptId = "prompt-001";
        String workspaceId = "workspace1";

        CandidateVariable cv = new CandidateVariable();
        cv.setId("var-001");
        cv.setValue("值1");

        PePrompt pePrompt = new PePrompt();
        pePrompt.setId(promptId);
        pePrompt.setTaskId("task-001");
        pePrompt.setVariables(JSON.toJSONString(List.of(cv)));
        when(pePromptMapper.queryByPromptId(promptId, workspaceId)).thenReturn(pePrompt);
        when(peEvaluationTaskMapper.queryByTaskIdAndStatus("task-001", workspaceId))
            .thenReturn(Collections.emptyList());

        String result = promptService.deletePrompt(projectId, promptId, workspaceId);
        assertEquals("200 OK", result);
        verify(peVariableMapper).updateVariableRelationCount("var-001", workspaceId);
        verify(peVariableMapper).deleteVariablesByTaskId("task-001", workspaceId);
        verify(pePromptMapper).deleteByPromptId(promptId, workspaceId);
    }

    // ========== listPrompts ==========

    @Test
    void testListPrompts_Success() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        QueryCondition body = new QueryCondition();
        body.setTaskId("task-001");

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);

        PePrompt pePrompt = new PePrompt();
        pePrompt.setId("prompt-001");
        pePrompt.setTaskId("task-001");
        pePrompt.setType(1);
        pePrompt.setVariables("[]");
        when(pePromptMapper.queryByCondition(body, workspaceId)).thenReturn(List.of(pePrompt));

        PePromptPageInfo result = promptService.listPrompts(projectId, workspaceId, body);
        assertNotNull(result);
    }

    @Test
    void testListPrompts_HistoryType() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        QueryCondition body = new QueryCondition();
        body.setTaskId("task-001");
        body.setType(SourceType.HISTORY.getType());

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);

        PePrompt pePrompt = new PePrompt();
        pePrompt.setId("prompt-001");
        pePrompt.setTaskId("task-001");
        pePrompt.setType(SourceType.HISTORY.getType());
        pePrompt.setVariables("[]");
        when(pePromptMapper.queryByCondition(body, workspaceId)).thenReturn(List.of(pePrompt));
        when(peEvaluationTaskMapper.queryLatestSuccessEvaluationTaskIdByTaskId("task-001", workspaceId))
            .thenReturn(null);

        var result = promptService.listPrompts(projectId, workspaceId, body);
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    void testListPrompts_TaskNotExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        QueryCondition body = new QueryCondition();
        body.setTaskId("task-999");

        when(peTaskMapper.isIdExist("task-999", workspaceId)).thenReturn(false);

        assertThrows(AgentStudioException.class,
            () -> promptService.listPrompts(projectId, workspaceId, body));
    }

    // ========== updateSamllPrompt ==========

    @Test
    void testUpdateSmallPrompt_UpdateName() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        SmallPromptDto body = new SmallPromptDto();
        body.setId("prompt-001");
        body.setName("新名称");

        when(pePromptMapper.updateSmallPrompt(any(), eq(workspaceId))).thenReturn(1);
        when(pePromptMapper.checkDuplicateName("新名称", "prompt-001", workspaceId)).thenReturn(0);

        String result = promptService.updateSamllPrompt(projectId, workspaceId, body);
        assertNotNull(result);
    }

    @Test
    void testUpdateSmallPrompt_UpdateScore() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        SmallPromptDto body = new SmallPromptDto();
        body.setId("prompt-001");
        body.setManualScore(5);

        when(pePromptMapper.updateSmallPrompt(any(), eq(workspaceId))).thenReturn(1);
        when(pePromptMapper.checkDuplicateName(null, "prompt-001", workspaceId)).thenReturn(0);

        String result = promptService.updateSamllPrompt(projectId, workspaceId, body);
        assertNotNull(result);
    }

    @Test
    void testUpdateSmallPrompt_NoParam() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        SmallPromptDto body = new SmallPromptDto();
        body.setId("prompt-001");

        assertThrows(AgentStudioException.class,
            () -> promptService.updateSamllPrompt(projectId, workspaceId, body));
    }

    @Test
    void testUpdateSmallPrompt_DuplicateName_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        SmallPromptDto body = new SmallPromptDto();
        body.setId("prompt-001");
        body.setName("重名");

        when(pePromptMapper.updateSmallPrompt(any(), eq(workspaceId))).thenReturn(1);
        when(pePromptMapper.checkDuplicateName("重名", "prompt-001", workspaceId)).thenReturn(2);

        assertThrows(AgentStudioException.class,
            () -> promptService.updateSamllPrompt(projectId, workspaceId, body));
    }

    @Test
    void testUpdateSmallPrompt_DataNotExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        SmallPromptDto body = new SmallPromptDto();
        body.setId("prompt-999");
        body.setName("新名称");

        when(pePromptMapper.updateSmallPrompt(any(), eq(workspaceId))).thenReturn(0);

        assertThrows(AgentStudioException.class,
            () -> promptService.updateSamllPrompt(projectId, workspaceId, body));
    }

    // ========== checkUpdateOrDelete ==========

    @Test
    void testCheckUpdateOrDelete_Success() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        UpdateOrDeletePromptCheckDto body = new UpdateOrDeletePromptCheckDto();
        body.setTaskId("task-001");
        body.setPromptId("prompt-001");

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(pePromptMapper.queryByPromptId("prompt-001", workspaceId)).thenReturn(new PePrompt());
        when(peEvaluationTaskMapper.queryByTaskIdAndStatus("task-001", workspaceId))
            .thenReturn(Collections.emptyList());

        String result = promptService.checkUpdateOrDelete(projectId, workspaceId, body);
        assertNotNull(result);
    }

    @Test
    void testCheckUpdateOrDelete_InUse() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        UpdateOrDeletePromptCheckDto body = new UpdateOrDeletePromptCheckDto();
        body.setTaskId("task-001");
        body.setPromptId("prompt-001");

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);

        PePrompt checkPrompt = new PePrompt();
        checkPrompt.setTaskId("task-001");
        when(pePromptMapper.queryByPromptId("prompt-001", workspaceId)).thenReturn(checkPrompt);

        PePrompt evalPrompt = new PePrompt();
        evalPrompt.setId("prompt-001");
        PeEvaluationTask evalTask = new PeEvaluationTask();
        evalTask.setPrompts(List.of(evalPrompt));
        when(peEvaluationTaskMapper.queryByTaskIdAndStatus("task-001", workspaceId))
            .thenReturn(List.of(evalTask));

        assertThrows(AgentStudioException.class,
            () -> promptService.checkUpdateOrDelete(projectId, workspaceId, body));
    }

    @Test
    void testCheckUpdateOrDelete_TaskNotExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        UpdateOrDeletePromptCheckDto body = new UpdateOrDeletePromptCheckDto();
        body.setTaskId("task-999");
        body.setPromptId("prompt-001");

        when(peTaskMapper.isIdExist("task-999", workspaceId)).thenReturn(false);

        assertThrows(AgentStudioException.class,
            () -> promptService.checkUpdateOrDelete(projectId, workspaceId, body));
    }

    @Test
    void testCheckUpdateOrDelete_PromptNotExist_Throws() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        UpdateOrDeletePromptCheckDto body = new UpdateOrDeletePromptCheckDto();
        body.setTaskId("task-001");
        body.setPromptId("prompt-999");

        when(peTaskMapper.isIdExist("task-001", workspaceId)).thenReturn(true);
        when(pePromptMapper.queryByPromptId("prompt-999", workspaceId)).thenReturn(null);

        assertThrows(AgentStudioException.class,
            () -> promptService.checkUpdateOrDelete(projectId, workspaceId, body));
    }

    // ========== queryPrompt ==========

    @Test
    void testQueryPrompt_Success() {
        String projectId = "project1";
        String promptId = "prompt-001";
        String workspaceId = "workspace1";

        PePrompt pePrompt = new PePrompt();
        pePrompt.setId(promptId);
        pePrompt.setName("测试Prompt");
        pePrompt.setVariables("[]");
        when(pePromptMapper.queryByPromptId(promptId, workspaceId)).thenReturn(pePrompt);

        var result = promptService.queryPrompt(projectId, promptId, workspaceId);
        assertNotNull(result);
    }

    @Test
    void testQueryPrompt_WithVariables() {
        String projectId = "project1";
        String promptId = "prompt-001";
        String workspaceId = "workspace1";

        CandidateVariable cv = new CandidateVariable();
        cv.setId("var-001");
        cv.setValue("值1");

        PePrompt pePrompt = new PePrompt();
        pePrompt.setId(promptId);
        pePrompt.setName("带变量的Prompt");
        pePrompt.setVariables(JSON.toJSONString(List.of(cv)));
        when(pePromptMapper.queryByPromptId(promptId, workspaceId)).thenReturn(pePrompt);

        PeVariable peVariable = new PeVariable();
        peVariable.setId("var-001");
        peVariable.setName("变量1");
        peVariable.setKey("key1");
        when(peVariableMapper.selectVariablesByIds(List.of("var-001"), workspaceId))
            .thenReturn(List.of(peVariable));

        PePromptVo result = promptService.queryPrompt(projectId, promptId, workspaceId);
        assertNotNull(result);
        assertEquals(1, result.getVariableVo().size());
        assertEquals("值1", result.getVariableVo().get(0).getValue());
    }

    // ========== create ==========

    @Test
    void testCreate_CandidateType_Success() {
        PePromptDto body = new PePromptDto();
        body.setId(null);
        body.setName("候选模板1");
        body.setTaskId("task-001");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(Collections.emptyList());

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");

        when(peTaskMapper.isIdExist("task-001", "workspace1")).thenReturn(true);
        when(pePromptMapper.isNameExist("候选模板1", "task-001", null, "workspace1")).thenReturn(false);
        when(pePromptMapper.countCandidateTemplateByTaskId("task-001", "workspace1")).thenReturn(5);
        when(peVariableMapper.queryVariableKeysByTaskId("task-001", "workspace1")).thenReturn(Collections.emptyList());

        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple =
            new ImmutableTriple<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        PePrompt result = promptService.create(body, userInfo, triple, Collections.emptyList(), "workspace1");

        assertNotNull(result);
        verify(pePromptMapper).savePrompt(any(PePrompt.class));
        verify(peTaskMapper).raiseIterationNum("task-001", "workspace1");
    }

    @Test
    void testCreate_CandidateType_WithVariables() {
        VariableDto var1 = new VariableDto();
        var1.setKey("key1");
        var1.setName("变量1");
        var1.setValue("值1");

        PePromptDto body = new PePromptDto();
        body.setId(null);
        body.setName("候选模板2");
        body.setTaskId("task-001");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(List.of(var1));

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");

        when(peTaskMapper.isIdExist("task-001", "workspace1")).thenReturn(true);
        when(pePromptMapper.isNameExist("候选模板2", "task-001", null, "workspace1")).thenReturn(false);
        when(pePromptMapper.countCandidateTemplateByTaskId("task-001", "workspace1")).thenReturn(3);
        when(peVariableMapper.queryVariableKeysByTaskId("task-001", "workspace1")).thenReturn(Collections.emptyList());
        when(peVariableMapper.queryVariablesByTaskIdAndKey(any(), anyString(), anyString()))
            .thenReturn(Collections.emptyList());

        // handleVariable is private, construct triple via reflection
        PePrompt pePrompt = PePrompt.convertToPePrompt(body);
        pePrompt.setWorkspaceId("workspace1");
        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple =
            invokeHandleVariable(pePrompt, List.of(var1), true);

        PePrompt result = promptService.create(body, userInfo, triple, List.of(var1), "workspace1");
        assertNotNull(result);
    }

    @Test
    void testCreate_HistoryType_Success() {
        VariableDto var1 = new VariableDto();
        var1.setKey("k1");
        var1.setName("n1");
        var1.setValue("v1");

        PePromptDto body = new PePromptDto();
        body.setId(null);
        body.setName("");
        body.setTaskId("task-001");
        body.setType(SourceType.HISTORY.getType());
        body.setVariables(List.of(var1));

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");

        when(peTaskMapper.isIdExist("task-001", "workspace1")).thenReturn(true);
        when(pePromptMapper.isNameExist("", "task-001", null, "workspace1")).thenReturn(false);
        when(pePromptMapper.countCandidateTemplateByTaskId("task-001", "workspace1")).thenReturn(0);
        when(peTaskMapper.queryTaskNameById("task-001", "workspace1")).thenReturn("MyTask");

        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple =
            new ImmutableTriple<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        PePrompt result = promptService.create(body, userInfo, triple, List.of(var1), "workspace1");
        assertNotNull(result);
        verify(pePromptMapper).deleteRecordsByTaskId("task-001", "workspace1");
        verify(pePromptMapper).savePrompt(any(PePrompt.class));
    }

    @Test
    void testCreate_TaskNotExist_Throws() {
        PePromptDto body = new PePromptDto();
        body.setId(null);
        body.setName("测试");
        body.setTaskId("task-999");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(Collections.emptyList());

        when(peTaskMapper.isIdExist("task-999", "workspace1")).thenReturn(false);

        SimpleUser userInfo = new SimpleUser();
        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple =
            new ImmutableTriple<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(AgentStudioException.class,
            () -> promptService.create(body, userInfo, triple, Collections.emptyList(), "workspace1"));
    }

    @Test
    void testCreate_NameExist_Throws() {
        PePromptDto body = new PePromptDto();
        body.setId(null);
        body.setName("重名");
        body.setTaskId("task-001");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(Collections.emptyList());

        when(peTaskMapper.isIdExist("task-001", "workspace1")).thenReturn(true);
        when(pePromptMapper.isNameExist("重名", "task-001", null, "workspace1")).thenReturn(true);

        SimpleUser userInfo = new SimpleUser();
        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple =
            new ImmutableTriple<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(AgentStudioException.class,
            () -> promptService.create(body, userInfo, triple, Collections.emptyList(), "workspace1"));
    }

    @Test
    void testCreate_ExceedsCandidateTemplateQuota_Throws() {
        PePromptDto body = new PePromptDto();
        body.setId(null);
        body.setName("超额");
        body.setTaskId("task-001");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(Collections.emptyList());

        when(peTaskMapper.isIdExist("task-001", "workspace1")).thenReturn(true);
        when(pePromptMapper.isNameExist("超额", "task-001", null, "workspace1")).thenReturn(false);
        when(pePromptMapper.countCandidateTemplateByTaskId("task-001", "workspace1")).thenReturn(9);

        SimpleUser userInfo = new SimpleUser();
        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple =
            new ImmutableTriple<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(AgentStudioException.class,
            () -> promptService.create(body, userInfo, triple, Collections.emptyList(), "workspace1"));
    }

    // ========== update ==========

    @Test
    void testUpdate_Success() {
        VariableDto var1 = new VariableDto();
        var1.setKey("key1");
        var1.setName("变量1");
        var1.setValue("值1");

        PePromptDto body = new PePromptDto();
        body.setId("prompt-001");
        body.setName("更新模板");
        body.setTaskId("task-001");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(List.of(var1));

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");

        PePrompt oldPrompt = new PePrompt();
        oldPrompt.setId("prompt-001");
        oldPrompt.setVariables("[]");

        when(peTaskMapper.isIdExist("task-001", "workspace1")).thenReturn(true);
        when(pePromptMapper.isNameExist("更新模板", "task-001", "prompt-001", "workspace1")).thenReturn(false);
        when(pePromptMapper.queryByPromptId("prompt-001", "workspace1")).thenReturn(oldPrompt);

        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple =
            new ImmutableTriple<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        PePrompt result = promptService.update(body, userInfo, triple, List.of(var1), "workspace1");
        assertNotNull(result);
        verify(pePromptMapper).updatePrompt(any(PePrompt.class));
    }

    @Test
    void testUpdate_TaskNotExist_Throws() {
        PePromptDto body = new PePromptDto();
        body.setId("prompt-001");
        body.setName("更新模板");
        body.setTaskId("task-999");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(Collections.emptyList());

        when(peTaskMapper.isIdExist("task-999", "workspace1")).thenReturn(false);

        SimpleUser userInfo = new SimpleUser();
        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> triple =
            new ImmutableTriple<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertThrows(AgentStudioException.class,
            () -> promptService.update(body, userInfo, triple, Collections.emptyList(), "workspace1"));
    }

    // ========== handleVariable (private method, tested via reflection) ==========

    @Test
    void testHandleVariable_ExceedsQuota_Throws() {
        PePrompt pePrompt = new PePrompt();
        pePrompt.setTaskId("task-001");
        pePrompt.setWorkspaceId("workspace1");

        List<String> existingKeys = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            existingKeys.add("existing-key-" + i);
        }
        when(peVariableMapper.queryVariableKeysByTaskId("task-001", "workspace1")).thenReturn(existingKeys);

        VariableDto newVar = new VariableDto();
        newVar.setKey("new-key");
        newVar.setName("新变量");
        newVar.setValue("值");

        assertThrows(AgentStudioException.class,
            () -> invokeHandleVariable(pePrompt, List.of(newVar), true));
    }

    @Test
    void testHandleVariable_WithinQuota_Success() {
        PePrompt pePrompt = new PePrompt();
        pePrompt.setTaskId("task-001");
        pePrompt.setWorkspaceId("workspace1");

        when(peVariableMapper.queryVariableKeysByTaskId("task-001", "workspace1"))
            .thenReturn(List.of("existing-key-1"));
        when(peVariableMapper.queryVariablesByTaskIdAndKey(any(), anyString(), anyString()))
            .thenReturn(Collections.emptyList());

        VariableDto newVar = new VariableDto();
        newVar.setKey("new-key");
        newVar.setName("新变量");
        newVar.setValue("值");

        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> result =
            invokeHandleVariable(pePrompt, List.of(newVar), true);

        assertNotNull(result);
        assertEquals(1, result.getLeft().size());
        assertEquals(0, result.getMiddle().size());
        assertEquals(1, result.getRight().size());
    }

    @Test
    void testHandleVariable_UpdateExistingVariable() {
        PePrompt pePrompt = new PePrompt();
        pePrompt.setTaskId("task-001");
        pePrompt.setWorkspaceId("workspace1");

        when(peVariableMapper.queryVariableKeysByTaskId("task-001", "workspace1"))
            .thenReturn(List.of("key1"));
        PeVariable existingVar = new PeVariable();
        existingVar.setId("var-001");
        existingVar.setKey("key1");
        existingVar.setRelationCount(2);
        when(peVariableMapper.queryVariablesByTaskIdAndKey(any(), anyString(), anyString()))
            .thenReturn(List.of(existingVar));

        VariableDto updateVar = new VariableDto();
        updateVar.setKey("key1");
        updateVar.setName("更新变量");
        updateVar.setValue("新值");

        ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> result =
            invokeHandleVariable(pePrompt, List.of(updateVar), true);

        assertNotNull(result);
        assertEquals(0, result.getLeft().size());
        assertEquals(1, result.getMiddle().size());
        assertEquals(3, result.getMiddle().get(0).getRelationCount());
    }

    // ========== updatePrompt (separate method) ==========

    @Test
    void testUpdatePrompt_CandidateType_Success() {
        VariableDto var1 = new VariableDto();
        var1.setKey("key1");
        var1.setName("变量1");
        var1.setValue("值1");

        PePromptDto body = new PePromptDto();
        body.setId("prompt-001");
        body.setName("更新候选");
        body.setTaskId("task-001");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(List.of(var1));
        body.setModelConfig(new ModelConfig());

        when(peTaskMapper.isIdExist("task-001", "workspace1")).thenReturn(true);
        when(pePromptMapper.isNameExist("更新候选", "task-001", "prompt-001", "workspace1")).thenReturn(false);
        when(peVariableMapper.queryVariableKeysByTaskId("task-001", "workspace1")).thenReturn(List.of("key1"));
        PeVariable existingVar = new PeVariable();
        existingVar.setId("var-001");
        existingVar.setKey("key1");
        existingVar.setRelationCount(2);
        when(peVariableMapper.queryVariablesByTaskIdAndKey(any(), anyString(), anyString()))
            .thenReturn(List.of(existingVar));

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);

            String result = promptService.updatePrompt("project1", "workspace1", body);
            assertEquals("200 OK", result);
            verify(pePromptMapper).updatePrompt(any(PePrompt.class));
        }
    }

    @Test
    void testUpdatePrompt_TaskNotExist_Throws() {
        PePromptDto body = new PePromptDto();
        body.setId("prompt-001");
        body.setName("更新");
        body.setTaskId("task-999");
        body.setType(SourceType.CANDIDATE.getType());
        body.setVariables(Collections.emptyList());
        body.setModelConfig(new ModelConfig());

        when(peTaskMapper.isIdExist("task-999", "workspace1")).thenReturn(false);

        SimpleUser userInfo = new SimpleUser();
        userInfo.setUserName("testUser");
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUser).thenReturn(userInfo);

            assertThrows(AgentStudioException.class,
                () -> promptService.updatePrompt("project1", "workspace1", body));
        }
    }

    // ========== saveHistory ==========

    @Test
    void testSaveHistory_Success() {
        VariableDto var1 = new VariableDto();
        var1.setKey("k1");
        var1.setName("n1");
        var1.setValue("v1");

        PePrompt pePrompt = new PePrompt();
        pePrompt.setTaskId("task-001");
        pePrompt.setWorkspaceId("workspace1");
        pePrompt.setName("");

        when(peTaskMapper.queryTaskNameById("task-001", "workspace1")).thenReturn("MyTask");

        promptService.saveHistory(pePrompt, List.of(var1));

        verify(pePromptMapper).deleteRecordsByTaskId("task-001", "workspace1");
        verify(pePromptMapper).savePrompt(any(PePrompt.class));
        assertTrue(pePrompt.getName().startsWith("MyTask-"));
    }

    @Test
    void testSaveHistory_WithExistingName() {
        VariableDto var1 = new VariableDto();
        var1.setKey("k1");
        var1.setName("n1");
        var1.setValue("v1");

        PePrompt pePrompt = new PePrompt();
        pePrompt.setTaskId("task-001");
        pePrompt.setWorkspaceId("workspace1");
        pePrompt.setName("已有名称");

        when(peTaskMapper.queryTaskNameById("task-001", "workspace1")).thenReturn("MyTask");

        promptService.saveHistory(pePrompt, List.of(var1));

        assertEquals("已有名称", pePrompt.getName());
        verify(pePromptMapper).savePrompt(any(PePrompt.class));
    }

    @Test
    void testSaveHistory_EmptyVariables() {
        PePrompt pePrompt = new PePrompt();
        pePrompt.setTaskId("task-001");
        pePrompt.setWorkspaceId("workspace1");
        pePrompt.setName("历史模板");

        when(peTaskMapper.queryTaskNameById("task-001", "workspace1")).thenReturn("MyTask");

        promptService.saveHistory(pePrompt, Collections.emptyList());

        verify(pePromptMapper).savePrompt(any(PePrompt.class));
    }

    // ========== getVariableVos ==========

    @Test
    void testGetVariableVos_Success() {
        CandidateVariable cv = new CandidateVariable();
        cv.setId("var-001");
        cv.setValue("值1");
        String variables = JSON.toJSONString(List.of(cv));

        PeVariable peVariable = new PeVariable();
        peVariable.setId("var-001");
        peVariable.setName("变量1");
        peVariable.setKey("key1");

        when(peVariableMapper.selectVariablesByIds(List.of("var-001"), "workspace1"))
            .thenReturn(List.of(peVariable));

        var result = promptService.getVariableVos(variables, "workspace1");
        assertEquals(1, result.size());
        assertEquals("值1", result.get(0).getValue());
        assertEquals("key1", result.get(0).getKey());
    }

    @Test
    void testGetVariableVos_EmptyVariables() {
        String variables = "[]";
        var result = promptService.getVariableVos(variables, "workspace1");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetVariableVos_NullVariables_Throws() {
        assertThrows(Exception.class,
            () -> promptService.getVariableVos(null, "workspace1"));
    }

    /**
     * 反射调用 private handleVariable 方法，避免直接访问权限限制
     */
    @SuppressWarnings("unchecked")
    private ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>> invokeHandleVariable(
        PePrompt pePrompt, List<VariableDto> variables, boolean noPromptId) {
        try {
            var method = PromptService.class.getDeclaredMethod("handleVariable",
                PePrompt.class, List.class, boolean.class);
            method.setAccessible(true);
            return (ImmutableTriple<List<PeVariable>, List<PeVariable>, List<CandidateVariable>>)
                method.invoke(promptService, pePrompt, variables, noPromptId);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Failed to invoke handleVariable", e.getCause());
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke handleVariable via reflection", e);
        }
    }
}
