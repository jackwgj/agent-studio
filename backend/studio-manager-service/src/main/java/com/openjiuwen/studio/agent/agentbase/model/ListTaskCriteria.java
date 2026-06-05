/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务列表查询条件
 *
 * @since 2025-04-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListTaskCriteria {
    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("task_type")
    private String taskType;

    @JsonProperty("task_status")
    private String taskStatus;

    @JsonProperty("page_num")
    private int pageNum;

    @JsonProperty("page_size")
    private int pageSize;
}
