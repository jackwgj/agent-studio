/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.knowledge.FaqDeleteBatchResp;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBranchInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentExportParams;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ListComplexIntentBranchQo;
import com.openjiuwen.studio.agent.manager.dto.ListComplexIntentQo;
import com.openjiuwen.studio.agent.manager.entity.ComplexIntentBranchEntity;
import com.openjiuwen.studio.agent.manager.entity.ComplexIntentEntity;
import com.openjiuwen.studio.agent.manager.mapper.ComplexIntentBranchMapper;
import com.openjiuwen.studio.agent.manager.mapper.ComplexIntentMapper;
import com.openjiuwen.studio.agent.manager.rce.models.knowledge.FaqResp;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ComplexIntentService 测试类
 *
 */
@Slf4j
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
public class ComplexIntentServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    private static MockedStatic<CryptoUtils> mockedCryptoUtils;

    @InjectMocks
    ComplexIntentManagementService complexIntentManagementServiceMock;

    @Autowired
    ComplexIntentManagementService complexIntentManagementService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComplexIntentBranchMapper complexIntentBranchMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComplexIntentMapper complexIntentMapper;

    private AutoCloseable mockitoCloseable;

    private static final String TEST_NAME = "test-branch";

    private static final String TEST_PROJECT_ID = "project-123";

    private static final String TEST_WORKSPACE_ID = "workspace-456";

    private static final String TEST_INTENT_ID = "intent-789";

    private static final String TEST_BRANCH_ID = "branch-001";

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

        mockedCryptoUtils = Mockito.mockStatic(CryptoUtils.class);
        mockedCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("fake_knowledge_authorization");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
        mockedCryptoUtils.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        ReflectionTestUtils.setField(complexIntentManagementService, "complexIntentRepoId", "test-repo");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testComplexIntent() {
        ComplexIntentInfoReq req = new ComplexIntentInfoReq().setName("name").setDescription("description");

        // 创建
        String intentId =
            complexIntentManagementService.createComplexIntent(Constants.TEST_PROJECT_ID,
                Constants.TEST_WORKSPACE_ID, req).getIntentId();
        Assertions.assertNotNull(intentId);

        ComplexIntentInfoReq req1 =
            new ComplexIntentInfoReq().setName("name1").setDescription("description1").setId("fake_id");

        String intentId1 =
            complexIntentManagementService.createComplexIntent(Constants.TEST_PROJECT_ID,
                Constants.TEST_WORKSPACE_ID, req1).getIntentId();
        Assertions.assertEquals("fake_id", intentId1);

        // 名称重复
        Assertions.assertThrows(AgentStudioException.class, () -> {
            complexIntentManagementService.createComplexIntent(Constants.TEST_PROJECT_ID,
                Constants.TEST_WORKSPACE_ID, req);
        });

        // 修改
        String nameAfter = req.getName() + "_modify";
        req.setName(nameAfter);
        complexIntentManagementService.modifyComplexIntent(Constants.TEST_PROJECT_ID, intentId1,
            Constants.TEST_WORKSPACE_ID, req);
        Assertions.assertEquals(
            complexIntentManagementService.retrieveComplexIntent(Constants.TEST_PROJECT_ID, intentId1,
                Constants.TEST_WORKSPACE_ID).getName(),
            nameAfter);

        ListComplexIntentQo listComplexIntentQo = new ListComplexIntentQo().setLimit(12).setOffset(0)
            .setWorkspaceId("default");
        // 列表
        Assertions.assertEquals(complexIntentManagementService
            .listComplexIntent(Constants.TEST_PROJECT_ID, listComplexIntentQo)
            .getCount(), 2);

        // 删除
        complexIntentManagementService.deleteComplexIntent(Constants.TEST_PROJECT_ID, intentId, Constants.TEST_WORKSPACE_ID);
        Assertions.assertEquals(
            complexIntentManagementService.listComplexIntent(Constants.TEST_PROJECT_ID, listComplexIntentQo.setLimit(0)
                    .setWorkspaceId("default"))
                .getCount(),
            1);
    }

    @Sql(scripts = {"classpath:sql/complex_intent_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Test
    void testComplexBranchIntent() {
        // mock api
        String testFaqId = "id1";
        FaqResp faqIdRsp = new FaqResp();
        faqIdRsp.setFaqId(testFaqId);
        FaqDeleteBatchResp deleteResp = new FaqDeleteBatchResp();
        deleteResp.setDeletedCount(1);
        deleteResp.setTotalCount(1);

        String intentId = "test_intent_id";
        ComplexIntentBranchInfoReq req = new ComplexIntentBranchInfoReq().setName("name")
            .setDescription("description")
            .setExamples(Arrays.asList("1", "1"))
            .setBranchId("test_branch_id");
        // 意图样例重复
        Assertions.assertThrows(AgentStudioException.class, () -> {
            complexIntentManagementService.createComplexIntentBranch(Constants.TEST_PROJECT_ID, intentId,
                Constants.TEST_WORKSPACE_ID, req);
        });
        // 创建
        req.setExamples(Arrays.asList("1", "2"));
        String branchId =
            complexIntentManagementService.createComplexIntentBranch(Constants.TEST_PROJECT_ID, intentId,
                    Constants.TEST_WORKSPACE_ID, req)
                .getBranchId();
        Assertions.assertNotNull(branchId);
        // 名称重复
        Assertions.assertThrows(AgentStudioException.class, () -> {
            complexIntentManagementService.createComplexIntentBranch(Constants.TEST_PROJECT_ID, intentId,
                Constants.TEST_WORKSPACE_ID, req);
        });

        // 修改
        String nameAfter = req.getName() + "_modify";
        req.setName(nameAfter);
        complexIntentManagementService.modifyComplexIntentBranch(Constants.TEST_PROJECT_ID, intentId, branchId,
            Constants.TEST_WORKSPACE_ID, req);
        // 查询
        Assertions.assertEquals(complexIntentManagementService
            .listComplexIntentBranch(Constants.TEST_PROJECT_ID, intentId,
                new ListComplexIntentBranchQo().setWorkspaceId("default"))
            .getCount(), 1);

        // 删除
        complexIntentManagementService.deleteComplexIntentBranch(Constants.TEST_PROJECT_ID, intentId, branchId,
            Constants.TEST_WORKSPACE_ID);
        Assertions.assertEquals(complexIntentManagementService
            .listComplexIntentBranch(Constants.TEST_PROJECT_ID, intentId,
                new ListComplexIntentBranchQo().setWorkspaceId("default"))
            .getCount(), 0);
    }

    @Test
    void test_branch_name() {
        ComplexIntentBranchEntity searchEntity = new ComplexIntentBranchEntity();
        searchEntity.setProjectId(TEST_PROJECT_ID);
        searchEntity.setWorkspaceId(TEST_WORKSPACE_ID);
        searchEntity.setBranchName(TEST_NAME);
        searchEntity.setIntentId(TEST_INTENT_ID);

        when(complexIntentBranchMapper.getEntitiesAccurate(searchEntity)).thenReturn(Collections.emptyList());

        // 无同名分支，正常返回
        assertDoesNotThrow(() -> complexIntentManagementService.checkIntentBranchNameRepeat(TEST_NAME, TEST_PROJECT_ID,
            TEST_WORKSPACE_ID, TEST_INTENT_ID, TEST_BRANCH_ID));
    }

    @Test
    void test_branch_name_multiple() {
        List<ComplexIntentBranchEntity> entities = new ArrayList<>();
        ComplexIntentBranchEntity entity1 = new ComplexIntentBranchEntity();
        entity1.setBranchName("testBranch");

        ComplexIntentBranchEntity entity2 = new ComplexIntentBranchEntity();
        entity2.setBranchName("testBranch");

        entities.add(entity1);
        entities.add(entity2);

        when(complexIntentBranchMapper.getEntitiesAccurate(any(ComplexIntentBranchEntity.class))).thenReturn(entities);

        assertThrows(AgentStudioException.class,
            () -> complexIntentManagementServiceMock.checkIntentBranchNameRepeat(TEST_NAME, TEST_PROJECT_ID,
                TEST_WORKSPACE_ID, TEST_INTENT_ID, TEST_BRANCH_ID));
    }

    @Test
    void test_branch_name_repeat() {
        List<ComplexIntentBranchEntity> entities = new ArrayList<>();
        ComplexIntentBranchEntity entity = new ComplexIntentBranchEntity();
        entity.setBranchName("testBranch");
        entity.setBranchId("TEST_BRANCH_ID");

        entities.add(entity);

        when(complexIntentBranchMapper.getEntitiesAccurate(any(ComplexIntentBranchEntity.class))).thenReturn(entities);

        assertThrows(AgentStudioException.class,
            () -> complexIntentManagementServiceMock.checkIntentBranchNameRepeat(TEST_NAME, TEST_PROJECT_ID,
                TEST_WORKSPACE_ID, TEST_INTENT_ID, TEST_BRANCH_ID));
    }

    @Test
    void test_intent_name_multiple() {
        List<ComplexIntentEntity> entities = new ArrayList<>();
        ComplexIntentEntity entity1 = new ComplexIntentEntity();
        entity1.setName("TEST_NAME");

        ComplexIntentEntity entity2 = new ComplexIntentEntity();
        entity1.setName("TEST_NAME");

        entities.add(entity1);
        entities.add(entity2);

        when(complexIntentMapper.getEntitiesAccurate(any(ComplexIntentEntity.class))).thenReturn(entities);

        assertThrows(AgentStudioException.class,
            () -> complexIntentManagementServiceMock.checkIntentNameRepeat(TEST_NAME, TEST_PROJECT_ID,
                TEST_WORKSPACE_ID, TEST_INTENT_ID));
    }

    @Test
    void test_create_complex_intent_branches_not_null() {
        ComplexIntentInfoReq req = new ComplexIntentInfoReq().setName("name").setDescription("description");
        ComplexIntentBranchInfoReq branchReq = new ComplexIntentBranchInfoReq().setName("testBranchName")
            .setDescription("branchDescription");
        List<ComplexIntentBranchInfoReq> branchReqs = new ArrayList<>();
        branchReqs.add(branchReq);
        req.setBranches(branchReqs);
        when(complexIntentMapper.getEntitiesAccurate(any(ComplexIntentEntity.class))).thenReturn(new ArrayList<>());
        assertDoesNotThrow(
            () -> complexIntentManagementServiceMock.createComplexIntent(TEST_PROJECT_ID, TEST_WORKSPACE_ID, req));

    }

    @Test
    void test_modify_complex_intent_branches_not_null() {
        ComplexIntentInfoReq req = new ComplexIntentInfoReq().setName("newIntentName").setDescription("description");
        ComplexIntentBranchInfoReq branchReq = new ComplexIntentBranchInfoReq().setName("testBranch")
            .setDescription("branchDescription")
            .setBranchId("TEST_BRANCH_ID");
        List<ComplexIntentBranchInfoReq> branchReqs = new ArrayList<>();
        branchReqs.add(branchReq);
        req.setBranches(branchReqs);
        when(complexIntentMapper.getByKeyAndWorkspaceId(anyString(), anyString(), anyString())).thenReturn(
            new ComplexIntentEntity());
        ComplexIntentEntity complexIntentEntity = new ComplexIntentEntity();
        complexIntentEntity.setIntentId(TEST_INTENT_ID);
        List<ComplexIntentEntity> entities = new ArrayList<>();
        entities.add(complexIntentEntity);
        when(complexIntentMapper.getEntitiesAccurate(any(ComplexIntentEntity.class))).thenReturn(entities);
        when(complexIntentMapper.updateByKey(any(ComplexIntentEntity.class))).thenReturn(1);
        List<ComplexIntentBranchEntity> branchEntities = new ArrayList<>();
        ComplexIntentBranchEntity entity = new ComplexIntentBranchEntity();
        entity.setBranchName("testBranch");
        entity.setBranchId("TEST_BRANCH_ID");
        branchEntities.add(entity);

        when(complexIntentBranchMapper.getEntitiesAccurate(any(ComplexIntentBranchEntity.class))).thenReturn(
            branchEntities);
        when(complexIntentBranchMapper.updateByKey(any(ComplexIntentBranchEntity.class))).thenReturn(1);
        assertDoesNotThrow(() -> complexIntentManagementServiceMock.modifyComplexIntent(TEST_PROJECT_ID, TEST_INTENT_ID,
            TEST_WORKSPACE_ID, req));
    }

    @Test
    void test_modify_complex_intent_branch_id_null() {
        ComplexIntentInfoReq req = new ComplexIntentInfoReq().setName("newIntentName").setDescription("description");
        ComplexIntentBranchInfoReq branchReq = new ComplexIntentBranchInfoReq().setName("testBranch")
            .setDescription("branchDescription");
        List<ComplexIntentBranchInfoReq> branchReqs = new ArrayList<>();
        branchReqs.add(branchReq);
        req.setBranches(branchReqs);
        when(complexIntentMapper.getByKeyAndWorkspaceId(anyString(), anyString(), anyString())).thenReturn(
            new ComplexIntentEntity());
        ComplexIntentEntity complexIntentEntity = new ComplexIntentEntity();
        complexIntentEntity.setIntentId(TEST_INTENT_ID);
        List<ComplexIntentEntity> entities = new ArrayList<>();
        entities.add(complexIntentEntity);
        when(complexIntentMapper.getEntitiesAccurate(any(ComplexIntentEntity.class))).thenReturn(entities);
        when(complexIntentMapper.updateByKey(any(ComplexIntentEntity.class))).thenReturn(1);

        when(complexIntentBranchMapper.getEntities(any(ComplexIntentBranchEntity.class))).thenReturn(
            new ArrayList<>());

        assertDoesNotThrow(() -> complexIntentManagementServiceMock.modifyComplexIntent(TEST_PROJECT_ID, TEST_INTENT_ID,
            TEST_WORKSPACE_ID, req));
    }

    @Test
    void testIntentExport() {
        // 导出测试
        Resource resource = complexIntentManagementService.exportIntentTemplate(Constants.TEST_PROJECT_ID,
            Constants.TEST_WORKSPACE_ID);
        assertNotNull(resource);
    }

    @Test
    @Sql(scripts = {"classpath:sql/complex_intent_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testIntentExportWithBody() {
        // 导出测试
        ArrayList<String> ids = new ArrayList<>();
        ids.add("test_intent_id");
        ComplexIntentExportParams complexIntentExportParams = new ComplexIntentExportParams().setIds(ids);
        complexIntentManagementService.exportIntent(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID,
            complexIntentExportParams);
        Resource resource = complexIntentManagementService.exportIntentTemplate(Constants.TEST_PROJECT_ID,
            Constants.TEST_WORKSPACE_ID);
        assertNotNull(resource);
    }
}

