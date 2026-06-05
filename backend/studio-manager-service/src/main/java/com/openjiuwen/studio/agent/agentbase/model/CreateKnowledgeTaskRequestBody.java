/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建任务的请求体
 *
 * @since 2025-04-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateKnowledgeTaskRequestBody {
    @JsonProperty("task_type")
    private String taskType;

    @JsonProperty("file_ids")
    private List<String> fileIds;
}
