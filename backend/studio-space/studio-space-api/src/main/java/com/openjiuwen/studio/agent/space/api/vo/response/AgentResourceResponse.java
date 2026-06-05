/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 删除agent的响应体
 */
@Data
public class AgentResourceResponse {
    /**
     * 成功删除的taskId
     */
    @JsonProperty("task_ids")
    private List<String> taskIds;
}
