/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 运行执行一个工具时需要的参数。
 */
@ApiModel(description = "运行执行一个工具时需要的参数。")

@Validated

public class RunToolRequestBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("parameter")
    @Length(max = 10000000)
    private String parameter = null;

    @JsonProperty("tool_obs_key")
    private String toolObsKey = null;

    public String getParameter() {
        return parameter;
    }

    public RunToolRequestBody setParameter(String parameter) {
        this.parameter = parameter;
        return this;
    }

    public String getToolObsKey() {
        return toolObsKey;
    }

    public RunToolRequestBody setToolObsKey(String toolObsKey) {
        this.toolObsKey = toolObsKey;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RunToolRequestBody {\n");

        sb.append("    parameter: ").append(toIndentedString(parameter)).append("\n");
        sb.append("    toolObsKey: ").append(toIndentedString(toolObsKey)).append("\n");
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
        RunToolRequestBody runToolRequestBody = (RunToolRequestBody) o;
        return Objects.equals(this.parameter, runToolRequestBody.parameter) && Objects.equals(this.toolObsKey,
            runToolRequestBody.toolObsKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter, toolObsKey);
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
