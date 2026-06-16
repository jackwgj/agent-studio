/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.mcp;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.mapper.McpServiceMapper;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpInnerServiceTest {

    @Mock
    private EncryptionAdapter encryptionAdapter;

    @Mock
    private McpServiceMapper mcpServiceMapper;

    @Mock
    private ShareInnerService shareInnerService;

    @InjectMocks
    private McpInnerService mcpInnerService;

    @Test
    void testGetMcpByIds_EmptyList() {
        List<McpServiceEntity> result = mcpInnerService.getMcpByIds("ws1", new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMcpByIds_NullList() {
        List<McpServiceEntity> result = mcpInnerService.getMcpByIds("ws1", null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMcpByIds_WithData() {
        List<String> ids = List.of("id1", "id2");
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("id1");
        when(mcpServiceMapper.selectList(ids, null, null, "ws1")).thenReturn(List.of(entity));

        List<McpServiceEntity> result = mcpInnerService.getMcpByIds("ws1", ids);
        assertEquals(1, result.size());
    }

    @Test
    void testGetMcpServerInfoByIds_EmptyList() {
        List<McpServerInfo> result = mcpInnerService.getMcpServerInfoByIds("ws1", new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMcpServerInfoByIds_EmptyQueryResult() {
        when(mcpServiceMapper.selectList(anyList(), isNull(), isNull(), anyString())).thenReturn(new ArrayList<>());

        List<McpServerInfo> result = mcpInnerService.getMcpServerInfoByIds("ws1", List.of("id1"));
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMcpServerInfoByIds_WithData() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("id1");
        entity.setName("test-mcp");
        entity.setProjectId("proj1");
        entity.setWorkspaceId("ws1");
        entity.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        entity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));

        when(mcpServiceMapper.selectList(List.of("id1"), null, null, "ws1")).thenReturn(List.of(entity));

        List<McpServerInfo> result = mcpInnerService.getMcpServerInfoByIds("ws1", List.of("id1"));
        assertEquals(1, result.size());
        assertEquals("id1", result.get(0).getServerId());
    }

    @Test
    void testGetMcpServerInfoById_Found() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("id1");
        entity.setName("test-mcp");
        entity.setProjectId("proj1");
        entity.setWorkspaceId("ws1");
        entity.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        entity.setLastUpdatedDate(new Timestamp(System.currentTimeMillis()));

        when(mcpServiceMapper.selectByPrimaryKey("id1", "ws1")).thenReturn(entity);

        McpServerInfo result = mcpInnerService.getMcpServerInfoById("id1", "ws1");
        assertNotNull(result);
        assertEquals("id1", result.getServerId());
    }

    @Test
    void testGetMcpServerInfoById_NotFound() {
        when(mcpServiceMapper.selectByPrimaryKey("id1", "ws1")).thenReturn(null);

        McpServerInfo result = mcpInnerService.getMcpServerInfoById("id1", "ws1");
        assertNull(result);
    }

    @Test
    void testGetMcpByName_Found() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setName("test-mcp");
        when(mcpServiceMapper.selectByName("test-mcp", null, null, "ws1")).thenReturn(List.of(entity));

        McpServiceEntity result = mcpInnerService.getMcpByName("ws1", "test-mcp");
        assertNotNull(result);
    }

    @Test
    void testGetMcpByName_NotFound() {
        when(mcpServiceMapper.selectByName("nonexistent", null, null, "ws1")).thenReturn(new ArrayList<>());

        assertThrows(AgentStudioException.class, () ->
            mcpInnerService.getMcpByName("ws1", "nonexistent"));
    }

    @Test
    void testIsMcpExist_True() {
        McpServiceEntity entity = new McpServiceEntity();
        when(mcpServiceMapper.selectByName("test-mcp", null, null, "ws1")).thenReturn(List.of(entity));

        assertTrue(mcpInnerService.isMcpExist("ws1", "test-mcp"));
    }

    @Test
    void testIsMcpExist_False() {
        when(mcpServiceMapper.selectByName("nonexistent", null, null, "ws1")).thenReturn(new ArrayList<>());

        assertFalse(mcpInnerService.isMcpExist("ws1", "nonexistent"));
    }

    @Test
    void testBuildServerConfig_BlankText() {
        String result = mcpInnerService.buildServerConfig("");
        assertEquals("", result);
    }

    @Test
    void testBuildServerConfig_NullText() {
        String result = mcpInnerService.buildServerConfig(null);
        assertNull(result);
    }

    @Test
    void testBuildServerConfig_EncryptedText() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain1");

            when(encryptionAdapter.decrypt("encrypted_text")).thenReturn("decrypted_text");

            String result = mcpInnerService.buildServerConfig("encrypted_text");
            assertEquals("encrypted_text", result);
        }
    }

    @Test
    void testBuildServerConfig_PlainText() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain1");

            when(encryptionAdapter.decrypt("plain_text")).thenReturn("plain_text");
            when(encryptionAdapter.encrypt("plain_text", "domain1")).thenReturn("encrypted_result");

            String result = mcpInnerService.buildServerConfig("plain_text");
            assertEquals("encrypted_result", result);
        }
    }

    @Test
    void testCreateOrUpdate_NewMcp() {
        McpServerInfo server = new McpServerInfo();
        server.setServerId("s1");
        server.setName("test-mcp");
        server.setProjectId("proj1");
        server.setCreateTime(new Date());
        server.setUpdateTime(new Date());

        when(mcpServiceMapper.selectByName("test-mcp", null, null, "ws1")).thenReturn(new ArrayList<>());
        when(mcpServiceMapper.insert(any(McpServiceEntity.class))).thenReturn(1);

        boolean result = mcpInnerService.createOrUpdate(server, "ws1");
        assertTrue(result);
        verify(mcpServiceMapper).insert(any(McpServiceEntity.class));
    }

    @Test
    void testCreateOrUpdate_ExistingMcp() {
        McpServerInfo server = new McpServerInfo();
        server.setServerId("s1");
        server.setName("test-mcp");
        server.setProjectId("proj1");
        server.setCreateTime(new Date());
        server.setUpdateTime(new Date());

        McpServiceEntity existing = new McpServiceEntity();
        when(mcpServiceMapper.selectByName("test-mcp", null, null, "ws1")).thenReturn(List.of(existing));
        when(mcpServiceMapper.updateById(eq("s1"), any(McpServiceEntity.class))).thenReturn(1);

        boolean result = mcpInnerService.createOrUpdate(server, "ws1");
        assertTrue(result);
        verify(mcpServiceMapper).updateById(eq("s1"), any(McpServiceEntity.class));
    }

    @Test
    void testCreateOrUpdate_ExceptionThrown() {
        McpServerInfo server = new McpServerInfo();
        server.setServerId("s1");
        server.setName("test-mcp");
        server.setProjectId("proj1");
        server.setCreateTime(new Date());
        server.setUpdateTime(new Date());

        when(mcpServiceMapper.selectByName("test-mcp", null, null, "ws1")).thenReturn(new ArrayList<>());
        when(mcpServiceMapper.insert(any(McpServiceEntity.class))).thenThrow(new RuntimeException("DB error"));

        boolean result = mcpInnerService.createOrUpdate(server, "ws1");
        assertFalse(result);
    }
}
