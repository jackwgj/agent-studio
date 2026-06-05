/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.Data;

@Data
public class PluginContent {

    @JSONField(name = "result")
    private PluginResult result;
}
