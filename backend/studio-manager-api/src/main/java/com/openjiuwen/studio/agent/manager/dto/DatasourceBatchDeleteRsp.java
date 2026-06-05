/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 删除数据源响应体
 */
@ApiModel(description = "删除数据源响应体")

@Validated

public class DatasourceBatchDeleteRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    @NotNull
    private Long count = null;

    @JsonProperty("ids")
    @Valid
    @Size(min = 1, max = 100)
    private List<@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Length(min = 1, max = 64) String> ids = null;

    public Long getCount() {
        return count;
    }

    public DatasourceBatchDeleteRsp setCount(Long count) {
        this.count = count;
        return this;
    }

    public List<String> getIds() {
        return ids;
    }

    public DatasourceBatchDeleteRsp setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceBatchDeleteRsp {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    ids: ").append(toIndentedString(ids)).append("\n");
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
        DatasourceBatchDeleteRsp datasourceBatchDeleteRsp = (DatasourceBatchDeleteRsp) o;
        return Objects.equals(this.count, datasourceBatchDeleteRsp.count) && Objects.equals(this.ids,
            datasourceBatchDeleteRsp.ids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, ids);
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
