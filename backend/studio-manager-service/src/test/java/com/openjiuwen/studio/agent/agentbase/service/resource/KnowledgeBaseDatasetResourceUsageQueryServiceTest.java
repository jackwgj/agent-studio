/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.agentbase.constant.Constant;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
public class KnowledgeBaseDatasetResourceUsageQueryServiceTest {

    @InjectMocks
    private KnowledgeBaseDatasetResourceUsageQueryService
        knowledgeBaseDatasetResourceUsageQueryService;

    @Test
    void test_queryResourceUsageCount_throw_exception() {
        assertThrows(AgentBaseException.class,
            () -> knowledgeBaseDatasetResourceUsageQueryService.queryResourceUsageCount(Constant.TEST_DOMAIN_ID));
    }
}
