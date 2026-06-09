/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * skill操作卡片配置
 */
@ApiModel(description = "skill操作卡片配置")

@Validated

public class JiuwenParamsSysOperationCard implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("mode")
    private String mode = null;

    @JsonProperty("workConfig")
    @Valid
    private JiuwenParamsSysOperationCardWorkConfig workConfig = null;

    public String getId() {
        return id;
    }

    public JiuwenParamsSysOperationCard setId(String id) {
        this.id = id;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public JiuwenParamsSysOperationCard setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public JiuwenParamsSysOperationCardWorkConfig getWorkConfig() {
        return workConfig;
    }

    public JiuwenParamsSysOperationCard setWorkConfig(JiuwenParamsSysOperationCardWorkConfig workConfig) {
        this.workConfig = workConfig;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenParamsSysOperationCard {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
        sb.append("    workConfig: ").append(toIndentedString(workConfig)).append("\n");
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
        JiuwenParamsSysOperationCard jiuwenParamsSysOperationCard = (JiuwenParamsSysOperationCard) o;
        return Objects.equals(this.id, jiuwenParamsSysOperationCard.id) && Objects.equals(this.mode,
            jiuwenParamsSysOperationCard.mode) && Objects.equals(this.workConfig,
            jiuwenParamsSysOperationCard.workConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mode, workConfig);
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
