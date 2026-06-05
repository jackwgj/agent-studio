/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;
import com.openjiuwen.studio.agent.space.common.validator.ValidStringList;

import lombok.Data;

import java.util.List;

/**
 * AI PPT创建任务请求
 */
@Data
public class AiPptCreateTaskReq {
    @ValidNormalString
    @JsonProperty("task_id")
    private String taskId;

    @ValidStringList(maxSize = 5)
    @JsonProperty("files")
    private List<String> files;
}
