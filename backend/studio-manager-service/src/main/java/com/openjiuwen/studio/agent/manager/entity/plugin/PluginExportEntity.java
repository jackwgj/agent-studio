/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PluginExportEntity {
    private PluginEntity metadata;

    @JsonProperty("import_type")
    private String importType;
}
