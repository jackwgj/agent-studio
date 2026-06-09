/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * workflow应用列表响应体
 */
@ApiModel(description = "workflow应用列表响应体")
@Validated
public class WorkflowListRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    private Long count = null;

    @JsonProperty("workflow_list")
    @Valid
    @Size()
    private List<WorkflowListItem> workflowList = null;

    public Long getCount() {
        return count;
    }

    public WorkflowListRsp setCount(Long count) {
        this.count = count;
        return this;
    }

    public List<WorkflowListItem> getWorkflowList() {
        return workflowList;
    }

    public WorkflowListRsp setWorkflowList(List<WorkflowListItem> workflowList) {
        this.workflowList = workflowList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowListRsp {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    workflowList: ").append(toIndentedString(workflowList)).append("\n");
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
        WorkflowListRsp workflowListRsp = (WorkflowListRsp) o;
        return Objects.equals(this.count, workflowListRsp.count) && Objects.equals(this.workflowList,
            workflowListRsp.workflowList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, workflowList);
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