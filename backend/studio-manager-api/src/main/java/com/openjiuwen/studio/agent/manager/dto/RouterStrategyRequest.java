/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * RouterStrategyRequest
 */

@Validated

public class RouterStrategyRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("strategy_name")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5a-zA-Z0-9_.:/|\\\\ -]{1,35}$")
    @Length(max = 64)
    private String strategyName = null;

    @JsonProperty("strategy_key")
    @Length(max = 128)
    private String strategyKey = null;

    @JsonProperty("strategy_description")
    @Length(max = 4096)
    private String strategyDescription = null;

    @JsonProperty("model_service_list")
    @Valid
    @Size(max = 3)
    private List<ModelServiceRsp> modelServiceList = null;

    @JsonProperty("strategy_timeout")
    @Range(min = 1000L, max = 1000000L)
    private Integer strategyTimeout = null;

    @JsonProperty("strategy_retry_count")
    @Range(min = 0L, max = 100L)
    private Integer strategyRetryCount = null;

    public String getStrategyName() {
        return strategyName;
    }

    public RouterStrategyRequest setStrategyName(String strategyName) {
        this.strategyName = strategyName;
        return this;
    }

    public String getStrategyKey() {
        return strategyKey;
    }

    public RouterStrategyRequest setStrategyKey(String strategyKey) {
        this.strategyKey = strategyKey;
        return this;
    }

    public String getStrategyDescription() {
        return strategyDescription;
    }

    public RouterStrategyRequest setStrategyDescription(String strategyDescription) {
        this.strategyDescription = strategyDescription;
        return this;
    }

    public List<ModelServiceRsp> getModelServiceList() {
        return modelServiceList;
    }

    public RouterStrategyRequest setModelServiceList(List<ModelServiceRsp> modelServiceList) {
        this.modelServiceList = modelServiceList;
        return this;
    }

    public Integer getStrategyTimeout() {
        return strategyTimeout;
    }

    public RouterStrategyRequest setStrategyTimeout(Integer strategyTimeout) {
        this.strategyTimeout = strategyTimeout;
        return this;
    }

    public Integer getStrategyRetryCount() {
        return strategyRetryCount;
    }

    public RouterStrategyRequest setStrategyRetryCount(Integer strategyRetryCount) {
        this.strategyRetryCount = strategyRetryCount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RouterStrategyRequest {\n");

        sb.append("    strategyName: ").append(toIndentedString(strategyName)).append("\n");
        sb.append("    strategyKey: ").append(toIndentedString(strategyKey)).append("\n");
        sb.append("    strategyDescription: ").append(toIndentedString(strategyDescription)).append("\n");
        sb.append("    modelServiceList: ").append(toIndentedString(modelServiceList)).append("\n");
        sb.append("    strategyTimeout: ").append(toIndentedString(strategyTimeout)).append("\n");
        sb.append("    strategyRetryCount: ").append(toIndentedString(strategyRetryCount)).append("\n");
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
        RouterStrategyRequest routerStrategyRequest = (RouterStrategyRequest) o;
        return Objects.equals(this.strategyName, routerStrategyRequest.strategyName) && Objects.equals(this.strategyKey,
            routerStrategyRequest.strategyKey) && Objects.equals(this.strategyDescription,
            routerStrategyRequest.strategyDescription) && Objects.equals(this.modelServiceList,
            routerStrategyRequest.modelServiceList) && Objects.equals(this.strategyTimeout,
            routerStrategyRequest.strategyTimeout) && Objects.equals(this.strategyRetryCount,
            routerStrategyRequest.strategyRetryCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strategyName, strategyKey, strategyDescription, modelServiceList, strategyTimeout,
            strategyRetryCount);
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
