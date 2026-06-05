/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * TokenCredential
 *
 */
@Data
@Builder
@ToString(exclude = {"id"})
public class TokenCredential {
    private String id;
}
