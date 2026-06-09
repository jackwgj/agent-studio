/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * WorkflowDataQueryConfigVOOrders
 */

@Validated

public class WorkflowDataQueryConfigVOOrders implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("field")
    private String field = null;

    @JsonProperty("asc")
    private Boolean asc = false;

    public String getField() {
        return field;
    }

    public WorkflowDataQueryConfigVOOrders setField(String field) {
        this.field = field;
        return this;
    }

    public WorkflowDataQueryConfigVOOrders setAsc(Boolean asc) {
        this.asc = asc;
        return this;
    }

    public Boolean isAsc() {
        return asc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowDataQueryConfigVOOrders {\n");

        sb.append("    field: ").append(toIndentedString(field)).append("\n");
        sb.append("    asc: ").append(toIndentedString(asc)).append("\n");
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
        WorkflowDataQueryConfigVOOrders workflowDataQueryConfigVOOrders = (WorkflowDataQueryConfigVOOrders) o;
        return Objects.equals(this.field, workflowDataQueryConfigVOOrders.field) && Objects.equals(this.asc,
            workflowDataQueryConfigVOOrders.asc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, asc);
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
