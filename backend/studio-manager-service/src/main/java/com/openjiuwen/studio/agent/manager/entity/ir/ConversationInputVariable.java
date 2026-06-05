/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity.ir;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 *  ir用户输入参数
 *
 */
@Data
public class ConversationInputVariable {

    private String name;

    private String type;

    private String description;

    private boolean required;

    @JsonProperty("default")
    private Object defaultValue;
}
