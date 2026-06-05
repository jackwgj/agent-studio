/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.service;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.KnowledgeRepoService;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.LakeSearchService;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * KnowledgeRepoContext测试类
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class KnowledgeRepoContextTest extends BaseTest {

    @Autowired
    private KnowledgeRepoContext knowledgeRepoContext;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        // Create mock map with LakeSearch service
        Map<String, KnowledgeRepoService> mockMap = new HashMap<>();
        mockMap.put("LakeSearch", mock(LakeSearchService.class));
        ReflectionTestUtils.setField(knowledgeRepoContext, "knowledgeRepoServiceMap", mockMap);
        ReflectionTestUtils.setField(knowledgeRepoContext, "knowledgeSource", "KooSearch");
        ReflectionTestUtils.setField(knowledgeRepoContext, "authMode", "basic");
    }

    @Test
    void test_getService_should_return_LakeSearchService_when_type_is_LakeSearch() {
        ReflectionTestUtils.setField(knowledgeRepoContext, "knowledgeSource", "LakeSearch");
        KnowledgeRepoService result = knowledgeRepoContext.getService(null);
        assertInstanceOf(LakeSearchService.class, result);

        result = knowledgeRepoContext.getService("LakeSearch");
        assertInstanceOf(LakeSearchService.class, result);
    }

    @Test
    void test_getService_should_return_LakeSearchService_when_type_is_CUSTOM() {
        ReflectionTestUtils.setField(knowledgeRepoContext, "knowledgeSource", "CUSTOM");
        KnowledgeRepoService result = knowledgeRepoContext.getService(null);
        assertInstanceOf(LakeSearchService.class, result);

        result = knowledgeRepoContext.getService("CUSTOM");
        assertInstanceOf(LakeSearchService.class, result);
    }

    @Test
    void test_getService_should_throw_AgentManagerException_when_type_is_Other() {
        assertThrows(AgentStudioException.class, () -> {
            knowledgeRepoContext.getService("Other");
        });
    }
}
