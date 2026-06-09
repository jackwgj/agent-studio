/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数据源表格字段信息
 */
@ApiModel(description = "数据源表格字段信息")

@Validated

public class DatasourceColumnRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("description")
    private String description = null;

    public String getName() {
        return name;
    }

    public DatasourceColumnRsp setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public DatasourceColumnRsp setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DatasourceColumnRsp setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceColumnRsp {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
        DatasourceColumnRsp datasourceColumnRsp = (DatasourceColumnRsp) o;
        return Objects.equals(this.name, datasourceColumnRsp.name) && Objects.equals(this.type,
            datasourceColumnRsp.type) && Objects.equals(this.description, datasourceColumnRsp.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, description);
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
