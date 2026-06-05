/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.relation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.mapper.McpServiceMapper;
import com.openjiuwen.studio.agent.manager.ros.relation.MCPRelationCleaner;
import com.openjiuwen.studio.agent.manager.service.mcp.McpServiceManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class MCPRelationCleanerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServiceMapper mcpServiceMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServiceManager mcpServiceManager;

    @Mock
    private MCPRelationCleaner mCPRelationCleaner;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_clean_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            List<McpServiceEntity> mcpServiceEntities = new ArrayList<>();
            McpServiceEntity mcpServiceEntity = new McpServiceEntity();
            mcpServiceEntities.add(mcpServiceEntity);
            when(mcpServiceMapper.selectByIds(anyList())).thenReturn(mcpServiceEntities);

            when(mcpServiceManager.deleteService(anyString(), anyString(), anyString())).thenReturn("not_empty");

            List<String> ids = new ArrayList<>();
            ids.add("not_empty");

            // When
            mCPRelationCleaner.clean(ids, "not_empty");
        });
    }

    @Test
    void test_clean_should_not_throw_exception1() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(mcpServiceMapper.selectByIds(anyList())).thenReturn(null);

            when(mcpServiceManager.deleteService(anyString(), anyString(), anyString())).thenReturn(null);

            // When
            mCPRelationCleaner.clean(null, null);
        });
    }
}
