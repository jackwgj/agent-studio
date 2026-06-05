/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.AppInfo;
import com.openjiuwen.studio.agent.manager.dto.AppListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListAppsQo;
import com.openjiuwen.studio.agent.manager.dto.SearchCriteria;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.rce.service.JiuWenService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 功能描述
 *
 */
@Transactional
@Sql(scripts = {"classpath:sql/app_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AppManagementServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    @Autowired
    WorkflowManagementService workflowManagementService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    @Autowired
    private AgentManagementService agentManagementService;

    @Autowired
    private AppManagementService appManagementService;

    @Autowired
    private JiuWenService jiuWenService;

    private AutoCloseable mockitoCloseable;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(Constants.TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("test_copy_user_id");
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("zh-cn");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(agentManagementService, "mgObsService", mgObsService);
        ReflectionTestUtils.setField(workflowManagementService, "obsService", mgObsService);
        ReflectionTestUtils.setField(appManagementService, "obsService", mgObsService);
        ReflectionTestUtils.setField(workflowManagementService, "opSvcProjectId", Constants.TEST_PROJECT_ID);

        // AgentRuntimeClient runtimeClient = Mockito.mock(AgentRuntimeClient.class);
        //
        // JSONObject obj = JSONObject.parseObject(
        //     "{\"id\":\"chat-856957422b584881894b0eb52e69d81a\",\"model\":\"qwen3-235b-a22b\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"123\",\"tool_calls\":[]},\"logprobs\":null,\"finish_reason\":\"stop\",\"stop_reason\":null}]}");
        //
        // Mockito.when(runtimeClient.askModel(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
        //     .thenReturn(ResponseEntity.ok(obj));
        // ReflectionTestUtils.setField(jiuWenService, "runtimeClient", runtimeClient);
    }

    @Test
    void testListApps() {
        // setup
        ListAppsQo body = new ListAppsQo().setOffset(0).setLimit(10);

        // run the test
        AppListRsp result = appManagementService.listApps(Constants.TEST_PROJECT_ID, body);
        assertNotNull(result);

        result = appManagementService.listApps(Constants.TEST_PROJECT_ID, body.setLimit(1));
        assertNotNull(result);
        assertEquals(1, result.getAppList().size());

        body.setTagId(Constants.TEST_TAG_ID);
        AppListRsp resultWithTag = appManagementService.listApps(Constants.TEST_PROJECT_ID, body.setLimit(10));
        assertNotNull(resultWithTag);

        body.setTagId("test");
        AppListRsp resultWithInvalidTag = appManagementService.listApps(Constants.TEST_PROJECT_ID, body);
        assertNotNull(resultWithInvalidTag);
        assertEquals(0, resultWithInvalidTag.getCount());
    }

    @Test
    @Transactional
    @Sql(scripts = {"classpath:sql/agent_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_copyAgentApp() {
        // 初始化百宝箱agent应用数据
        when(mgObsService.uploadObsFile(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
            "test_dsl_path");
        when(mgObsService.downloadObsFile(eq("test_dsl_path"))).thenReturn(
            TestUtil.getStringFromFile("classpath:response/agent_dsl.json"));
        agentManagementService.publishAgent(Constants.TEST_PROJECT_ID, Constants.TEST_AGENT_ID,
            Constants.TEST_WORKSPACE_ID, List.of("da112ce6-5951-11ef-abd9-607d092d9149"));

        // 复制百宝箱agent应用
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSource(Constants.TEST_AGENT_TYPE);
        AppInfo appInfo = appManagementService.copyApp("test_copy_project_id", Constants.TEST_AGENT_ID, "default",
            "default", searchCriteria);
        assertNotNull(appInfo);
    }

    @Test
    @Transactional
    @Sql(scripts = {"classpath:sql/workflow_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_copyWorkflowApp() {
        // 初始化百宝箱workflow应用数据
        String filePath = "workflow-ir/workflow/flow/test_llm_workflow_id.json";
        when(mgObsService.downloadObsFile(eq(filePath))).thenReturn(
            TestUtil.getStringFromFile("classpath:response/test_llm_workflow_id.json"));
        workflowManagementService.publishWorkflow(Constants.TEST_PROJECT_ID, Constants.TEST_LLM_WORKFLOW_ID,
            List.of("da112ce6-5951-11ef-abd9-607d092d9149"));

        // 复制百宝箱workflow应用
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSource(Constants.TEST_WORKFLOW_TYPE);
        AppInfo appInfo = appManagementService.copyApp("test_copy_project_id", Constants.TEST_LLM_WORKFLOW_ID,
            "default", "default", searchCriteria);
        assertNotNull(appInfo);
    }
}
