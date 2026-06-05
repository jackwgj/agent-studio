/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能描述
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveMessages {

    private List<SensitiveMessage> message;

    private String receipt;
}
