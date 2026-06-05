/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 数据源执行响应
 */
@ApiModel(description = "数据源执行响应")

@Validated

public class DatasourceExecuteResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("output_list")
    @Valid
    @NotNull
    @Size()
    private List<Object> outputList = new ArrayList<Object>();

    @JsonProperty("row_num")
    @NotNull
    private Integer rowNum = null;

    public List<Object> getOutputList() {
        return outputList;
    }

    public DatasourceExecuteResp setOutputList(List<Object> outputList) {
        this.outputList = outputList;
        return this;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public DatasourceExecuteResp setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceExecuteResp {\n");

        sb.append("    outputList: ").append(toIndentedString(outputList)).append("\n");
        sb.append("    rowNum: ").append(toIndentedString(rowNum)).append("\n");
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
        DatasourceExecuteResp datasourceExecuteResp = (DatasourceExecuteResp) o;
        return Objects.equals(this.outputList, datasourceExecuteResp.outputList) && Objects.equals(this.rowNum,
            datasourceExecuteResp.rowNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputList, rowNum);
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
