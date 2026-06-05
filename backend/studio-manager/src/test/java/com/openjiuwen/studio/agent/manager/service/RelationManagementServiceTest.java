/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_WORKSPACE_ID;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.McpServerReference;
import com.openjiuwen.studio.agent.manager.entity.MappingEntity;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RelationManagementService测试类
 *
 */
@Transactional
public class RelationManagementServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<CommonUtil> commonUtilMockedStatic;

    @Autowired
    private RelationManagementService relationManagementService;

    private AutoCloseable mockitoCloseable;

    @BeforeAll
    static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        commonUtilMockedStatic = Mockito.mockStatic(CommonUtil.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(Constants.TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_CREATOR_ID);
        commonUtilMockedStatic.when(CommonUtil::getTenantId).thenReturn("system");
        commonUtilMockedStatic.when(CommonUtil::getDeptCode).thenReturn("system");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        commonUtilMockedStatic.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        mockitoCloseable = MockitoAnnotations.openMocks(this);

        when(RequestContextUtils.getRequestUserId()).thenReturn(Constants.TEST_CREATOR_ID);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    @Sql(scripts = {"classpath:sql/relation_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testHandleAgentMcp() {
        List<MappingEntity> mcpReferences = new ArrayList<>();
        MappingEntity mcpMapping = new MappingEntity();
        mcpMapping.setAppId("test_agent_id_0818001");
        mcpMapping.setAppType(CommonConstant.AGENT_TYPE);
        mcpMapping.setResourceId("276a2cd7b4b646f793d7d1da8ae4bf54");
        mcpMapping.setResourceType(CommonConstant.MCP_TYPE);
        mcpReferences.add(mcpMapping);
        List<MappingEntity> list = relationManagementService.createAgentMcp(Constants.TEST_PROJECT_ID,
            TEST_WORKSPACE_ID, "test_agent_id_0818001", null, mcpReferences);
        Assertions.assertNotNull(list);
    }

    @Test
    @Sql(scripts = {"classpath:sql/relation_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testUpdateAgentMcp() {
        List<MappingEntity> mcpReferences = new ArrayList<>();
        MappingEntity mcpMapping = new MappingEntity();
        mcpMapping.setAppId("test_agent_id_0818001");
        mcpMapping.setAppType(CommonConstant.AGENT_TYPE);
        mcpMapping.setResourceId("276a2cd7b4b646f793d7d1da8ae4bf54");
        mcpMapping.setResourceType(CommonConstant.MCP_TYPE);
        mcpMapping.setResourceChooseTools("[]");
        mcpReferences.add(mcpMapping);
        List<MappingEntity> list = relationManagementService.updateAgentMcp(Constants.TEST_PROJECT_ID,
            TEST_WORKSPACE_ID, "test_agent_id_0818001", null, mcpReferences);
        Assertions.assertNotNull(list);
    }

    @Test
    @Sql(scripts = {"classpath:sql/relation_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testListAgentMcpServers() {
        mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(TEST_WORKSPACE_ID);
        mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(TEST_WORKSPACE_ID);
        commonUtilMockedStatic.when(CommonUtil::getTenantId).thenReturn("system");
        commonUtilMockedStatic.when(CommonUtil::getDeptCode).thenReturn("system");
        List<McpServerReference> list = relationManagementService.listAgentMcpServers("test_agent_id_0818001");
        Assertions.assertNotNull(list);
    }
}
