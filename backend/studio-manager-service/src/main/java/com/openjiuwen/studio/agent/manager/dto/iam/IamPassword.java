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
 * @description iam认证数据结构模型
 * @since 2025/8/23
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IamPassword {
    @JsonProperty("user")
    @JSONField(name = "user")
    private IamUser user;
}
