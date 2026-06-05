/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.constant.Constant;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeSegmentRuleEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeSegmentRuleMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeSegRuleInfo;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeConnectionRouterService;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeRepoContext;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.LakeSearchService;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeSegmentRuleRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeSegmentRuleResponseBody;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeSegmentRule;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeSegmentRulesResponseBody;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class KnowledgeBaseRuleServiceImplTest {
    @Mock
    private KnowledgeSegmentRuleMapper segmentRuleMapper;

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KnowledgeRepoContext knowledgeRepoContext;

    private AutoCloseable mockitoCloseable;

    @Mock
    private LakeSearchService lakeSearchService;

    @Mock
    private KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    @InjectMocks
    private KnowledgeBaseRuleServiceImpl knowledgeBaseRuleServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(knowledgeBaseRuleServiceImpl, "segmentRuleMapper", segmentRuleMapper);
        ReflectionTestUtils.setField(knowledgeBaseRuleServiceImpl, "knowledgeBaseMapper", knowledgeBaseMapper);
        ReflectionTestUtils.setField(knowledgeBaseRuleServiceImpl, "knowledgeConnectionRouterService",
            knowledgeConnectionRouterService);
        ReflectionTestUtils.setField(knowledgeBaseRuleServiceImpl, "knowledgeRepoContext", knowledgeRepoContext);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_deleteSegmentRule_should_return_not_null() throws Exception {
        when(knowledgeRepoContext.getService(any())).thenReturn(lakeSearchService);
        when(segmentRuleMapper.selectByPrimaryKey(any(), any())).thenReturn(new KnowledgeSegmentRuleEntity());
        when(segmentRuleMapper.deleteByPrimaryKey(any(), any())).thenReturn(1);
        // When
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                .thenReturn(Constant.TEST_USER_ID);
            ReflectionTestUtils.invokeMethod(knowledgeBaseRuleServiceImpl, "deleteKnowledgeSegmentRule", null, null,
                null);
        }
    }

    @Test
    void listSegmentRuleTest() {
        KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
        knowledgeBaseEntity.setKnowledgeBaseConnectionId("connection-id");
        when(knowledgeBaseMapper.selectByIdAndWorkspaceId(any(), any())).thenReturn(knowledgeBaseEntity);
        when(segmentRuleMapper.selectByDomainId(any(), any(), eq("connection-id"))).thenReturn(new ArrayList<>());
        ListKnowledgeSegmentRulesResponseBody segmentRuleListResp
            = knowledgeBaseRuleServiceImpl.listKnowledgeSegmentRules("projectId", "workspaceId", "repoId");
        Assertions.assertEquals(segmentRuleListResp.getRuleList().size(), 1);
    }

    @Test
    void listSegmentRuleTestWithoutRepoId() {
        KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
        knowledgeBaseEntity.setKnowledgeBaseConnectionId("connection-id2");
        when(segmentRuleMapper.selectByDomainId(any(), any(), eq("connection-id2"))).thenReturn(new ArrayList<>());
        when(knowledgeConnectionRouterService.findConnectionIdByContext()).thenReturn("connection-id2");
        ListKnowledgeSegmentRulesResponseBody segmentRuleListResp
            = knowledgeBaseRuleServiceImpl.listKnowledgeSegmentRules("projectId", "workspaceId", null);
        Assertions.assertEquals(segmentRuleListResp.getRuleList().size(), 1);
    }

    @Test
    void test_createSegmentRule_should_return_valid_response() throws Exception {
        // 准备输入参数
        String projectId = "test-project-id";
        String workspaceId = "test-workspace-id";

        CreateKnowledgeSegmentRuleRequestBody body = new CreateKnowledgeSegmentRuleRequestBody();
        body.setRuleRegexs(List.of("regex1", "regex2"));

        KnowledgeSegRuleInfo knowledgeSegRuleInfo = new KnowledgeSegRuleInfo();
        knowledgeSegRuleInfo.setRuleId("test-rule-id");
        knowledgeSegRuleInfo.setKnowledgeBaseConnectionId("test-connection-id");

        // 模拟 service 调用返回
        when(knowledgeRepoContext.getService(any())).thenReturn(lakeSearchService);
        when(lakeSearchService.createSegmentRule(any(KnowledgeSegmentRule.class), any())).thenReturn(
            knowledgeSegRuleInfo);

        // 模拟 mapper 插入操作 - 使用thenReturn而不是doNothing
        when(segmentRuleMapper.insert(any(KnowledgeSegmentRuleEntity.class))).thenReturn(1);

        // 模拟 RequestContextUtils 返回值
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId)
                .thenReturn(Constant.TEST_USER_ID);
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName).thenReturn("test-user");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId)
                .thenReturn("test-domain-id");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainName)
                .thenReturn("test-domain-name");

            // 执行方法
            CreateKnowledgeSegmentRuleResponseBody response = knowledgeBaseRuleServiceImpl.createKnowledgeSegmentRule(
                projectId, workspaceId, body);

            // 验证结果
            Assertions.assertNotNull(response);
            Assertions.assertEquals("test-rule-id", response.getId());

            // 验证是否调用了正确的 mapper 方法
            ArgumentCaptor<KnowledgeSegmentRuleEntity> captor = ArgumentCaptor.forClass(
                KnowledgeSegmentRuleEntity.class);
            verify(segmentRuleMapper).insert(captor.capture());

            KnowledgeSegmentRuleEntity capturedEntity = captor.getValue();
            Assertions.assertEquals(projectId, capturedEntity.getProjectId());
            Assertions.assertEquals(workspaceId, capturedEntity.getWorkspaceId());// 使用零空格连接
            Assertions.assertEquals("test-user", capturedEntity.getCreator());
            Assertions.assertEquals(Constant.TEST_USER_ID, capturedEntity.getCreatorId());
        }
    }
}
