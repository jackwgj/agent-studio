/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * Datapoints
 */

@Validated

public class Datapoints implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("unit")
    private String unit = null;

    @JsonProperty("average")
    private Double average = null;

    @JsonProperty("max")
    private Double max = null;

    @JsonProperty("min")
    private Double min = null;

    @JsonProperty("sum")
    private Double sum = null;

    @JsonProperty("variance")
    private Double variance = null;

    @JsonProperty("timestamp")
    private Long timestamp = null;

    public String getUnit() {
        return unit;
    }

    public Datapoints setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public Double getAverage() {
        return average;
    }

    public Datapoints setAverage(Double average) {
        this.average = average;
        return this;
    }

    public Double getMax() {
        return max;
    }

    public Datapoints setMax(Double max) {
        this.max = max;
        return this;
    }

    public Double getMin() {
        return min;
    }

    public Datapoints setMin(Double min) {
        this.min = min;
        return this;
    }

    public Double getSum() {
        return sum;
    }

    public Datapoints setSum(Double sum) {
        this.sum = sum;
        return this;
    }

    public Double getVariance() {
        return variance;
    }

    public Datapoints setVariance(Double variance) {
        this.variance = variance;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Datapoints setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Datapoints {\n");

        sb.append("    unit: ").append(toIndentedString(unit)).append("\n");
        sb.append("    average: ").append(toIndentedString(average)).append("\n");
        sb.append("    max: ").append(toIndentedString(max)).append("\n");
        sb.append("    min: ").append(toIndentedString(min)).append("\n");
        sb.append("    sum: ").append(toIndentedString(sum)).append("\n");
        sb.append("    variance: ").append(toIndentedString(variance)).append("\n");
        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
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
        Datapoints datapoints = (Datapoints) o;
        return Objects.equals(this.unit, datapoints.unit) && Objects.equals(this.average, datapoints.average)
            && Objects.equals(this.max, datapoints.max) && Objects.equals(this.min, datapoints.min) && Objects.equals(
            this.sum, datapoints.sum) && Objects.equals(this.variance, datapoints.variance) && Objects.equals(
            this.timestamp, datapoints.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, average, max, min, sum, variance, timestamp);
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
