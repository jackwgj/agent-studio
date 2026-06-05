/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import com.openjiuwen.studio.agent.agentbase.constant.Constant;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeRepoMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
public class KnowledgeBaseDatasetStorageResourceUsageQueryServiceTest {

    @Mock
    private KnowledgeRepoMapper knowledgeRepoMapper;

    @InjectMocks
    private com.openjiuwen.studio.agent.agentbase.service.resource.KnowledgeBaseDatasetStorageResourceUsageQueryService
        knowledgeBaseDatasetStorageResourceUsageQueryService;

    @Test
    void test_queryResourceUsageCountInKnowledgeBase_throw_exception() {
        assertThrows(AgentBaseException.class,
            () -> knowledgeBaseDatasetStorageResourceUsageQueryService.queryResourceUsageCountInKnowledgeBase(
                Constant.TEST_PROJECT_ID, Constant.TEST_DOMAIN_ID));
    }

    @Test
    void test_queryResourceUsageCount() {
        Mockito.when(knowledgeRepoMapper.sumKnowledgeRepoSizeWithTenantType(anyString(), anyString())).thenReturn(10L);
        Long l = knowledgeBaseDatasetStorageResourceUsageQueryService.queryResourceUsageCount("domainId");
    }
}
