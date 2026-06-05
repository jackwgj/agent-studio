/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
public class IamDomain {
    @JsonProperty("name")
    @JSONField(name = "name")
    private String name;
}
