/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSON;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.DeveloperAgentListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDeveloperAgentsQo;
import com.openjiuwen.studio.agent.manager.dto.SearchCriteria;
import com.openjiuwen.studio.agent.manager.dto.iam.DeveloperHeaderUserInfo;
import com.openjiuwen.studio.agent.manager.dto.iam.DeveloperUserInfo;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.service.workspace.WorkspaceService;
import com.openjiuwen.studio.prompt.engineering.utils.HttpUtil;

import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class DeveloperManagementServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkspaceService workspaceService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentMapper agentMapper;

    @InjectMocks
    private DeveloperManagementService developerManagementService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_listDeveloperAgents_should_return_not_null() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
             MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
             MockedStatic<HttpUtil> mockedStaticHttpUtil = mockStatic(HttpUtil.class, RETURNS_DEEP_STUBS)) {
            // Given
            DeveloperHeaderUserInfo userInfoHeader = new DeveloperHeaderUserInfo();
            DeveloperUserInfo iamUserInfo = new DeveloperUserInfo();
            iamUserInfo.setDomainId("not_empty");
            iamUserInfo.setUserId("not_empty");
            iamUserInfo.setProjectId("not_empty");
            userInfoHeader.setIamUserInfo(iamUserInfo);
            mockedStaticJSON.when(() -> JSON.parseObject(anyString(), eq(DeveloperHeaderUserInfo.class))).thenReturn(userInfoHeader);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestProjectId()).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("not_empty");

            mockedStaticHttpUtil.when(() -> HttpUtil.getLanguage()).thenReturn("not_empty");

            List<Agent> agentList = new ArrayList<>();
            Agent agent = new Agent();
            agentList.add(agent);
            when(agentMapper.selectByProjectIdAndSearchCriteria(anyString(), any(SearchCriteria.class))).thenReturn(agentList);

            when(workspaceService.getPersonalWorkspaceInfo(anyString(), anyString()).getId()).thenReturn("not_empty");

            ListDeveloperAgentsQo listDeveloperAgentsQo = new ListDeveloperAgentsQo();

            // When
            DeveloperAgentListRsp result = developerManagementService.listDeveloperAgents("not_empty", listDeveloperAgentsQo);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_listDeveloperAgents_should_return_not_null1() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
             MockedStatic<Strings> mockedStaticStringUtils = mockStatic(Strings.class, RETURNS_DEEP_STUBS);
             MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
             MockedStatic<HttpUtil> mockedStaticHttpUtil = mockStatic(HttpUtil.class, RETURNS_DEEP_STUBS)) {
            // Given
            DeveloperHeaderUserInfo userInfoHeader = new DeveloperHeaderUserInfo();
            DeveloperUserInfo iamUserInfo = new DeveloperUserInfo();
            iamUserInfo.setDomainId("not_empty");
            iamUserInfo.setUserId("not_empty");
            iamUserInfo.setProjectId("not_empty");
            userInfoHeader.setIamUserInfo(iamUserInfo);
            mockedStaticJSON.when(() -> JSON.parseObject(anyString(), eq(DeveloperHeaderUserInfo.class))).thenReturn(userInfoHeader);

            mockedStaticStringUtils.when(() -> Strings.CS.equals(anyString(), eq("en-US"))).thenReturn(true);
            mockedStaticStringUtils.when(() -> Strings.CS.equals(anyString(), anyString())).thenReturn(true);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestProjectId()).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("not_empty");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("not_empty");

            mockedStaticHttpUtil.when(() -> HttpUtil.getLanguage()).thenReturn("not_empty");

            List<Agent> agentList = new ArrayList<>();
            Agent agent = new Agent();
            agentList.add(agent);
            when(agentMapper.selectByProjectIdAndSearchCriteria(anyString(), any(SearchCriteria.class))).thenReturn(agentList);

            when(workspaceService.getPersonalWorkspaceInfo(anyString(), anyString()).getId()).thenReturn("not_empty");

            ListDeveloperAgentsQo listDeveloperAgentsQo = new ListDeveloperAgentsQo();

            // When
            DeveloperAgentListRsp result = developerManagementService.listDeveloperAgents("not_empty", listDeveloperAgentsQo);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_listDeveloperAgents_should_return_not_null4() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
             MockedStatic<Strings> mockedStaticStringUtils = mockStatic(Strings.class, RETURNS_DEEP_STUBS);
             MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
             MockedStatic<HttpUtil> mockedStaticHttpUtil = mockStatic(HttpUtil.class, RETURNS_DEEP_STUBS)) {
            // Given
            DeveloperHeaderUserInfo userInfoHeader = new DeveloperHeaderUserInfo();
            DeveloperUserInfo iamUserInfo = new DeveloperUserInfo();
            iamUserInfo.setDomainId("");
            iamUserInfo.setUserId("");
            iamUserInfo.setProjectId("");
            userInfoHeader.setIamUserInfo(iamUserInfo);
            mockedStaticJSON.when(() -> JSON.parseObject(anyString(), eq(DeveloperHeaderUserInfo.class))).thenReturn(userInfoHeader);

            mockedStaticStringUtils.when(() -> Strings.CS.equals(anyString(), eq("en-US"))).thenReturn(true);
            mockedStaticStringUtils.when(() -> Strings.CS.equals(anyString(), anyString())).thenReturn(true);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestProjectId()).thenReturn("");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn("");

            mockedStaticHttpUtil.when(() -> HttpUtil.getLanguage()).thenReturn("");

            List<Agent> agentList = new ArrayList<>();
            Agent agent = new Agent();
            agentList.add(agent);
            when(agentMapper.selectByProjectIdAndSearchCriteria(anyString(), any(SearchCriteria.class))).thenReturn(agentList);

            when(workspaceService.getPersonalWorkspaceInfo(anyString(), anyString()).getId()).thenReturn("");

            ListDeveloperAgentsQo listDeveloperAgentsQo = new ListDeveloperAgentsQo();

            // When
            DeveloperAgentListRsp result = developerManagementService.listDeveloperAgents("", listDeveloperAgentsQo);

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_listDeveloperAgents_should_throw_exception() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS);
             MockedStatic<Strings> mockedStaticStringUtils = mockStatic(Strings.class, RETURNS_DEEP_STUBS);
             MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
             MockedStatic<HttpUtil> mockedStaticHttpUtil = mockStatic(HttpUtil.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticJSON.when(() -> JSON.parseObject(anyString(), eq(DeveloperHeaderUserInfo.class))).thenReturn(null);

            mockedStaticStringUtils.when(() -> Strings.CS.equals(anyString(), eq("en-US"))).thenReturn(true);
            mockedStaticStringUtils.when(() -> Strings.CS.equals(anyString(), anyString())).thenReturn(true);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestProjectId()).thenReturn(null);
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn(null);
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId()).thenReturn(null);

            mockedStaticHttpUtil.when(() -> HttpUtil.getLanguage()).thenReturn(null);

            when(agentMapper.selectByProjectIdAndSearchCriteria(anyString(), any(SearchCriteria.class))).thenReturn(null);

            when(workspaceService.getPersonalWorkspaceInfo(anyString(), anyString()).getId()).thenReturn(null);

            // Then
            assertThrows(AgentStudioException.class, () -> developerManagementService.listDeveloperAgents(null, null));
        }
    }


    @Test
    void testExistPublishedAgent() {
        boolean result = developerManagementService.existPublishedAgent("", System.currentTimeMillis());
        Assertions.assertFalse(result);
        boolean result2 = developerManagementService.existPublishedAgent("test_domain_id", System.currentTimeMillis());
        Assertions.assertFalse(result2);
    }
}
