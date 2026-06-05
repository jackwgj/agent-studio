/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.task.dao;

import com.openjiuwen.studio.prompt.engineering.task.domain.Context;
import com.openjiuwen.studio.prompt.engineering.task.domain.JobStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务实例
 *
 */
@Data
public class JobInstance {
    private String id;

    private String name;

    @NotNull
    private LocalDateTime createdOn;

    @NotNull
    private LocalDateTime updatedOn;

    private String jobSpecId;

    private Context context;

    /**
     * 重试次数，如果没有重试，则是0
     */
    private int retryTime;

    private JobStatus status;

    private LocalDateTime startedOn;

    private LocalDateTime endedOn;

    /**
     * 唯一标识一个执行者
     */
    private String handlerId;

    private String accountId;

    /**
     * 关联的JobSpec对象
     */
    private JobSpec jobSpec;

    /**
     * 关联的用户信息
     */
    private UserEntity user;

    private String workspaceId;

    private String projectId;
}
