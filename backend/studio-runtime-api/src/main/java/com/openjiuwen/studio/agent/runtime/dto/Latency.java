/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 耗时时间统计
 */
@ApiModel(description = "耗时时间统计")

@Validated

public class Latency implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("overall")
    private Double overall = null;

    @JsonProperty("model")
    private Double model = null;

    @JsonProperty("plugin")
    private Double plugin = null;

    public Double getOverall() {
        return overall;
    }

    public Latency setOverall(Double overall) {
        this.overall = overall;
        return this;
    }

    public Double getModel() {
        return model;
    }

    public Latency setModel(Double model) {
        this.model = model;
        return this;
    }

    public Double getPlugin() {
        return plugin;
    }

    public Latency setPlugin(Double plugin) {
        this.plugin = plugin;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Latency {\n");

        sb.append("    overall: ").append(toIndentedString(overall)).append("\n");
        sb.append("    model: ").append(toIndentedString(model)).append("\n");
        sb.append("    plugin: ").append(toIndentedString(plugin)).append("\n");
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
        Latency latency = (Latency) o;
        return Objects.equals(this.overall, latency.overall) && Objects.equals(this.model, latency.model)
            && Objects.equals(this.plugin, latency.plugin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(overall, model, plugin);
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
