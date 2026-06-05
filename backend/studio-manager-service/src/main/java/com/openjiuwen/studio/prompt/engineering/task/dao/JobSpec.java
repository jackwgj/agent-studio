/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Job描述
 *
 */
@Data
public class JobSpec {
    private String id;

    @NotNull
    private LocalDateTime createdOn;

    @NotNull
    private LocalDateTime updatedOn;

    /**
     * 从业务上看任务的名称，不同的业务任务使用不同的名字。
     * 并不是枚举，后续可能会增加新的任务
     */
    private String name;

    /**
     * 抽象的任务类型，这个面向抽象的任务管理系统的。
     */
    private JobType type;

    @JsonProperty("workspace_id")
    private String workspaceId;
}
