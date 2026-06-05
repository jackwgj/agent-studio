/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import lombok.Builder;
import lombok.Data;

/**
 * 会话管理参数集，workflow和agent通用
 *
 */
@Data
@Builder
public class ConversationParams {
    /**
     * 工作流或agent id
     */
    private String id;

    /**
     * project id
     */
    private String projectId;

    /**
     * 会话id
     */
    private String conversationId;

    /**
     * 版本id
     */
    private String versionId;

    /**
     * 执行类型
     */
    private String executeType;
}
