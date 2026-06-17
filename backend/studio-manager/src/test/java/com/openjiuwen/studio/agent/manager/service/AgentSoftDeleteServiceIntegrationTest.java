/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.AgentListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListAgentsQo;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.rce.client.AgentRuntimeClient;
import com.openjiuwen.studio.agent.manager.service.workspace.WorkspaceMemberService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import com.openjiuwen.studio.prompt.engineering.service.PromptObsService;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

/**
 * AgentSoftDeleteServiceIntegrationTest Agent软删除集成测试类
 * 参考 EnvironmentServiceManagerServiceTest 的写法
 */
@Transactional
public class AgentSoftDeleteServiceIntegrationTest extends BaseTest {

    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    @Autowired
    private AgentManagementService agentManagementService;

    @MockitoBean(name = "mgObsService")
    private MgObsService mgObsService;

    @MockitoBean(name = "agentRuntimeClient")
    private AgentRuntimeClient agentRuntimeClient;

    @MockitoBean(name = "workSpaceMemberService")
    private WorkspaceMemberService workSpaceMemberService;

    @MockitoBean(name = "agentSpaceService")
    private AgentSpaceService agentSpaceService;

    @MockitoBean(name = "promptObsService")
    private PromptObsService promptObsService;

    @BeforeAll
    public static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_CREATOR);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_CREATOR_ID);
        mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(Constants.TEST_WORKSPACE_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainName).thenReturn("op-svc");
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("zh-cn");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(agentManagementService, "mgObsService", mgObsService);
        ReflectionTestUtils.setField(agentManagementService, "agentRuntimeClient", agentRuntimeClient);
        ReflectionTestUtils.setField(agentManagementService, "workSpaceMemberService", workSpaceMemberService);
        ReflectionTestUtils.setField(agentManagementService, "agentSpaceService", agentSpaceService);
        ReflectionTestUtils.setField(agentManagementService, "publishAgentBuilderEnable", true);
        doNothing().when(mgObsService).deleteObsObjects(anyString());
        doNothing().when(agentSpaceService).delete(anyString(), anyString());
    }

    @Test
    @Sql(scripts = {"classpath:sql/agent_soft_delete_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testSoftDeleteAgent() {
        // 获取删除前的Agent列表
        ListAgentsQo listQo = new ListAgentsQo();
        listQo.setOffset(0);
        listQo.setLimit(10);
        AgentListRsp listBefore = agentManagementService.listAgents(Constants.TEST_PROJECT_ID, listQo);
        int countBefore = Math.toIntExact(listBefore.getCount());

        // 执行软删除
        agentManagementService.deleteAgent(Constants.TEST_PROJECT_ID, "test_agent_for_delete", Constants.TEST_WORKSPACE_ID);

        // 验证删除后列表减少
        int countAfter = Math.toIntExact(agentManagementService.listAgents(Constants.TEST_PROJECT_ID, listQo).getCount());
        Assertions.assertEquals(countBefore - 1, countAfter);

        // 验证无法再查到已删除的Agent
        Assertions.assertThrows(AgentStudioException.class, () -> {
            agentManagementService.getAgent(Constants.TEST_PROJECT_ID, "test_agent_for_delete");
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/agent_soft_delete_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testSoftDeleteAgentWithRelatedResources() {
        // 删除有关联资源的Agent
        agentManagementService.deleteAgent(Constants.TEST_PROJECT_ID, "test_agent_with_releases", Constants.TEST_WORKSPACE_ID);

        // 验证已删除的Agent无法获取
        Assertions.assertThrows(AgentStudioException.class, () -> {
            agentManagementService.getAgent(Constants.TEST_PROJECT_ID, "test_agent_with_releases");
        });
    }

    @Test
    void testSoftDeleteAgentWithEmptyId() {
        // 删除空ID应该抛出异常
        Assertions.assertThrows(Exception.class, () -> {
            agentManagementService.deleteAgent(Constants.TEST_PROJECT_ID, "", Constants.TEST_WORKSPACE_ID);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/agent_soft_delete_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testSoftDeleteAgentWithNotExistId() {
        // 删除不存在的Agent应该抛出异常
        Assertions.assertThrows(AgentStudioException.class, () -> {
            agentManagementService.deleteAgent(Constants.TEST_PROJECT_ID, "non_existent_agent_id", Constants.TEST_WORKSPACE_ID);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/agent_soft_delete_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testSoftDeleteAgentPreservesHistory() {
        // 获取删除前的Agent数量（应该有3个）
        ListAgentsQo listQo = new ListAgentsQo();
        listQo.setOffset(0);
        listQo.setLimit(10);
        int countBefore = Math.toIntExact(agentManagementService.listAgents(Constants.TEST_PROJECT_ID, listQo).getCount());
        Assertions.assertEquals(3, countBefore, "删除前应该有3个Agent");

        // 删除一个Agent后，应该保留历史记录
        agentManagementService.deleteAgent(Constants.TEST_PROJECT_ID, "test_agent_history", Constants.TEST_WORKSPACE_ID);

        // 验证主表中的Agent已被删除
        // 通过查询列表验证（deleted = 0 的Agent）
        int countAfter = Math.toIntExact(agentManagementService.listAgents(Constants.TEST_PROJECT_ID, listQo).getCount());
        Assertions.assertEquals(2, countAfter, "删除后应该有2个Agent");
    }

    @Test
    @Sql(scripts = {"classpath:sql/agent_soft_delete_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testListAgentsAfterDelete() {
        // 先删除一个Agent
        agentManagementService.deleteAgent(Constants.TEST_PROJECT_ID, "test_agent_for_delete", Constants.TEST_WORKSPACE_ID);

        // 列出剩余的Agent
        ListAgentsQo listQo = new ListAgentsQo();
        listQo.setOffset(0);
        listQo.setLimit(10);
        AgentListRsp result = agentManagementService.listAgents(Constants.TEST_PROJECT_ID, listQo);

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getAgentList());
        // 验证被删除的Agent不在列表中
        boolean found = result.getAgentList().stream()
            .anyMatch(agent -> "test_agent_for_delete".equals(agent.getAgentId()));
        Assertions.assertFalse(found);
    }

    @Test
    @Sql(scripts = {"classpath:sql/agent_soft_delete_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testSoftDeleteMultipleAgents() {
        // 删除多个Agent
        agentManagementService.deleteAgent(Constants.TEST_PROJECT_ID, "test_agent_for_delete", Constants.TEST_WORKSPACE_ID);
        agentManagementService.deleteAgent(Constants.TEST_PROJECT_ID, "test_agent_with_releases", Constants.TEST_WORKSPACE_ID);

        // 列出剩余的Agent
        ListAgentsQo listQo = new ListAgentsQo();
        listQo.setOffset(0);
        listQo.setLimit(10);
        AgentListRsp result = agentManagementService.listAgents(Constants.TEST_PROJECT_ID, listQo);

        // 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getAgentList().size());
    }
}