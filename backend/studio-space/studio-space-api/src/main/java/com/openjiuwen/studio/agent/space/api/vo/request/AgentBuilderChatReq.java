/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.common.validator.IntegerEnumValue;
import com.openjiuwen.studio.agent.space.common.validator.StringEnumValue;
import com.openjiuwen.studio.agent.space.common.validator.ValidChatString;
import com.openjiuwen.studio.agent.space.common.validator.ValidJsonString;
import com.openjiuwen.studio.agent.space.common.validator.ValidNormalString;
import com.openjiuwen.studio.agent.space.common.validator.ValidStringList;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Chat请求类
 */
@Data
@Accessors(chain = true)
public class AgentBuilderChatReq {
    @NotBlank
    @Size(max = 64)
    @ValidNormalString
    @JsonProperty("task_id")
    private String taskId;

    @NotBlank
    @Size(max = 20000)
    @ValidChatString
    @JsonProperty("query")
    private String query;

    @Valid
    @JsonProperty("inputs")
    private ChatInputRequest inputs = new ChatInputRequest();

    @ValidStringList(maxSize = 1000)
    @JsonProperty("memories")
    private List<String> memories;

    @Size(max = 10000)
    @ValidJsonString
    @JsonProperty("planning_form")
    private String planningForm;

    /**
     * 对话类型
     * 0：首次用户问答；1：后续用户补充
     */
    @IntegerEnumValue({0, 1})
    @JsonProperty("chat_type")
    private int chatType;

    /**
     * 是否关闭联网搜索
     * 0：关闭联网搜索，1：开启联网搜索
     */
    @IntegerEnumValue({0, 1})
    @JsonProperty("enable_web_search")
    private int enableWebSearch = 1;

    /**
     * 是否是复杂任务
     * auto：自动判断是否是复杂任务；chat：简单任务；agent：复杂任务
     */
    @StringEnumValue(values = {"auto", "chat", "agent"})
    @JsonProperty("input_dialog_mode")
    private String inputDialogMode = "auto";

    /**
     * 是否开启深度思考
     * 0：关闭深度思考，1：开启深度思考
     */
    @IntegerEnumValue({0, 1})
    @JsonProperty("enable_thinking")
    private int enableThinking = 0;

    @ValidStringList(maxSize = 5)
    @JsonProperty("files")
    private List<String> files;
}
