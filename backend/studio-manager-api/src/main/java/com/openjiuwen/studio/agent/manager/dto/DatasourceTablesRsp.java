/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

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
 * 数据源表格列表响应信息
 */
@ApiModel(description = "数据源表格列表响应信息")

@Validated

public class DatasourceTablesRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    private Integer count = null;

    @JsonProperty("tables")
    @Valid
    @Size()
    private List<@Length() String> tables = null;

    public Integer getCount() {
        return count;
    }

    public DatasourceTablesRsp setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<String> getTables() {
        return tables;
    }

    public DatasourceTablesRsp setTables(List<String> tables) {
        this.tables = tables;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceTablesRsp {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    tables: ").append(toIndentedString(tables)).append("\n");
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
        DatasourceTablesRsp datasourceTablesRsp = (DatasourceTablesRsp) o;
        return Objects.equals(this.count, datasourceTablesRsp.count) && Objects.equals(this.tables,
            datasourceTablesRsp.tables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, tables);
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
