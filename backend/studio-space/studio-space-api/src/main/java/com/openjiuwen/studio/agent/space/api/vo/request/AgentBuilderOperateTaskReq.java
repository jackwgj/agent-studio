/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.IntegerEnumValue;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 任务操作类
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentBuilderOperateTaskReq {
    @NotBlank
    @Size(max = 64)
    @ValidNormalString
    @JsonProperty("task_id")
    private String taskId;

    /**
     * 操作类型
     * 1：删除；4：置顶；5：取消置顶；6：打分；7：手动停止
     */
    @JsonProperty("operate_type")
    @IntegerEnumValue({1, 4, 5, 6, 7})
    private int operateType;

    @Size(max = 64)
    @ValidNormalString
    @JsonProperty("answer_id")
    private String msgId;

    @JsonProperty("rating")
    @IntegerEnumValue({-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    private int rating;
}
