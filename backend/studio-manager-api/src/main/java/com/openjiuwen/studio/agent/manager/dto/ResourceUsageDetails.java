/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 资源使用详情
 */
@ApiModel(description = "资源使用详情")

@Validated

public class ResourceUsageDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("isExceedLimit")
    private Boolean isExceedLimit = true;

    @JsonProperty("usedLimit")
    private Integer usedLimit = null;

    @JsonProperty("totalAmount")
    private Integer totalAmount = null;

    public String getType() {
        return type;
    }

    public ResourceUsageDetails setType(String type) {
        this.type = type;
        return this;
    }

    public ResourceUsageDetails setIsExceedLimit(Boolean isExceedLimit) {
        this.isExceedLimit = isExceedLimit;
        return this;
    }

    public Boolean isIsExceedLimit() {
        return isExceedLimit;
    }

    public Integer getUsedLimit() {
        return usedLimit;
    }

    public ResourceUsageDetails setUsedLimit(Integer usedLimit) {
        this.usedLimit = usedLimit;
        return this;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public ResourceUsageDetails setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResourceUsageDetails {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    isExceedLimit: ").append(toIndentedString(isExceedLimit)).append("\n");
        sb.append("    usedLimit: ").append(toIndentedString(usedLimit)).append("\n");
        sb.append("    totalAmount: ").append(toIndentedString(totalAmount)).append("\n");
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
        ResourceUsageDetails resourceUsageDetails = (ResourceUsageDetails) o;
        return Objects.equals(this.type, resourceUsageDetails.type) && Objects.equals(this.isExceedLimit,
            resourceUsageDetails.isExceedLimit) && Objects.equals(this.usedLimit, resourceUsageDetails.usedLimit)
            && Objects.equals(this.totalAmount, resourceUsageDetails.totalAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, isExceedLimit, usedLimit, totalAmount);
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
