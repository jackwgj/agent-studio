/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 插件参数
 */
@ApiModel(description = "插件参数")
@Validated
public class PluginConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("plugin_id")
    private String pluginId = null;

    @JsonProperty("config")
    @Valid
    @Size()
    private Map<String, Object> config = null;

    public String getPluginId() {
        return pluginId;
    }

    public PluginConfig setPluginId(String pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public PluginConfig setConfig(Map<String, Object> config) {
        this.config = config;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PluginConfig {\n");

        sb.append("    pluginId: ").append(toIndentedString(pluginId)).append("\n");
        sb.append("    config: ").append(toIndentedString(config)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PluginConfig pluginConfig = (PluginConfig) o;
        return Objects.equals(this.pluginId, pluginConfig.pluginId) && Objects.equals(this.config, pluginConfig.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, config);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}