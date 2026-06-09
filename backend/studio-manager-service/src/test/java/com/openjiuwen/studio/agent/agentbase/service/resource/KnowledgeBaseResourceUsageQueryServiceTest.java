/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import static com.openjiuwen.studio.agent.agentbase.enums.ResourceType.KNOWLEDGE_BASE;
import static org.mockito.ArgumentMatchers.anyString;

import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeBaseResourceUsageQueryServiceTest {

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    private KnowledgeBaseResourceUsageQueryService
        knowledgeBaseResourceUsageQueryService;

    @BeforeEach
    void init() {
        knowledgeBaseResourceUsageQueryService = new KnowledgeBaseResourceUsageQueryService(knowledgeBaseMapper);
    }

    @Test
    void resourceTypeTest() {
        ResourceType resourceType = knowledgeBaseResourceUsageQueryService.resourceType();
        Assertions.assertEquals(resourceType, KNOWLEDGE_BASE);
    }

    @Test
    void queryResourceUsageCountTest() {
        Mockito.when(knowledgeBaseMapper.countByType(anyString(), anyString())).thenReturn(List.of(""));
        long res = knowledgeBaseResourceUsageQueryService.queryResourceUsageCount("domainId");
        Assertions.assertEquals(res, 1L);
    }

    @Test
    void queryResourceUsageCountInKnowledgeBaseTest() {
        Assertions.assertThrowsExactly(AgentBaseException.class,
            () -> knowledgeBaseResourceUsageQueryService.queryResourceUsageCountInKnowledgeBase("domainId", "repoId"));
    }

}