/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.DeveloperAgentListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDeveloperAgentsQo;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.service.workspace.WorkspaceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class DeveloperManagementServiceTest {

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private AgentMapper agentMapper;

    @InjectMocks
    private DeveloperManagementService developerManagementService;

    @Test
    void testExistPublishedAgent_BlankDomainId() {
        Boolean result = developerManagementService.existPublishedAgent("", System.currentTimeMillis());
        assertFalse(result);
    }

    @Test
    void testExistPublishedAgent_NullDomainId() {
        Boolean result = developerManagementService.existPublishedAgent(null, System.currentTimeMillis());
        assertFalse(result);
    }

    @Test
    void testExistPublishedAgent_HasPublished() {
        when(agentMapper.selectByDomainId(eq("domain-1"), eq(CommonConstant.AGENT_PUBLISHED), any(Timestamp.class)))
            .thenReturn(5);

        Boolean result = developerManagementService.existPublishedAgent("domain-1", System.currentTimeMillis());
        assertTrue(result);
    }

    @Test
    void testExistPublishedAgent_NoPublished() {
        when(agentMapper.selectByDomainId(eq("domain-1"), eq(CommonConstant.AGENT_PUBLISHED), any(Timestamp.class)))
            .thenReturn(0);

        Boolean result = developerManagementService.existPublishedAgent("domain-1", System.currentTimeMillis());
        assertFalse(result);
    }

    @Test
    void testListDeveloperAgents_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");
            reqCtx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            String xUserInfo = "{\"iamUserInfo\":{\"userId\":\"user-1\",\"domainId\":\"domain-1\",\"projectId\":\"proj-1\"}}";
            ListDeveloperAgentsQo qo = new ListDeveloperAgentsQo();
            qo.setWorkspaceId("ws-1");
            qo.setMarker(0);
            qo.setLimit(10);

            List<Agent> agents = new ArrayList<>();
            Agent agent = new Agent();
            agent.setAgentId("agent-1");
            agent.setName("Test Agent");
            agent.setType("agent");
            agents.add(agent);

            when(agentMapper.selectByProjectIdAndSearchCriteria(eq("proj-1"), any())).thenReturn(agents);

            DeveloperAgentListRsp result = developerManagementService.listDeveloperAgents(xUserInfo, qo);
            assertNotNull(result);
            assertNotNull(result.getData());
            assertEquals(1, result.getData().size());
        }
    }

    @Test
    void testListDeveloperAgents_EmptyWorkspaceId() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");
            reqCtx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            WorkspaceEntity workspace = new WorkspaceEntity();
            workspace.setId("ws-personal");
            when(workspaceService.getPersonalWorkspaceInfo("proj-1", "user-1")).thenReturn(workspace);

            String xUserInfo = "{\"iamUserInfo\":{\"userId\":\"user-1\",\"domainId\":\"domain-1\",\"projectId\":\"proj-1\"}}";
            ListDeveloperAgentsQo qo = new ListDeveloperAgentsQo();
            qo.setMarker(0);
            qo.setLimit(10);

            when(agentMapper.selectByProjectIdAndSearchCriteria(eq("proj-1"), any())).thenReturn(Collections.emptyList());

            DeveloperAgentListRsp result = developerManagementService.listDeveloperAgents(xUserInfo, qo);
            assertNotNull(result);
        }
    }

    @Test
    void testListDeveloperAgents_InvalidUserInfo() {
        String xUserInfo = "{}";
        ListDeveloperAgentsQo qo = new ListDeveloperAgentsQo();
        qo.setMarker(0);
        qo.setLimit(10);

        assertThrows(AgentStudioException.class,
            () -> developerManagementService.listDeveloperAgents(xUserInfo, qo));
    }
}
