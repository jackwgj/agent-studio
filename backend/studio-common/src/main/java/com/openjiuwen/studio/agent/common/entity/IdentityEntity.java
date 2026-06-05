/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 功能描述
 *
 */
@Data
public class IdentityEntity {
    private List<String> methods;

    @JsonProperty("assume_role")
    private AssumeRoleEntity assumeRole;
}
