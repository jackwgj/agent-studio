/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.entity.ShareResourcePromptEntity;
import com.openjiuwen.studio.prompt.engineering.mapper.ShareResourcePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.ShareScopePromptMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ShareInnerPromptService 单元测试类
 */
@ExtendWith(MockitoExtension.class)
class ShareInnerPromptServiceTest {

    @Mock
    private ShareScopePromptMapper shareScopePromptMapper;

    @Mock
    private ShareResourcePromptMapper shareResourcePromptMapper;

    @InjectMocks
    private ShareInnerPromptService shareInnerPromptService;

    // ========== 原有测试 ==========

    @Test
    void testCancelPromptShared_WithShareRecord_ThrowException() {
        String templateId = "test-template-001";
        String projectId = "test-project-001";
        String workspaceId = "test-workspace-001";

        ShareResourcePromptEntity mockShareResource = new ShareResourcePromptEntity();
        when(shareResourcePromptMapper.selectShareResourceEntityById(templateId, projectId, workspaceId))
            .thenReturn(mockShareResource);

        AgentStudioException exception = Assertions.assertThrows(
            AgentStudioException.class,
            () -> shareInnerPromptService.cancelPromptShared(templateId, projectId, workspaceId)
        );

        verify(shareResourcePromptMapper, times(1))
            .selectShareResourceEntityById(templateId, projectId, workspaceId);
    }

    @Test
    void testCancelPromptShared_WithoutShareRecord_ReturnTrue() {
        String templateId = "test-template-002";
        String projectId = "test-project-002";
        String workspaceId = "test-workspace-002";

        when(shareResourcePromptMapper.selectShareResourceEntityById(templateId, projectId, workspaceId))
            .thenReturn(null);

        boolean result = shareInnerPromptService.cancelPromptShared(templateId, projectId, workspaceId);

        Assertions.assertTrue(result);
        verify(shareResourcePromptMapper, times(1))
            .selectShareResourceEntityById(templateId, projectId, workspaceId);
    }

    @Test
    void testCancelPromptShared_WithNullParams_ReturnTrue() {
        when(shareResourcePromptMapper.selectShareResourceEntityById(null, null, null))
            .thenReturn(null);

        boolean result = shareInnerPromptService.cancelPromptShared(null, null, null);

        Assertions.assertTrue(result);
        verify(shareResourcePromptMapper, times(1))
            .selectShareResourceEntityById(null, null, null);
    }

    // ========== 补充测试 ==========

    @Test
    void testCancelPromptShared_EmptyString_SharedResourceExists() {
        ShareResourcePromptEntity entity = new ShareResourcePromptEntity();
        when(shareResourcePromptMapper.selectShareResourceEntityById("", "", ""))
            .thenReturn(entity);

        AgentStudioException exception = Assertions.assertThrows(
            AgentStudioException.class,
            () -> shareInnerPromptService.cancelPromptShared("", "", "")
        );

        Assertions.assertEquals(StudioError.SHARE_RESOURCE_CANNOT_BE_DELETE_DIRECTLY, exception.getErrorCode());
    }

    @Test
    void testCancelPromptShared_VerifyMapperCalled() {
        String templateId = "tmpl-001";
        String projectId = "proj-001";
        String workspaceId = "ws-001";

        when(shareResourcePromptMapper.selectShareResourceEntityById(templateId, projectId, workspaceId))
            .thenReturn(null);

        boolean result = shareInnerPromptService.cancelPromptShared(templateId, projectId, workspaceId);
        Assertions.assertTrue(result);
        verify(shareResourcePromptMapper).selectShareResourceEntityById(templateId, projectId, workspaceId);
    }

    @Test
    void testCancelPromptShared_MultipleNullChecks() {
        when(shareResourcePromptMapper.selectShareResourceEntityById("tmpl-001", null, null))
            .thenReturn(null);

        boolean result = shareInnerPromptService.cancelPromptShared("tmpl-001", null, null);
        Assertions.assertTrue(result);
    }
}
