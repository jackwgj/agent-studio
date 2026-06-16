/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkuManageServiceTest {

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private WorkflowMapper workflowMapper;

    @InjectMocks
    private SkuManageService skuManageService;

    @Test
    void testValidateExceededLimitOrNot_NonHcEnv() {
        ReflectionTestUtils.setField(skuManageService, "envType", "public");
        assertDoesNotThrow(() -> skuManageService.validateExceededLimitOrNot("ATTR_CODE", 10));
    }

    @Test
    void testValidateExceededLimitOrNot_HcEnv() {
        ReflectionTestUtils.setField(skuManageService, "envType", "HC");
        assertDoesNotThrow(() -> skuManageService.validateExceededLimitOrNot("ATTR_CODE", 10));
    }

    @Test
    void testValidateAttrEnable_Disabled() {
        ReflectionTestUtils.setField(skuManageService, "skuValidateEnable", false);
        assertDoesNotThrow(() -> skuManageService.validateAttrEnable("ATTR_CODE"));
    }

    @Test
    void testValidateAttrEnable_Enabled() {
        ReflectionTestUtils.setField(skuManageService, "skuValidateEnable", true);
        assertDoesNotThrow(() -> skuManageService.validateAttrEnable("ATTR_CODE"));
    }

    @Test
    void testValidateInsertAppCountExceededLimitOrNot_NonHcEnv() {
        ReflectionTestUtils.setField(skuManageService, "envType", "public");
        assertDoesNotThrow(() -> skuManageService.validateInsertAppCountExceededLimitOrNot());
    }

    @Test
    void testValidateInsertAppCountExceededLimitOrNot_HcEnv() {
        ReflectionTestUtils.setField(skuManageService, "envType", "HC");
        assertDoesNotThrow(() -> skuManageService.validateInsertAppCountExceededLimitOrNot());
    }

    @Test
    void testQueryAppCountExceededLimit() {
        List<Integer> result = skuManageService.queryAppCountExceededLimit();
        assertEquals(2, result.size());
        assertEquals(-1, result.get(0));
        assertEquals(-1, result.get(1));
    }

    @Test
    void testValidateImportAppCountExceededLimitOrNot() {
        assertDoesNotThrow(() ->
            skuManageService.validateImportAppCountExceededLimitOrNot(List.of("a1"), List.of("w1"), "ws1"));
    }

    @Test
    void testCalculateAppendAgentCount_EmptyList() {
        assertEquals(0, skuManageService.calculateAppendAgentCount(null, "ws1"));
        assertEquals(0, skuManageService.calculateAppendAgentCount(new ArrayList<>(), "ws1"));
    }

    @Test
    void testCalculateAppendAgentCount_WithAgents() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("proj1");

            List<String> importList = List.of("agent1", "agent2");
            Agent existingAgent = new Agent();
            existingAgent.setAgentId("agent1");

            when(agentMapper.selectByIdsAndProjectIdAndWorkspaceId("proj1", "ws1", importList))
                .thenReturn(List.of(existingAgent));

            int result = skuManageService.calculateAppendAgentCount(importList, "ws1");
            assertEquals(1, result);
        }
    }

    @Test
    void testCalculateAppendAgentCount_AllExist() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("proj1");

            List<String> importList = List.of("agent1");
            Agent existingAgent = new Agent();
            existingAgent.setAgentId("agent1");

            when(agentMapper.selectByIdsAndProjectIdAndWorkspaceId("proj1", "ws1", importList))
                .thenReturn(List.of(existingAgent));

            int result = skuManageService.calculateAppendAgentCount(importList, "ws1");
            assertEquals(0, result);
        }
    }

    @Test
    void testCalculateAppendWorkflowCount_EmptyList() {
        assertEquals(0, skuManageService.calculateAppendWorkflowCount(null, "ws1"));
        assertEquals(0, skuManageService.calculateAppendWorkflowCount(new ArrayList<>(), "ws1"));
    }

    @Test
    void testCalculateAppendWorkflowCount_WithWorkflows() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("proj1");

            List<String> importList = List.of("wf1", "wf2");
            WorkflowEntity existingWf = new WorkflowEntity();
            existingWf.setId("wf1");

            when(workflowMapper.selectByWorkflowIdList("proj1", "ws1", importList))
                .thenReturn(List.of(existingWf));

            int result = skuManageService.calculateAppendWorkflowCount(importList, "ws1");
            assertEquals(1, result);
        }
    }

    @Test
    void testCalculateAppendWorkflowCount_WithBlankEntries() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("proj1");

            List<String> importList = List.of("wf1", "", "wf2");
            List<String> filteredList = List.of("wf1", "wf2");

            when(workflowMapper.selectByWorkflowIdList("proj1", "ws1", filteredList))
                .thenReturn(new ArrayList<>());

            int result = skuManageService.calculateAppendWorkflowCount(importList, "ws1");
            assertEquals(2, result);
        }
    }
}
