/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model.memory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 用户画像标签信息
 *
 */
@Data
public class UserProfileTag {
    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private String value;

    @JsonProperty("topic")
    private String topic;
}
