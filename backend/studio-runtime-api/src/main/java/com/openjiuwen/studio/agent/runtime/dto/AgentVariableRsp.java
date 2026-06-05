/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 查询变量详情
 */
@ApiModel(description = "查询变量详情")

@Validated

public class AgentVariableRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("status")
    @Length(max = 64)
    private String status = null;

    @JsonProperty("data")
    @Valid
    @Size()
    private List<VariableInfo> data = null;

    public String getStatus() {
        return status;
    }

    public AgentVariableRsp setStatus(String status) {
        this.status = status;
        return this;
    }

    public List<VariableInfo> getData() {
        return data;
    }

    public AgentVariableRsp setData(List<VariableInfo> data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AgentVariableRsp {\n");

        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
        AgentVariableRsp agentVariableRsp = (AgentVariableRsp) o;
        return Objects.equals(this.status, agentVariableRsp.status) && Objects.equals(this.data, agentVariableRsp.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, data);
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
