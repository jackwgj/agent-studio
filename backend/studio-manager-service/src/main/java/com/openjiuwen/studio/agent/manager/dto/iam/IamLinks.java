/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.iam;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description iamlinks对象
 * @since 2025/9/11
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IamLinks {
    @JsonProperty("self")
    @JSONField(name = "self")
    private String self;

    @JsonProperty("previous")
    @JSONField(name = "previous")
    private String previous;

    @JsonProperty("next")
    @JSONField(name = "next")
    private String next;
}
