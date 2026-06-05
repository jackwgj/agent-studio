/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_PROJECT_ID;
import static com.openjiuwen.studio.agent.manager.constant.Constants.TEST_WORKSPACE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.ListRouterStrategyQo;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceRsp;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyBaseInfo;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyListResponse;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyRequest;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Transactional
public class RouterStrategyMgmtServiceTest extends BaseTest {
    private static final String MOCK_MODEL_SERVICE_ID = "64c8e9bb-7ded-4fc9-b63e-9ff3f0e7a588";

    private static final String MOCK_STRATEGY_NAME = "TestModelRouter";

    private static final String MOCK_STRATEGY_ID = "TsetStrategyId";

    @Autowired
    private RouterStrategyMgmtService strategyMgmtService;

    private static MockedStatic<RequestContextUtils> mockedStatic;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    @Autowired
    private ModelServiceManager modelServiceManager;

    @BeforeAll
    public static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_USER_ID);
        mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(TEST_WORKSPACE_ID);
    }

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(modelServiceManager, "obsService", mgObsService);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
    }

    private RouterStrategyRequest createRouterStrategyRequest() {
        return new RouterStrategyRequest().setStrategyKey("strategyKey")
            .setStrategyDescription("decs")
            .setStrategyName("StrategyName")
            .setStrategyRetryCount(1)
            .setStrategyTimeout(1000)
            .setModelServiceList(Collections.singletonList(new ModelServiceRsp().setId(MOCK_MODEL_SERVICE_ID)));
    }

    @Test
    @Sql(scripts = {"classpath:sql/router_strategy_db.sql", "classpath:sql/model_service_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void test_create() {
        RouterStrategyRequest request = createRouterStrategyRequest();
        request.setStrategyName(MOCK_STRATEGY_NAME);

        try {
            strategyMgmtService.createRouterStrategy(TEST_PROJECT_ID, TEST_WORKSPACE_ID, request);
        } catch (AgentStudioException e) {
            assertEquals(StudioError.ROUTER_STRATEGY_NAME_REPEATED, e.getErrorCode());
        }

        request.setStrategyName("testName");

        try {
            // 模型服务不存在
            List<ModelServiceRsp> models = new ArrayList<>();
            models.add(new ModelServiceRsp().setId("not_exist"));
            request.setModelServiceList(models);
            strategyMgmtService.createRouterStrategy(TEST_PROJECT_ID, TEST_WORKSPACE_ID, request);
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_REQUEST_BODY_INVALID_MODEL_NOT_EXIST, e.getErrorCode());
        }

        try {
            // 模型服务重复
            List<ModelServiceRsp> models = new ArrayList<>();
            models.add(new ModelServiceRsp().setId("not_exist"));
            models.add(new ModelServiceRsp().setId("not_exist"));
            request.setModelServiceList(models);
            strategyMgmtService.createRouterStrategy(TEST_PROJECT_ID, TEST_WORKSPACE_ID, request);
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_REQUEST_BODY_INVALID, e.getErrorCode());
        }

        try {
            // 模型服务部分不存在
            List<ModelServiceRsp> models = new ArrayList<>();
            models.add(new ModelServiceRsp().setId("not_exist"));
            models.add(new ModelServiceRsp().setId(MOCK_MODEL_SERVICE_ID));
            request.setModelServiceList(models);
            strategyMgmtService.createRouterStrategy(TEST_PROJECT_ID, TEST_WORKSPACE_ID, request);
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_REQUEST_BODY_INVALID_MODEL_NOT_EXIST, e.getErrorCode());
        }

        // when(mgObsService.uploadObsFile(Mockito.anyString(), Mockito.anyString(), -1)).thenReturn("");

        String filePath = "workflow-ir/workflow/flow/test_prologue_workflow_id.json";
        when(mgObsService.downloadObsFile(eq(filePath))).thenReturn(
            TestUtil.getStringFromFile("classpath:response/test_prologue_workflow_id.json"));

        List<ModelServiceRsp> models = new ArrayList<>();
        models.add(new ModelServiceRsp().setId(MOCK_MODEL_SERVICE_ID));
        request.setModelServiceList(models);
        RouterStrategyBaseInfo base = strategyMgmtService.createRouterStrategy(TEST_PROJECT_ID, TEST_WORKSPACE_ID,
            request);
        assertNotNull(base);
    }

    @Test
    @Sql(scripts = {"classpath:sql/router_strategy_db.sql", "classpath:sql/model_service_db.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void test_delete(){
        try{
            strategyMgmtService.deleteRouterStrategy(TEST_PROJECT_ID, TEST_WORKSPACE_ID, MOCK_STRATEGY_ID );
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_ROUTER_STRATEGY_NOT_EXIST, e.getErrorCode());
        }

        strategyMgmtService.deleteRouterStrategy("test_project_id", TEST_WORKSPACE_ID, "test_model_router" );

    }

    @Test
    @Sql(scripts = {"classpath:sql/router_strategy_db.sql", "classpath:sql/model_service_db.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void test_list() {
        ListRouterStrategyQo listRouterStrategyQo = mock(ListRouterStrategyQo.class);
        when(listRouterStrategyQo.getWorkspaceId()).thenReturn(TEST_WORKSPACE_ID);
        when(listRouterStrategyQo.getQuery()).thenReturn(null);
        when(listRouterStrategyQo.getPageSize()).thenReturn(10);
        when(listRouterStrategyQo.getPageNum()).thenReturn(2);
        when(listRouterStrategyQo.getSortBy()).thenReturn(null);

        RouterStrategyListResponse res = strategyMgmtService.listRouterStrategy(TEST_PROJECT_ID, listRouterStrategyQo);
        assertEquals(0, res.getData().size());
        assertEquals(1, res.getTotal().intValue());

        ListRouterStrategyQo listRouterStrategyQo1 = mock(ListRouterStrategyQo.class);
        when(listRouterStrategyQo1.getWorkspaceId()).thenReturn(TEST_WORKSPACE_ID);
        when(listRouterStrategyQo1.getQuery()).thenReturn(null);
        when(listRouterStrategyQo1.getPageSize()).thenReturn(10);
        when(listRouterStrategyQo1.getPageNum()).thenReturn(1);
        when(listRouterStrategyQo1.getSortBy()).thenReturn(null);

        RouterStrategyListResponse res1 = strategyMgmtService.listRouterStrategy(TEST_PROJECT_ID, listRouterStrategyQo1);
        assertEquals(1, res1.getData().size());
        assertEquals(1, res1.getTotal().intValue());
    }

    @Test
    @Sql(scripts = {"classpath:sql/router_strategy_db.sql", "classpath:sql/model_service_db.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void test_detail(){
        RouterStrategyBaseInfo baseInfo;
        try {
            baseInfo = strategyMgmtService.strategyDetail(TEST_PROJECT_ID, TEST_WORKSPACE_ID, MOCK_STRATEGY_ID);
            Assertions.assertNotNull(baseInfo);
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_ROUTER_STRATEGY_NOT_EXIST, e.getErrorCode());
        }
    }

    @Test
    @Sql(scripts = {"classpath:sql/router_strategy_db.sql", "classpath:sql/model_service_db.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void test_update(){

        try {
            strategyMgmtService.updateRouterStrategy(TEST_PROJECT_ID, TEST_WORKSPACE_ID, MOCK_STRATEGY_ID,
                    new RouterStrategyRequest());
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_ROUTER_STRATEGY_NOT_EXIST, e.getErrorCode());
        }

        RouterStrategyRequest res = createRouterStrategyRequest();
        res.setStrategyName("TestModelRouter");
        try {
            strategyMgmtService.updateRouterStrategy(TEST_PROJECT_ID,
                    "default", "test_model_router", res);
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.ROUTER_STRATEGY_NAME_REPEATED, e.getErrorCode());
        }

        strategyMgmtService.updateRouterStrategy(TEST_PROJECT_ID, "default", "test_model_router",res);
    }


}
