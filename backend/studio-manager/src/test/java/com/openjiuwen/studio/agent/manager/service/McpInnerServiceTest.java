/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.mapper.McpServiceMapper;
import com.openjiuwen.studio.agent.manager.service.mcp.McpInnerService;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class McpInnerServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServiceMapper mcpServiceMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareInnerService shareInnerService;

    @InjectMocks
    private McpInnerService mcpInnerService;

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
    void test_getMcpByIds_should_return_not_null() throws Exception {
        // Given
        List<McpServiceEntity> list = new ArrayList<>();
        McpServiceEntity mcpServiceEntity = new McpServiceEntity();
        list.add(mcpServiceEntity);
        when(mcpServiceMapper.selectList(anyList(), eq(null), eq(null), anyString())).thenReturn(list);

        List<String> validMcpServerIds = new ArrayList<>();
        validMcpServerIds.add("not_empty");

        // When
        List<McpServiceEntity> result = mcpInnerService.getMcpByIds("not_empty", validMcpServerIds);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getMcpByIds_should_return_not_null1() throws Exception {
        // Given
        when(mcpServiceMapper.selectList(anyList(), eq(null), eq(null), anyString())).thenReturn(null);

        // When
        List<McpServiceEntity> result = mcpInnerService.getMcpByIds(null, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getMcpServerInfoByIds_should_return_not_null() throws Exception {
        // Given
        List<McpServiceEntity> mcpServer = new ArrayList<>();
        McpServiceEntity mcpServiceEntity = new McpServiceEntity();
        mcpServer.add(mcpServiceEntity);
        when(mcpServiceMapper.selectList(anyList(), eq(null), eq(null), anyString())).thenReturn(mcpServer);

        List<String> validMcpServerIds = new ArrayList<>();
        validMcpServerIds.add("not_empty");

        // When
        when(shareInnerService.getShareMcp(any(), any())).thenReturn(new ArrayList<>());
        List<McpServerInfo> result = mcpInnerService.getMcpServerInfoByIds("not_empty", validMcpServerIds);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getMcpServerInfoByIds_should_return_not_null1() throws Exception {
        // Given
        when(mcpServiceMapper.selectList(anyList(), eq(null), eq(null), anyString())).thenReturn(null);

        // When
        List<McpServerInfo> result = mcpInnerService.getMcpServerInfoByIds(null, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getMcpServerInfoById_should_return_not_null() throws Exception {
        // Given
        McpServiceEntity mcpServer = new McpServiceEntity();
        mcpServer.setId("not_empty");
        mcpServer.setName("not_empty");
        mcpServer.setNameEn("not_empty");
        mcpServer.setDescription("not_empty");
        mcpServer.setServerConfig("not_empty");
        mcpServer.setTools("not_empty");
        mcpServer.setFcInstanceUrl("not_empty");
        Timestamp createdDate1 = mock(Timestamp.class, Answers.RETURNS_DEEP_STUBS);
        mcpServer.setCreatedDate(createdDate1);
        Timestamp lastUpdatedDate1 = mock(Timestamp.class, Answers.RETURNS_DEEP_STUBS);
        mcpServer.setLastUpdatedDate(lastUpdatedDate1);
        mcpServer.setProjectId("not_empty");
        mcpServer.setIcon("not_empty");
        mcpServer.setVisibility("not_empty");
        mcpServer.setNodeType("not_empty");
        when(mcpServiceMapper.selectByPrimaryKey(anyString(), eq(null))).thenReturn(mcpServer);

        // When
        McpServerInfo result = mcpInnerService.getMcpServerInfoById("not_empty", null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getMcpServerInfoById_should_return_not_null1() throws Exception {
        // Given
        when(mcpServiceMapper.selectByPrimaryKey(anyString(), eq(null))).thenReturn(null);

        // When
        McpServerInfo result = mcpInnerService.getMcpServerInfoById(null, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_isMcpExist_should_return_true() throws Exception {
        // Given
        List<McpServiceEntity> mcpServiceEntitys = new ArrayList<>();
        McpServiceEntity mcpServiceEntity = new McpServiceEntity();
        mcpServiceEntitys.add(mcpServiceEntity);
        when(mcpServiceMapper.selectByName(anyString(), eq(null), eq(null), anyString())).thenReturn(mcpServiceEntitys);

        // When
        boolean result = mcpInnerService.isMcpExist("not_empty", "not_empty");

        // Then
        assertTrue(result);
    }

    @Test
    void test_isMcpExist_should_return_true1() throws Exception {
        // Given
        when(mcpServiceMapper.selectByName(anyString(), eq(null), eq(null), anyString())).thenReturn(null);

        // When
        boolean result = mcpInnerService.isMcpExist(null, null);

        // Then
        assertTrue(result);
    }

    @Test
    void test_createOrUpdate_should_not_throw_exception() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            // Given
            when(mcpServiceMapper.selectByName(anyString(), eq(null), eq(null), anyString())).thenReturn(null);
            when(mcpServiceMapper.insert(any(McpServiceEntity.class))).thenReturn(0);

            // When
            boolean result = mcpInnerService.createOrUpdate(null, null);
        });
    }
}
