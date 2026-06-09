/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * ModifyTaskReq
 */
@Validated
public class ModifyTaskReq {
    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("timeout")
    private Integer timeout = null;

    public String getName() {
        return name;
    }

    public ModifyTaskReq setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public ModifyTaskReq setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModifyTaskReq {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
        ModifyTaskReq modifyTaskReq = (ModifyTaskReq) o;
        return Objects.equals(this.name, modifyTaskReq.name) && Objects.equals(this.timeout, modifyTaskReq.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, timeout);
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
