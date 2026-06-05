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

import java.util.List;

/**
 * @description iam认证数据结构模型
 * @since 2025/8/23
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IamIdentity {
    @JsonProperty("methods")
    @JSONField(name = "methods")
    private List<String> methods;

    @JsonProperty("password")
    @JSONField(name = "password")
    private IamPassword password;
}
