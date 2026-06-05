/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 数据查询节点config对象。
 */
@ApiModel(description = "数据查询节点config对象。")

@Validated

public class WorkflowDataQueryConfigVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("logic")
    @NotNull
    private LogicEnum logic = null;

    @JsonProperty("conditions")
    @Valid
    @Size()
    private List<WorkflowBranchConfigVOConditions> conditions = null;

    @JsonProperty("limit")
    private Integer limit = null;

    @JsonProperty("offset")
    private Integer offset = null;

    @JsonProperty("orders")
    @Valid
    @Size()
    private List<WorkflowDataQueryConfigVOOrders> orders = null;

    public LogicEnum getLogic() {
        return logic;
    }

    public WorkflowDataQueryConfigVO setLogic(LogicEnum logic) {
        this.logic = logic;
        return this;
    }

    public List<WorkflowBranchConfigVOConditions> getConditions() {
        return conditions;
    }

    public WorkflowDataQueryConfigVO setConditions(List<WorkflowBranchConfigVOConditions> conditions) {
        this.conditions = conditions;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public WorkflowDataQueryConfigVO setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public WorkflowDataQueryConfigVO setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public List<WorkflowDataQueryConfigVOOrders> getOrders() {
        return orders;
    }

    public WorkflowDataQueryConfigVO setOrders(List<WorkflowDataQueryConfigVOOrders> orders) {
        this.orders = orders;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowDataQueryConfigVO {\n");

        sb.append("    logic: ").append(toIndentedString(logic)).append("\n");
        sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    orders: ").append(toIndentedString(orders)).append("\n");
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
        WorkflowDataQueryConfigVO workflowDataQueryConfigVO = (WorkflowDataQueryConfigVO) o;
        return Objects.equals(this.logic, workflowDataQueryConfigVO.logic) && Objects.equals(this.conditions,
            workflowDataQueryConfigVO.conditions) && Objects.equals(this.limit, workflowDataQueryConfigVO.limit)
            && Objects.equals(this.offset, workflowDataQueryConfigVO.offset) && Objects.equals(this.orders,
            workflowDataQueryConfigVO.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logic, conditions, limit, offset, orders);
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

    /**
     * 逻辑操作符。
     */
    public enum LogicEnum {
        AND("AND"),

        OR("OR");

        private String value;

        LogicEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static LogicEnum fromValue(String text) {
            for (LogicEnum b : LogicEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
