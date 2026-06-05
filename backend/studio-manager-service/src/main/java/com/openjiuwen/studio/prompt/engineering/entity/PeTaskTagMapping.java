/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 标签和任务关联关系表
 *
 * @TableName t_mapping_pe_task_tag
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeTaskTagMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工程任务id
     */
    private String taskId;

    /**
     * 标签id
     */
    private String tagId;

    private String workspaceId;
}
