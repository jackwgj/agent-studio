/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import static com.openjiuwen.studio.agent.agentbase.common.constant.CommonConstant.CustomModel.EMBEDDING;
import static com.openjiuwen.studio.agent.agentbase.common.constant.CommonConstant.CustomModel.RERANK;
import static com.openjiuwen.studio.agent.agentbase.common.constant.KnowledgeLockConstant.KNOWLEDGE_TEMPLATE_MGMT_LOCK;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeRepoInfo;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.CssUniSearchService;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeConnectionRouterService;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.manager.dto.ModelInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class KbRepoTemplateMgmtServiceTest {

    @InjectMocks
    private KbRepoTemplateMgmtService kbRepoTemplateMgmtService;

    @Mock
    private KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    @Mock
    private CssUniSearchService cssUniSearchService;

    @Mock
    private RedisClient redisClient;

    RedisLock mockLock = Mockito.mock(RedisLock.class);

    @BeforeEach
    void setUp() throws JsonProcessingException {
        kbRepoTemplateMgmtService = new KbRepoTemplateMgmtService(redisClient, knowledgeConnectionRouterService,
            cssUniSearchService);
        Mockito.when(redisClient.getLock(KNOWLEDGE_TEMPLATE_MGMT_LOCK)).thenReturn(mockLock);

    }

    @Test
    void templateRepoManagementScheduleTaskGetLockTest() throws Exception {
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "knowledgeSource", "KooSearch");
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "logicKnowledgeRepoEnabled", true);
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "shareRepoCheckInterval", 20);
        Mockito.when(mockLock.tryLock(Duration.ofMinutes(10))).thenReturn(true);
        Mockito.when(knowledgeConnectionRouterService.findFreeConnectionIds())
            .thenReturn(List.of("connectionId1", "connectionId2"));
        List<ModelInfo> embeddingModelInfos = List.of(new ModelInfo().setId("11").setName("embeddingModelName1"),
            new ModelInfo().setId("12").setName("embeddingModelName2"));

        List<ModelInfo> rerankModelInfos = List.of(new ModelInfo().setId("21").setName("rerankModelName1"),
            new ModelInfo().setId("22").setName("rerankModelName2"));

        Mockito.when(cssUniSearchService.listModels(argThat(argument -> {
            if (argument == null) {
                return false;
            }
            return EMBEDDING.equals(argument.getModelType());
        }), eq("connectionId1"))).thenReturn(embeddingModelInfos);
        Mockito.when(cssUniSearchService.listModels(argThat(argument -> {
            if (argument == null) {
                return false;
            }
            return RERANK.equals(argument.getModelType());
        }), eq("connectionId1"))).thenReturn(rerankModelInfos);
        ListKnowledgeRepoResp listKnowledgeRepoResp = new ListKnowledgeRepoResp();
        listKnowledgeRepoResp.setTotal(1L);
        KnowledgeRepoInfo knowledgeRepoInfo = new KnowledgeRepoInfo();
        knowledgeRepoInfo.setShareRepoUsed(85);
        knowledgeRepoInfo.setShareRepoQuota(15);
        listKnowledgeRepoResp.setDataList(List.of(knowledgeRepoInfo));
        Mockito.when(
            cssUniSearchService.listTemplateKnowledgeRepos(any(), eq("connectionId1"), anyString(), anyString(),
                anyString(), anyString())).thenReturn(listKnowledgeRepoResp);
        doNothing().when(cssUniSearchService).createTemplateRepo(eq("connectionId1"), anyString(), anyString());
        kbRepoTemplateMgmtService.templateRepoManagementScheduleTask();
    }

    @Test
    void templateRepoManagementScheduleTaskNotGetLockTest() throws Exception {
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "knowledgeSource", "KooSearch");
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "logicKnowledgeRepoEnabled", true);
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "shareRepoCheckInterval", 20);
        Mockito.when(mockLock.tryLock(Duration.ofMinutes(10))).thenReturn(false);
        kbRepoTemplateMgmtService.templateRepoManagementScheduleTask();
    }

    @Test
    void templateRepoManagementScheduleTaskWrongSourceTest() throws Exception {
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "knowledgeSource", "XXX");
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "logicKnowledgeRepoEnabled", true);
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "shareRepoCheckInterval", 20);
        Mockito.when(mockLock.tryLock(Duration.ofMinutes(10))).thenReturn(true);
        kbRepoTemplateMgmtService.templateRepoManagementScheduleTask();
    }

    @Test
    void templateRepoManagementScheduleTaskSwitchOffTest() throws Exception {
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "knowledgeSource", "KooSearch");
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "logicKnowledgeRepoEnabled", false);
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "shareRepoCheckInterval", 20);
        Mockito.when(mockLock.tryLock(Duration.ofMinutes(10))).thenReturn(true);
        kbRepoTemplateMgmtService.templateRepoManagementScheduleTask();
    }

    @Test
    void templateRepoManagementScheduleTaskThrowException() throws Exception {
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "knowledgeSource", "KooSearch");
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "logicKnowledgeRepoEnabled", true);
        ReflectionTestUtils.setField(kbRepoTemplateMgmtService, "shareRepoCheckInterval", 20);
        Mockito.when(mockLock.tryLock(Duration.ofMinutes(10))).thenReturn(true);

        Mockito.when(knowledgeConnectionRouterService.findFreeConnectionIds()).thenReturn(null);
        assertThrows(AgentBaseException.class, () -> kbRepoTemplateMgmtService.templateRepoManagementScheduleTask());
    }
}