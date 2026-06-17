/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.ListCustomObjectQo;
import com.openjiuwen.studio.agent.manager.dto.ObjectBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.ObjectInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ObjectRsp;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import com.openjiuwen.studio.prompt.engineering.service.PromptObsService;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * CustomObjectManagementServiceIntegrationTest 自定义对象管理服务集成测试类
 * 参考 EnvironmentServiceManagerServiceTest 的写法
 */
@Transactional
public class CustomObjectManagementServiceIntegrationTest extends BaseTest {

    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    @Autowired
    private CustomObjectManagementService customObjectManagementService;

    @MockitoBean(name = "mgObsService")
    private MgObsService mgObsService;

    @MockitoBean(name = "promptObsService")
    private PromptObsService promptObsService;

    @BeforeAll
    public static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("zh-cn");
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(Constants.TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_USER_ID);
        mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(Constants.TEST_WORKSPACE_ID);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
    }

    @BeforeEach
    void setUp() {
        // 设置 envType 为 SIMPLE，这样 creatorId 会使用 getRequestUserId()
        ReflectionTestUtils.setField(customObjectManagementService, "envType", "SIMPLE");
    }

    @Test
    @Sql(scripts = {"classpath:sql/custom_object_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testCreateCustomObject() {
        ObjectInfoReq body = new ObjectInfoReq().setName("test_object").setSchemas("{\"type\":\"object\"}");
        ObjectBriefRsp result = customObjectManagementService.createCustomObject(
            Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, body);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/custom_object_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testCreateCustomObjectWithSpecifiedId() {
        ObjectInfoReq body = new ObjectInfoReq()
            .setName("test_object_specified_id")
            .setSchemas("{\"type\":\"object\"}")
            .setId("specified_object_id");
        ObjectBriefRsp result = customObjectManagementService.createCustomObject(
            Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, body);
        Assertions.assertEquals("specified_object_id", result.getId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/custom_object_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testCreateCustomObjectDuplicateName() {
        ObjectInfoReq body = new ObjectInfoReq().setName("duplicate_name").setSchemas("{\"type\":\"object\"}");
        // 第一次创建应该成功
        customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, body);
        // 第二次创建同名应该抛出异常
        Assertions.assertThrows(AgentStudioException.class, () -> {
            customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, body);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/custom_object_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testModifyCustomObject() {
        // 先创建
        ObjectInfoReq createBody = new ObjectInfoReq()
            .setName("modify_test")
            .setSchemas("{\"type\":\"object\"}");
        ObjectBriefRsp created = customObjectManagementService.createCustomObject(
            Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, createBody);

        // 修改名称
        ObjectInfoReq modifyBody = new ObjectInfoReq()
            .setName("modify_test_modified")
            .setSchemas("{\"type\":\"object\"}");
        ObjectBriefRsp modified = customObjectManagementService.modifyCustomObject(
            Constants.TEST_PROJECT_ID, created.getId(), Constants.TEST_WORKSPACE_ID, modifyBody);
        Assertions.assertNotNull(modified);

        // 修改内容
        ObjectInfoReq modifyContentBody = new ObjectInfoReq()
            .setName("modify_test_modified")
            .setSchemas("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}");
        ObjectBriefRsp modifiedContent = customObjectManagementService.modifyCustomObject(
            Constants.TEST_PROJECT_ID, created.getId(), Constants.TEST_WORKSPACE_ID, modifyContentBody);
        Assertions.assertNotNull(modifiedContent);
    }

    @Test
    @Sql(scripts = {"classpath:sql/custom_object_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testRetrieveCustomObject() {
        // 创建对象
        ObjectInfoReq body = new ObjectInfoReq()
            .setName("retrieve_test")
            .setSchemas("{\"type\":\"object\"}");
        ObjectBriefRsp created = customObjectManagementService.createCustomObject(
            Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, body);

        // 查询不存在的对象
        Assertions.assertThrows(AgentStudioException.class, () -> {
            customObjectManagementService.retrieveCustomObject(Constants.TEST_PROJECT_ID, "non_existent_id", Constants.TEST_WORKSPACE_ID);
        });

        // 查询存在的对象 (注意: retrieveCustomObject 返回 ObjectRsp 而非 ObjectBriefRsp)
        ObjectRsp retrieved = customObjectManagementService.retrieveCustomObject(
            Constants.TEST_PROJECT_ID, created.getId(), Constants.TEST_WORKSPACE_ID);
        Assertions.assertNotNull(retrieved);
        Assertions.assertEquals("retrieve_test", retrieved.getName());
    }

    @Test
    @Sql(scripts = {"classpath:sql/custom_object_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testListCustomObject() {
        // 创建多个对象
        customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID,
            new ObjectInfoReq().setName("list_test_1").setSchemas("{}"));
        customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID,
            new ObjectInfoReq().setName("list_test_2").setSchemas("{}"));

        // 列表查询
        ListCustomObjectQo listQo = new ListCustomObjectQo()
            .setLimit(10)
            .setOffset(0)
            .setWorkspaceId(Constants.TEST_WORKSPACE_ID);
        var listResult = customObjectManagementService.listCustomObject(Constants.TEST_PROJECT_ID, listQo);
        Assertions.assertNotNull(listResult);
        Assertions.assertTrue(listResult.getCount() >= 2);
    }

    @Test
    @Sql(scripts = {"classpath:sql/custom_object_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testListCustomObjectWithNameFilter() {
        // 创建对象
        customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID,
            new ObjectInfoReq().setName("filter_test_unique").setSchemas("{}"));
        customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID,
            new ObjectInfoReq().setName("filter_test_other").setSchemas("{}"));

        // 带名称过滤的列表查询
        ListCustomObjectQo listQo = new ListCustomObjectQo()
            .setLimit(10)
            .setOffset(0)
            .setName("filter_test_unique")
            .setWorkspaceId(Constants.TEST_WORKSPACE_ID);
        var listResult = customObjectManagementService.listCustomObject(Constants.TEST_PROJECT_ID, listQo);
        Assertions.assertNotNull(listResult);
        Assertions.assertEquals(1, listResult.getCount());
    }

    @Test
    @Sql(scripts = {"classpath:sql/custom_object_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDeleteCustomObject() {
        // 创建对象
        ObjectInfoReq body = new ObjectInfoReq()
            .setName("delete_test")
            .setSchemas("{\"type\":\"object\"}");
        ObjectBriefRsp created = customObjectManagementService.createCustomObject(
            Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, body);

        // 验证对象存在
        ListCustomObjectQo listQo = new ListCustomObjectQo()
            .setLimit(10)
            .setOffset(0)
            .setWorkspaceId(Constants.TEST_WORKSPACE_ID);
        long countBefore = customObjectManagementService.listCustomObject(Constants.TEST_PROJECT_ID, listQo).getCount();

        // 删除对象
        customObjectManagementService.deleteCustomObject(
            Constants.TEST_PROJECT_ID, created.getId(), Constants.TEST_WORKSPACE_ID);

        // 验证对象已删除
        long countAfter = customObjectManagementService.listCustomObject(Constants.TEST_PROJECT_ID, listQo).getCount();
        Assertions.assertEquals(countBefore - 1, countAfter);
    }
}