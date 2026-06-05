/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.ResourceVersionInfo;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareScopeEntity;
import com.openjiuwen.studio.agent.manager.mapper.ShareResourceMapper;
import com.openjiuwen.studio.agent.manager.mapper.ShareScopeMapper;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class ShareInnerServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareScopeMapper shareScopeMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareResourceMapper shareResourceMapper;

    @InjectMocks
    private ShareInnerService shareInnerService;

    private AutoCloseable mockitoCloseable;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServiceDao serviceDao;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_getShareScopeByWorkspaceAndId_should_return_not_null() throws Exception {
        // Given
        List<ShareScopeEntity> list = new ArrayList<>();
        ShareScopeEntity shareScopeEntity = new ShareScopeEntity();
        list.add(shareScopeEntity);
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(anyList(), anyString())).thenReturn(list);

        List<String> resourceId = new ArrayList<>();
        resourceId.add("not_empty");

        // When
        List<ShareScopeEntity> result = shareInnerService.getShareScopeByWorkspaceAndId(resourceId, "not_empty");

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getShareScopeByWorkspaceAndId_should_return_not_null1() throws Exception {
        // Given
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(anyList(), anyString())).thenReturn(null);

        // When
        List<ShareScopeEntity> result = shareInnerService.getShareScopeByWorkspaceAndId(null, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getOriginSharedInfoByResourceId_should_return_not_null() throws Exception {
        // Given
        List<ShareResourceEntity> list = new ArrayList<>();
        ShareResourceEntity shareResourceEntity = new ShareResourceEntity();
        list.add(shareResourceEntity);
        when(shareResourceMapper.selectShareResourceByResourceIds(anyList())).thenReturn(list);

        List<String> resourceIds = new ArrayList<>();
        resourceIds.add("not_empty");

        // When
        List<ShareResourceEntity> result = shareInnerService.getOriginSharedInfoByResourceId(resourceIds);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_getOriginSharedInfoByResourceId_should_return_not_null1() throws Exception {
        // Given
        when(shareResourceMapper.selectShareResourceByResourceIds(anyList())).thenReturn(null);

        // When
        List<ShareResourceEntity> result = shareInnerService.getOriginSharedInfoByResourceId(null);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_cancelPluginVersionShared_share_resource_is_null() throws Exception {
        try (MockedStatic<ObjectUtils> objectUtilsMock = mockStatic(ObjectUtils.class)) {
            ShareResourceEntity shareResourceEntity = null;
            when(shareResourceMapper.selectShareResourceEntityById(anyString(), anyString(), anyString())).thenReturn(
                shareResourceEntity);
            objectUtilsMock.when(() -> ObjectUtils.isEmpty(shareResourceEntity)).thenReturn(true);

            boolean result = shareInnerService.cancelPluginVersionShared("projectId", "workspaceId", "pluginId",
                "versionId");
            assertTrue(result);
        }
    }

    @Test
    void test_cancelPluginVersionShared_version_list_has_match() throws Exception {
        try (MockedStatic<ObjectUtils> objectUtilsMock = mockStatic(ObjectUtils.class)) {

            ShareResourceEntity shareResourceEntity = mock(ShareResourceEntity.class);
            when(shareResourceMapper.selectShareResourceEntityById(anyString(), anyString(), anyString())).thenReturn(
                shareResourceEntity);
            objectUtilsMock.when(() -> ObjectUtils.isEmpty(shareResourceEntity)).thenReturn(false);

            ResourceVersionInfo versionInfo = mock(ResourceVersionInfo.class);
            when(versionInfo.getVersionId()).thenReturn("versionId");
            List<ResourceVersionInfo> versionInfos = List.of(versionInfo);
            when(shareResourceEntity.getVersionList()).thenReturn(
                "[{\"version_id\":\"versionId\",\"version_name\":\"12345\"}]");

            assertThrows(AgentStudioException.class, () -> {
                shareInnerService.cancelPluginVersionShared("projectId", "workspaceId", "pluginId", "versionId");
            });
        }
    }

    @Test
    void testCancelMCPShared_WithSharedRecord_ThrowException() {
        // 1. 准备测试参数
        String mcpId = "mcp-123456";
        String projectId = "project-789";
        String workspaceId = "workspace-001";

        // 2. 模拟Mapper返回非空的共享记录
        ShareResourceEntity mockShareResource = new ShareResourceEntity();
        when(shareResourceMapper.selectShareResourceEntityById(projectId, workspaceId, mcpId)).thenReturn(
            mockShareResource);

        // 3. 执行方法并验证异常
        AgentStudioException exception = Assertions.assertThrows(AgentStudioException.class, // 预期抛出的异常类型
            () -> shareInnerService.cancelMCPShared(mcpId, projectId, workspaceId) // 执行待测试方法
        );

        // 4. 验证异常信息是否正确
        // 验证Mapper方法被调用且仅调用1次（确保逻辑走了查询步骤）
        verify(shareResourceMapper, times(1)).selectShareResourceEntityById(projectId, workspaceId, mcpId);
        // 验证错误日志被打印（可选，若需验证日志可结合slf4j-test）
    }

    // 测试场景2：无MCP共享记录（查询结果为空），应返回true
    @Test
    void testCancelMCPShared_WithoutSharedRecord_ReturnTrue() {
        // 1. 准备测试参数
        String mcpId = "mcp-654321";
        String projectId = "project-987";
        String workspaceId = "workspace-002";

        // 2. 模拟Mapper返回null（无共享记录）
        when(shareResourceMapper.selectShareResourceEntityById(projectId, workspaceId, mcpId)).thenReturn(null);

        // 3. 执行待测试方法
        boolean result = shareInnerService.cancelMCPShared(mcpId, projectId, workspaceId);

        // 4. 验证结果
        Assertions.assertTrue(result); // 验证返回值为true
        // 验证Mapper方法仅被调用1次
        verify(shareResourceMapper, times(1)).selectShareResourceEntityById(projectId, workspaceId, mcpId);
    }

    @Test
    void test_getShareMcp_share_scope_entities() throws Exception {
        // Arrange
        List<String> serviceIds = new ArrayList<>();
        String workspaceId = "workspace1";
        ShareScopeEntity shareScopeEntity = new ShareScopeEntity();
        shareScopeEntity.setId("1111");
        List<ShareScopeEntity> shareScopeEntities = new ArrayList<>();
        shareScopeEntities.add(shareScopeEntity);
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(any(), any())).thenReturn(
            shareScopeEntities);
        when(serviceDao.listMcpServiceByShareResourceEntity(any())).thenReturn(new ArrayList<>());
        // Act
        List<McpServiceEntity> result = shareInnerService.getShareMcp(serviceIds, workspaceId);

        // Assert
        assertTrue(result.isEmpty());

    }

    @Test
    void test_isShared() throws Exception {
        // Arrange
        String workspaceId = "workspace1";
        ShareScopeEntity shareScopeEntity = new ShareScopeEntity();
        shareScopeEntity.setId("1111");
        List<ShareScopeEntity> shareScopeEntities = new ArrayList<>();
        shareScopeEntities.add(shareScopeEntity);
        when(shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(any(), any())).thenReturn(
            shareScopeEntities);
        // Act
        boolean result = shareInnerService.isShared("serviceIds", workspaceId);

        // Assert
        assertTrue(result);

    }

}
