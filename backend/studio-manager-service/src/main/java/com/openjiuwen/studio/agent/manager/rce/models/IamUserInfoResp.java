/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 查询IAM用户详情
 *
 */
@Data
@Accessors(chain = true)
public class IamUserInfoResp {
    @JsonProperty("user")
    private IamUserInfo2 user;
}
