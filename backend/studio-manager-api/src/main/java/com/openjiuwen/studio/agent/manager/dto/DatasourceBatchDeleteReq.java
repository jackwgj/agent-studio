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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 删除数据源响应体
 */
@ApiModel(description = "删除数据源响应体")

@Validated

public class DatasourceBatchDeleteReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("ids")
    @Valid
    @NotNull
    @Size(min = 1, max = 100)
    private List<@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Length(min = 1, max = 64) String> ids = new ArrayList<String>();

    public List<String> getIds() {
        return ids;
    }

    public DatasourceBatchDeleteReq setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceBatchDeleteReq {\n");

        sb.append("    ids: ").append(toIndentedString(ids)).append("\n");
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
        DatasourceBatchDeleteReq datasourceBatchDeleteReq = (DatasourceBatchDeleteReq) o;
        return Objects.equals(this.ids, datasourceBatchDeleteReq.ids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ids);
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
