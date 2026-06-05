/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 任务实体
 *
 */
@Data
@Builder
public class TaskEntity {
    /**
     * 任务id
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 会话id
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 用户id
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 域账号id
     */
    @JsonProperty("domain_id")
    private String domainId;

    /**
     * 工作空间id
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * project id
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 任务类型，支持Agent/Workflow
     */
    private String type;

    /**
     * 任务模式，当前支持ASYNC
     */
    private String mode;

    /**
     * 任务对应工作流或Agent id
     */
    @JsonProperty("app_id")
    private String appId;

    /**
     * 任务对应工作流或Agent版本
     */
    @JsonProperty("app_version")
    private String appVersion;

    /**
     * 是否是发布的版本
     */
    @JsonProperty("is_published")
    private Boolean isPublished;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 任务输入
     */
    private String inputs;

    /**
     * 任务输出
     */
    private String outputs;

    /**
     * 任务超时时间
     */
    private Integer timeout;

    /**
     * 任务信息
     */
    private String message;

    /**
     * 任务创建时间
     */
    @JsonProperty("create_time")
    private Date createTime;

    /**
     * 任务实际开始时间
     */
    @JsonProperty("start_time")
    private Date startTime;

    /**
     * 任务更新时间
     */
    @JsonProperty("update_time")
    private Date updateTime;

    /**
     * 任务结束时间
     */
    @JsonProperty("finish_time")
    private Date finishTime;
}
