/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.entity.ShareResourcePromptEntity;
import com.openjiuwen.studio.prompt.engineering.mapper.ShareResourcePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.ShareScopePromptMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ShareInnerPromptService 单元测试类
 */
@ExtendWith(MockitoExtension.class) // 启用Mockito扩展
public class ShareInnerPromptServiceTest {

    // 模拟Mapper（不需要真实数据库连接）
    @Mock
    private ShareScopePromptMapper shareScopePromptMapper;

    @Mock
    private ShareResourcePromptMapper shareResourcePromptMapper;

    // 将Mock的Mapper注入到待测试的Service中
    @InjectMocks
    private ShareInnerPromptService shareInnerPromptService;

    // 测试场景1：查询到共享记录（非空），应抛出指定异常
    @Test
    void testCancelPromptShared_WithShareRecord_ThrowException() {
        // 1. 准备测试数据
        String templateId = "test-template-001";
        String projectId = "test-project-001";
        String workspaceId = "test-workspace-001";

        // 模拟Mapper查询返回非空的ShareResourcePromptEntity
        ShareResourcePromptEntity mockShareResource = new ShareResourcePromptEntity();
        when(shareResourcePromptMapper.selectShareResourceEntityById(templateId, projectId, workspaceId))
            .thenReturn(mockShareResource);

        // 2. 执行测试并验证异常
        AgentStudioException exception = Assertions.assertThrows(
            AgentStudioException.class,
            () -> shareInnerPromptService.cancelPromptShared(templateId, projectId, workspaceId)
        );

        // 3. 验证异常信息是否正确
        // 验证Mapper方法被调用且仅调用一次
        verify(shareResourcePromptMapper, times(1))
            .selectShareResourceEntityById(templateId, projectId, workspaceId);
    }

    // 测试场景2：未查询到共享记录（为空），应返回true
    @Test
    void testCancelPromptShared_WithoutShareRecord_ReturnTrue() {
        // 1. 准备测试数据
        String templateId = "test-template-002";
        String projectId = "test-project-002";
        String workspaceId = "test-workspace-002";

        // 模拟Mapper查询返回null
        when(shareResourcePromptMapper.selectShareResourceEntityById(templateId, projectId, workspaceId))
            .thenReturn(null);

        // 2. 执行测试方法
        boolean result = shareInnerPromptService.cancelPromptShared(templateId, projectId, workspaceId);

        // 3. 验证结果
        Assertions.assertTrue(result); // 验证返回值为true
        // 验证Mapper方法被调用且仅调用一次
        verify(shareResourcePromptMapper, times(1))
            .selectShareResourceEntityById(templateId, projectId, workspaceId);
    }

    // 测试场景3：参数为null（边界场景），验证逻辑仍正常执行
    @Test
    void testCancelPromptShared_WithNullParams_ReturnTrue() {
        // 1. 模拟Mapper查询null（参数为null时也返回null）
        when(shareResourcePromptMapper.selectShareResourceEntityById(null, null, null))
            .thenReturn(null);

        // 2. 执行测试（参数全为null）
        boolean result = shareInnerPromptService.cancelPromptShared(null, null, null);

        // 3. 验证
        Assertions.assertTrue(result);
        verify(shareResourcePromptMapper, times(1))
            .selectShareResourceEntityById(null, null, null);
    }
}
