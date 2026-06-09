/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import static com.openjiuwen.studio.agent.agentbase.enums.ResourceType.KNOWLEDGE_BASE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ResourceUsageFactoryTest {

    @InjectMocks
    private ResourceUsageFactory resourceUsageFactory;

    @Test
    void test_queryResourceUsageCountInKnowledgeBase_throw_exception() {
        ReflectionTestUtils.setField(resourceUsageFactory, "resourceUsageQueryServiceMap", new HashMap<>());
        assertThrows(AgentBaseException.class,
            () -> resourceUsageFactory.chooseResourceUsageQueryServiceMap(KNOWLEDGE_BASE));
    }

}
