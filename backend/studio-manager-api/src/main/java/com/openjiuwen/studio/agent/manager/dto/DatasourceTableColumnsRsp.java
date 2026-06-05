/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 数据源表格字段响应信息
 */
@ApiModel(description = "数据源表格字段响应信息")

@Validated

public class DatasourceTableColumnsRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("field_list")
    @Valid
    @Size()
    private List<DatasourceColumnRsp> fieldList = null;

    public List<DatasourceColumnRsp> getFieldList() {
        return fieldList;
    }

    public DatasourceTableColumnsRsp setFieldList(List<DatasourceColumnRsp> fieldList) {
        this.fieldList = fieldList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceTableColumnsRsp {\n");

        sb.append("    fieldList: ").append(toIndentedString(fieldList)).append("\n");
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
        DatasourceTableColumnsRsp datasourceTableColumnsRsp = (DatasourceTableColumnsRsp) o;
        return Objects.equals(this.fieldList, datasourceTableColumnsRsp.fieldList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldList);
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
