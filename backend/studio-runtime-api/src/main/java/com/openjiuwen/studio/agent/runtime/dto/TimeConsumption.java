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
 * 知识型Agent执行耗时，秒
 */
@ApiModel(description = "知识型Agent执行耗时，秒")

@Validated

public class TimeConsumption implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("total_latency")
    private Double totalLatency = null;

    @JsonProperty("plugin_latency")
    private Double pluginLatency = null;

    @JsonProperty("model_latency")
    private Double modelLatency = null;

    @JsonProperty("wait_latency")
    private Double waitLatency = null;

    @JsonProperty("overall_latency")
    private Double overallLatency = null;

    public Double getTotalLatency() {
        return totalLatency;
    }

    public TimeConsumption setTotalLatency(Double totalLatency) {
        this.totalLatency = totalLatency;
        return this;
    }

    public Double getPluginLatency() {
        return pluginLatency;
    }

    public TimeConsumption setPluginLatency(Double pluginLatency) {
        this.pluginLatency = pluginLatency;
        return this;
    }

    public Double getModelLatency() {
        return modelLatency;
    }

    public TimeConsumption setModelLatency(Double modelLatency) {
        this.modelLatency = modelLatency;
        return this;
    }

    public Double getWaitLatency() {
        return waitLatency;
    }

    public TimeConsumption setWaitLatency(Double waitLatency) {
        this.waitLatency = waitLatency;
        return this;
    }

    public Double getOverallLatency() {
        return overallLatency;
    }

    public TimeConsumption setOverallLatency(Double overallLatency) {
        this.overallLatency = overallLatency;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TimeConsumption {\n");

        sb.append("    totalLatency: ").append(toIndentedString(totalLatency)).append("\n");
        sb.append("    pluginLatency: ").append(toIndentedString(pluginLatency)).append("\n");
        sb.append("    modelLatency: ").append(toIndentedString(modelLatency)).append("\n");
        sb.append("    waitLatency: ").append(toIndentedString(waitLatency)).append("\n");
        sb.append("    overallLatency: ").append(toIndentedString(overallLatency)).append("\n");
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
        TimeConsumption timeConsumption = (TimeConsumption) o;
        return Objects.equals(this.totalLatency, timeConsumption.totalLatency) && Objects.equals(this.pluginLatency,
            timeConsumption.pluginLatency) && Objects.equals(this.modelLatency, timeConsumption.modelLatency)
            && Objects.equals(this.waitLatency, timeConsumption.waitLatency) && Objects.equals(this.overallLatency,
            timeConsumption.overallLatency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalLatency, pluginLatency, modelLatency, waitLatency, overallLatency);
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
