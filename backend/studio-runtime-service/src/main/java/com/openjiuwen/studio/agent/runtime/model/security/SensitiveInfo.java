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
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveInfo {

    private String dmqMsgKey;

    private String topic;

    private String taskID;

    private List<SensitiveResult> result;
}
