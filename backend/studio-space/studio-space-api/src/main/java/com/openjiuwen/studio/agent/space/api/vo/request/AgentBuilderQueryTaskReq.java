/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.ValidChatString;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 查询任务请求
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentBuilderQueryTaskReq {
    @Size(max = 64)
    @ValidChatString
    @JsonProperty("search_key")
    private String searchKey;

    @Min(value = 1, message = "page num must be more than 1")
    private int num;

    @Min(value = 1, message = "page size must be more than 1")
    @Max(value = 1000, message = "page size must be less than 1000")
    private int size;

    /**
     * 运行方式
     * 0：AgentSpace；1：AgentWeb
     */
    @JsonProperty("run_type")
    private int runType = 0;

    @JsonProperty("agent_id")
    private String agentId;
}