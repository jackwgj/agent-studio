/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 内置沙箱组件参数，用于json解析
 *
 */
@Data
public class InnerCodeInterpreterParam {
    @JsonProperty("code")
    private String code;
}
