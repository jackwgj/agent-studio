/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.IntegerEnumValue;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AgentBudiler智能体快捷指令创建请求
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentBuilderOperateQuickCommandReq {
    /**
     * 快捷指令id
     */
    @Size(max = 64)
    @JsonProperty("command_id")
    private String commandId;

    /**
     * 对应智能体应用id，预置智能体应用暂时不包含
     */
    @NotBlank
    @Size(max = 64)
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 快捷指令名称
     */
    @Size(max = 64)
    @JsonProperty("quick_command_name")
    private String quickCommandName;

    /**
     * 开启关闭
     * 0：关闭
     * 1：开启
     */
    @IntegerEnumValue({0, 1})
    @JsonProperty("active")
    private Integer active;

    /**
     * 快捷指令描述
     */
    @Size(max = 1028)
    @JsonProperty("description")
    private String description;

    /**
     * 快捷指令内容
     */
    @Size(max = 1028)
    @JsonProperty("content")
    private String content;

    /**
     * 快捷指令序号
     */
    @Min(value = 1)
    @Max(value = Integer.MAX_VALUE)
    @JsonProperty("display_no")
    private Integer displayNo;

    /**
     * 是否为推荐快捷指令
     * 0：非推荐指令
     * 1：推荐指令
     */
    @IntegerEnumValue({0, 1})
    @JsonProperty("recommend")
    private Integer recommend;

    /**
     * 操作类型
     * 1：创建；2：修改；3：删除；4：开关；
     */
    @NotNull
    @IntegerEnumValue({1, 2, 3, 4})
    @JsonProperty("operate_type")
    private Integer operateType;
}