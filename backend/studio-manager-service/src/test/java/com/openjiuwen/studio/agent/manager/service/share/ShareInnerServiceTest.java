/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.share;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareScopeEntity;
import com.openjiuwen.studio.agent.manager.mapper.ShareResourceMapper;
import com.openjiuwen.studio.agent.manager.mapper.ShareScopeMapper;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareInnerServiceTest {

    @Mock
    private ShareScopeMapper shareScopeMapper;

    @Mock
    private ShareResourceMapper shareResourceMapper;

    @Mock
    private McpServiceDao serviceDao;

    @InjectMocks
    private ShareInnerService shareInnerService;

    @Test
    void testGetShareScopeByWorkspaceAndId_ReturnsData() {
        List<String> resourceIds = List.of("r1", "r2");
        List<ShareScopeEntity> expected = List.of(new ShareScopeEntity());
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(resourceIds, "ws1")).thenReturn(expected);

        List<ShareScopeEntity> result = shareInnerService.getShareScopeByWorkspaceAndId(resourceIds, "ws1");
        assertEquals(1, result.size());
    }

    @Test
    void testGetShareScopeByWorkspaceAndId_EmptyResult() {
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(anyList(), anyString()))
            .thenReturn(new ArrayList<>());

        List<ShareScopeEntity> result = shareInnerService.getShareScopeByWorkspaceAndId(List.of("r1"), "ws1");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOriginSharedInfoByResourceId_ReturnsData() {
        List<String> resourceIds = List.of("r1");
        List<ShareResourceEntity> expected = List.of(new ShareResourceEntity());
        when(shareResourceMapper.selectShareResourceByResourceIds(resourceIds)).thenReturn(expected);

        List<ShareResourceEntity> result = shareInnerService.getOriginSharedInfoByResourceId(resourceIds);
        assertEquals(1, result.size());
    }

    @Test
    void testCancelPluginShared_NoShareRecord() {
        when(shareResourceMapper.selectShareResourceEntityById("proj1", "ws1", "plugin1")).thenReturn(null);

        boolean result = shareInnerService.cancelPluginShared("proj1", "ws1", "plugin1");
        assertTrue(result);
    }

    @Test
    void testCancelPluginShared_HasShareRecord() {
        ShareResourceEntity entity = new ShareResourceEntity();
        when(shareResourceMapper.selectShareResourceEntityById("proj1", "ws1", "plugin1")).thenReturn(entity);

        assertThrows(AgentStudioException.class, () ->
            shareInnerService.cancelPluginShared("proj1", "ws1", "plugin1"));
    }

    @Test
    void testCancelPluginVersionShared_NoShareRecord() {
        when(shareResourceMapper.selectShareResourceEntityById("proj1", "ws1", "plugin1")).thenReturn(null);

        boolean result = shareInnerService.cancelPluginVersionShared("proj1", "ws1", "plugin1", "v1");
        assertTrue(result);
    }

    @Test
    void testCancelPluginVersionShared_VersionNotInShareList() {
        ShareResourceEntity entity = new ShareResourceEntity();
        entity.setVersionList("[{\"versionId\":\"v2\"}]");
        when(shareResourceMapper.selectShareResourceEntityById("proj1", "ws1", "plugin1")).thenReturn(entity);

        boolean result = shareInnerService.cancelPluginVersionShared("proj1", "ws1", "plugin1", "v1");
        assertTrue(result);
    }

    @Test
    void testCancelPluginVersionShared_VersionInShareList() {
        ShareResourceEntity entity = new ShareResourceEntity();
        entity.setVersionList("[{\"versionId\":\"v1\"}]");
        when(shareResourceMapper.selectShareResourceEntityById("proj1", "ws1", "plugin1")).thenReturn(entity);

        assertThrows(AgentStudioException.class, () ->
            shareInnerService.cancelPluginVersionShared("proj1", "ws1", "plugin1", "v1"));
    }

    @Test
    void testCancelMCPShared_NoShareRecord() {
        when(shareResourceMapper.selectShareResourceEntityById("proj1", "ws1", "mcp1")).thenReturn(null);

        boolean result = shareInnerService.cancelMCPShared("mcp1", "proj1", "ws1");
        assertTrue(result);
    }

    @Test
    void testCancelMCPShared_HasShareRecord() {
        ShareResourceEntity entity = new ShareResourceEntity();
        when(shareResourceMapper.selectShareResourceEntityById("proj1", "ws1", "mcp1")).thenReturn(entity);

        assertThrows(AgentStudioException.class, () ->
            shareInnerService.cancelMCPShared("mcp1", "proj1", "ws1"));
    }

    @Test
    void testGetShareMcp_EmptyScopeEntities() {
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(anyList(), anyString()))
            .thenReturn(new ArrayList<>());

        List<McpServiceEntity> result = shareInnerService.getShareMcp(List.of("s1"), "ws1");
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetShareMcp_WithData() {
        ShareScopeEntity scope = new ShareScopeEntity();
        scope.setResourceId("s1");
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(List.of("s1"), "ws1"))
            .thenReturn(List.of(scope));

        ShareResourceEntity resource = new ShareResourceEntity();
        when(shareResourceMapper.selectShareResourceByResourceIds(List.of("s1")))
            .thenReturn(List.of(resource));

        McpServiceEntity mcpEntity = new McpServiceEntity();
        when(serviceDao.listMcpServiceByShareResourceEntity(anyList())).thenReturn(List.of(mcpEntity));

        List<McpServiceEntity> result = shareInnerService.getShareMcp(List.of("s1"), "ws1");
        assertEquals(1, result.size());
    }

    @Test
    void testIsShared_True() {
        ShareScopeEntity scope = new ShareScopeEntity();
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(List.of("r1"), "ws1"))
            .thenReturn(List.of(scope));

        assertTrue(shareInnerService.isShared("r1", "ws1"));
    }

    @Test
    void testIsShared_False() {
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(List.of("r1"), "ws1"))
            .thenReturn(new ArrayList<>());

        assertFalse(shareInnerService.isShared("r1", "ws1"));
    }
}
