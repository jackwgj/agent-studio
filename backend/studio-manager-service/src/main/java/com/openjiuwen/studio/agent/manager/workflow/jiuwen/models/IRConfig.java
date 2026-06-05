/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * JiuWen ir配置类型
 *
 */
@Data
public class IRConfig {
    String id;

    String description;

    String type;

    boolean required = false;

    boolean template = false;

    boolean blank = false;

    @JsonProperty("list_schema")
    IRConfig listSchema;

    @JsonProperty("object_schema")
    List<IRConfig> objectSchema;
}
