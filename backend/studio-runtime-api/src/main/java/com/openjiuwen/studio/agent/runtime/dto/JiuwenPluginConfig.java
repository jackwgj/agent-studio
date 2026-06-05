/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * JiuwenPluginConfig
 */

@Validated

public class JiuwenPluginConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("pluginId")
    private String pluginId = null;

    @JsonProperty("config")
    @Valid
    @Size()
    private Map<String, Object> config = null;

    public String getPluginId() {
        return pluginId;
    }

    public JiuwenPluginConfig setPluginId(String pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public JiuwenPluginConfig setConfig(Map<String, Object> config) {
        this.config = config;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenPluginConfig {\n");

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
        JiuwenPluginConfig jiuwenPluginConfig = (JiuwenPluginConfig) o;
        return Objects.equals(this.pluginId, jiuwenPluginConfig.pluginId) && Objects.equals(this.config,
            jiuwenPluginConfig.config);
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
