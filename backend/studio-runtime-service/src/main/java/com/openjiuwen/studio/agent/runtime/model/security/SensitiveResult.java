/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveResult {

    private String channel;

    private int curNum;

    private String detectChannel;

    private String imageURL;

    private String securityResult;

    private int status;
}
