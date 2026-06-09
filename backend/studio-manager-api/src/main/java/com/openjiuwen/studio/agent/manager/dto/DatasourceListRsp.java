/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 数据源列表接口
 */
@ApiModel(description = "数据源列表接口")

@Validated

public class DatasourceListRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    @NotNull
    private Long count = null;

    @JsonProperty("datasource_list")
    @Valid
    @Size()
    private List<DatasourceBriefRsp> datasourceList = null;

    public Long getCount() {
        return count;
    }

    public DatasourceListRsp setCount(Long count) {
        this.count = count;
        return this;
    }

    public List<DatasourceBriefRsp> getDatasourceList() {
        return datasourceList;
    }

    public DatasourceListRsp setDatasourceList(List<DatasourceBriefRsp> datasourceList) {
        this.datasourceList = datasourceList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceListRsp {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    datasourceList: ").append(toIndentedString(datasourceList)).append("\n");
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
        DatasourceListRsp datasourceListRsp = (DatasourceListRsp) o;
        return Objects.equals(this.count, datasourceListRsp.count) && Objects.equals(this.datasourceList,
            datasourceListRsp.datasourceList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, datasourceList);
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
