/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SchemaConfig配置类型
 *
 */
@Data
public class RailsConfig {
    @Data
    public static class ActionConfig {
        private String action;

        @JsonProperty("action_extra_args")
        private Map<String, Object> actionExtraArgs;
    }

    private Map<String, Object> rails;

    @JsonProperty("actions_config")
    private List<ActionConfig> actionsConfig;
}
