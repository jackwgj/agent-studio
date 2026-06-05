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
 * 查询Controller会话的对话列表的返回模型
 */
@ApiModel(description = "查询Controller会话的对话列表的返回模型")
@Validated
public class ListControllerExecutionsResp {
    @JsonProperty("count")
    private Integer count = null;

    @JsonProperty("executions")
    @Valid
    @Size()
    private List<ControllerExecution> executions = null;

    public Integer getCount() {
        return count;
    }

    public ListControllerExecutionsResp setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<ControllerExecution> getExecutions() {
        return executions;
    }

    public ListControllerExecutionsResp setExecutions(List<ControllerExecution> executions) {
        this.executions = executions;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListControllerExecutionsResp {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    executions: ").append(toIndentedString(executions)).append("\n");
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
        ListControllerExecutionsResp listControllerExecutionsResp = (ListControllerExecutionsResp) o;
        return Objects.equals(this.count, listControllerExecutionsResp.count) && Objects.equals(this.executions,
            listControllerExecutionsResp.executions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, executions);
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
