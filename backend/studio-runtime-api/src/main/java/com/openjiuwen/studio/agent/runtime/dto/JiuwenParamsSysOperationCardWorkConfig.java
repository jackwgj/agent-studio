/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

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
 * 工作配置
 */
@ApiModel(description = "工作配置")

@Validated

public class JiuwenParamsSysOperationCardWorkConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workDir")
    private String workDir = null;

    @JsonProperty("shellAllowlist")
    @Valid
    @Size()
    private List<@Length() String> shellAllowlist = null;

    public String getWorkDir() {
        return workDir;
    }

    public JiuwenParamsSysOperationCardWorkConfig setWorkDir(String workDir) {
        this.workDir = workDir;
        return this;
    }

    public List<String> getShellAllowlist() {
        return shellAllowlist;
    }

    public JiuwenParamsSysOperationCardWorkConfig setShellAllowlist(List<String> shellAllowlist) {
        this.shellAllowlist = shellAllowlist;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenParamsSysOperationCardWorkConfig {\n");

        sb.append("    workDir: ").append(toIndentedString(workDir)).append("\n");
        sb.append("    shellAllowlist: ").append(toIndentedString(shellAllowlist)).append("\n");
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
        JiuwenParamsSysOperationCardWorkConfig jiuwenParamsSysOperationCardWorkConfig
            = (JiuwenParamsSysOperationCardWorkConfig) o;
        return Objects.equals(this.workDir, jiuwenParamsSysOperationCardWorkConfig.workDir) && Objects.equals(
            this.shellAllowlist, jiuwenParamsSysOperationCardWorkConfig.shellAllowlist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workDir, shellAllowlist);
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
