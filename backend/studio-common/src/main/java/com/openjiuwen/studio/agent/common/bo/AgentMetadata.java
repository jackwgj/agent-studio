/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.bo;

import lombok.Builder;
import lombok.Data;

/**
 * 运行态使用的元数据
 *
 */
@Data
@Builder
public class AgentMetadata {
    private String agentId;

    private String domainId;

    private String projectId;

    private String workspaceId;

    private String versionId;

    /**
     * 最新发布的时间，或IR中的更新时间（草稿）
     */
    private long updatedAt;

    private boolean isContentReviewEnable;

    private boolean safetyBarrier;

    /**
     * 应用详细类型
     * 工作流-chat/task
     * 智能体-未对接
     */
    private String appType;
}
