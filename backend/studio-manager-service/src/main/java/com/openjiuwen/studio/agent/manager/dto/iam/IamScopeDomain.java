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
 * iam认证数据结构模型
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IamScopeDomain {
    @JsonProperty("id")
    @JSONField(name = "id")
    private String id;

    @JsonProperty("name")
    @JSONField(name = "name")
    private String name;
}
