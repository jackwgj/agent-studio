/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Objects;

/**
 * ResumeTaskReq
 */
@Validated
public class ResumeTaskReq {
    @JsonProperty("inputs")
    @Valid
    @Size()
    private Map<String, Object> inputs = null;

    @JsonProperty("timeout")
    private Integer timeout = null;

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public ResumeTaskReq setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public ResumeTaskReq setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResumeTaskReq {\n");

        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    timeout: ").append(toIndentedString(timeout)).append("\n");
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
        ResumeTaskReq resumeTaskReq = (ResumeTaskReq) o;
        return Objects.equals(this.inputs, resumeTaskReq.inputs) && Objects.equals(this.timeout, resumeTaskReq.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs, timeout);
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
