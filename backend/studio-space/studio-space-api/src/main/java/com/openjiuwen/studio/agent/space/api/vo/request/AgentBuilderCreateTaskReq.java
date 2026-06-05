/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.IntegerEnumValue;
import com.openjiuwen.studio.agent.space.common.validator.ValidChatString;
import com.openjiuwen.studio.agent.space.common.validator.ValidJsonString;
import com.openjiuwen.studio.agent.space.common.validator.ValidStringList;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 任务创建请求
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentBuilderCreateTaskReq {
    @NotBlank
    @Size(max = 64)
    @ValidChatString
    @JsonProperty("task_name")
    private String taskName;

    @JsonProperty("agent_type")
    private int agentType = 1;

    /**
     * 运行方式
     * 0：AgentSpace；1：AgentWeb
     */
    @IntegerEnumValue({0, 1})
    @JsonProperty("run_type")
    private int runType = 0;

    /**
     * 运行的agent
     * 由AgentWeb发起的请求，存储的是shortCode
     */
    @NotBlank
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 用户agent列表
     * task_type是2和3才有意义
     */
    @ValidStringList(maxSize = 5)
    @JsonProperty("agent_list")
    private List<String> agentList = Collections.emptyList();

    /**
     * 运行模式
     * 0：探索模式
     * 1：规划模式
     */
    @IntegerEnumValue({0, 1})
    @JsonProperty("run_mode")
    private int runMode;

    /**
     * json
     * 兼容自定agent场景的额外配置
     */
    @Size(max = 10000)
    @ValidJsonString
    @JsonProperty("extra_agent_config")
    private String extraAgentConfig;
}
