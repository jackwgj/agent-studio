/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import com.openjiuwen.studio.agent.runtime.dto.MemoryVariable;
import com.openjiuwen.studio.agent.common.dto.TriggerConfig;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * IR metadata信息
 *
 */
@Data
public class AgentIrMetadata {
    /**
     * 用户userid
     */
    private String userId;

    /**
     * 租户项目id
     */
    private String projectId;

    /**
     * 租户domain id
     */
    private String domainId;

    /**
     * agent IR更新时间
     */
    private Date updatedAt;

    /**
     * agent 发布时间
     */
    private long publishedAt;

    /**
     * agent可见性
     */
    private String visibility;

    /**
     * agent 状态
     */
    private String status;

    /**
     * 记忆参数列表
     */
    private List<MemoryVariable> memoryVariables;

    /**
     * 触发器列表
     */
    private List<TriggerConfig> triggerConfigs;

    public Date getUpdatedAt() {
        return updatedAt == null ? new Date() : updatedAt;
    }

    private String workspaceId;
}
