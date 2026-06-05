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
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * CustomObjectManagementServiceTest 自定义对象管理服务测试类
 *
 */
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomObjectManagementServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    @Autowired
    private CustomObjectManagementService customObjectManagementService;

    private AutoCloseable mockitoCloseable;

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
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
    }

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(customObjectManagementService, "envType", "SIMPLE");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testCustomObject() {
        // 创建自定义对象
        ObjectInfoReq body = new ObjectInfoReq().setName("name").setSchemas("schema");
        ObjectBriefRsp result = customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID,
            Constants.TEST_WORKSPACE_ID, body);
        String objectId = result.getId();
        Assertions.assertNotNull(objectId);

        // 创建自定义对象，指定id
        ObjectInfoReq body1 = new ObjectInfoReq().setName("name1").setSchemas("shema1").setId("id1");
        ObjectBriefRsp result1 = customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID,
            Constants.TEST_WORKSPACE_ID, body1);
        String objectId1 = result1.getId();
        Assertions.assertEquals("id1", objectId1);

        // 名称重复
        Assertions.assertThrows(AgentStudioException.class, () -> {
            customObjectManagementService.createCustomObject(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID,
                body);
        });

        // 修改对象名字
        String modifyName = body.getName() + "_modify";
        body.setName(modifyName);
        ObjectBriefRsp result2 = customObjectManagementService.modifyCustomObject(Constants.TEST_PROJECT_ID, objectId,
            Constants.TEST_WORKSPACE_ID, body);

        Assertions.assertNotNull(result2);

        // 修改对象内容
        String modifyContent = body.getSchemas() + "_modify";
        body.setSchemas(modifyContent);
        ObjectBriefRsp result3 = customObjectManagementService.modifyCustomObject(Constants.TEST_PROJECT_ID, objectId,
            Constants.TEST_WORKSPACE_ID, body);
        Assertions.assertNotNull(result3);

        // 查询不存在对象
        Assertions.assertThrows(AgentStudioException.class, () -> {
            customObjectManagementService.retrieveCustomObject(Constants.TEST_PROJECT_ID, "fake_id",
                Constants.TEST_WORKSPACE_ID);
            });

        // 查询自定义列表
        ListCustomObjectQo listCustomObjectQo = new ListCustomObjectQo().setLimit(10)
            .setOffset(0)
            .setWorkspaceId(Constants.TEST_WORKSPACE_ID);
        Assertions.assertEquals(2,
            customObjectManagementService.listCustomObject(Constants.TEST_PROJECT_ID, listCustomObjectQo).getCount());

        // 带name模糊匹配查询
        ListCustomObjectQo listCustomObjectQo1 = new ListCustomObjectQo().setLimit(10)
            .setOffset(0).setName(body.getName())
            .setWorkspaceId(Constants.TEST_WORKSPACE_ID);
        Assertions.assertEquals(1,
            customObjectManagementService.listCustomObject(Constants.TEST_PROJECT_ID, listCustomObjectQo1).getCount());

        // 删除对象
        customObjectManagementService.deleteCustomObject(Constants.TEST_PROJECT_ID, objectId,
            Constants.TEST_WORKSPACE_ID);
        Assertions.assertEquals(1, customObjectManagementService.listCustomObject(Constants.TEST_PROJECT_ID,
            new ListCustomObjectQo().setLimit(10).setOffset(0).setWorkspaceId(Constants.TEST_WORKSPACE_ID)).getCount());
    }

}
