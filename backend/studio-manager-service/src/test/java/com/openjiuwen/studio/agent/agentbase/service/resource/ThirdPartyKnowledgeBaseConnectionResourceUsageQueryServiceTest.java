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
public class ThirdPartyKnowledgeBaseConnectionResourceUsageQueryServiceTest {

    @InjectMocks
    private ThirdPartyKnowledgeBaseConnectionResourceUsageQueryService
        service;

    @Test
    void test_queryResourceUsageCountInKnowledgeBase_throw_exception() {
        assertThrows(AgentBaseException.class,
            () -> service.queryResourceUsageCountInKnowledgeBase(Constant.TEST_PROJECT_ID, Constant.TEST_DOMAIN_ID));
    }
}
