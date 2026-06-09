/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

/**
 * 对话query列表
 */
@ApiModel(description = "对话query列表")
@Validated
public class AgentExecutionQueries {
    @JsonProperty("count")
    private Integer count = null;

    @JsonProperty("execution_queries")
    @Valid
    @Size()
    private List<ExecutionQuery> executionQueries = null;

    public Integer getCount() {
        return count;
    }

    public AgentExecutionQueries setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<ExecutionQuery> getExecutionQueries() {
        return executionQueries;
    }

    public AgentExecutionQueries setExecutionQueries(List<ExecutionQuery> executionQueries) {
        this.executionQueries = executionQueries;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AgentExecutionQueries {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    executionQueries: ").append(toIndentedString(executionQueries)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentExecutionQueries agentExecutionQueries = (AgentExecutionQueries) o;
        return Objects.equals(this.count, agentExecutionQueries.count) && Objects.equals(this.executionQueries,
            agentExecutionQueries.executionQueries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, executionQueries);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
