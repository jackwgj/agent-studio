/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * LongTermMemoryRuntime
 */

@Validated

public class LongTermMemoryRuntime implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memory_repo_id")
    @Length(max = 64)
    private String memoryRepoId = null;

    @JsonProperty("enable_retrieve")
    private Boolean enableRetrieve = false;

    @JsonProperty("enable_extract")
    private Boolean enableExtract = false;

    public String getMemoryRepoId() {
        return memoryRepoId;
    }

    public LongTermMemoryRuntime setMemoryRepoId(String memoryRepoId) {
        this.memoryRepoId = memoryRepoId;
        return this;
    }

    public LongTermMemoryRuntime setEnableRetrieve(Boolean enableRetrieve) {
        this.enableRetrieve = enableRetrieve;
        return this;
    }

    public Boolean isEnableRetrieve() {
        return enableRetrieve;
    }

    public LongTermMemoryRuntime setEnableExtract(Boolean enableExtract) {
        this.enableExtract = enableExtract;
        return this;
    }

    public Boolean isEnableExtract() {
        return enableExtract;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LongTermMemoryRuntime {\n");

        sb.append("    memoryRepoId: ").append(toIndentedString(memoryRepoId)).append("\n");
        sb.append("    enableRetrieve: ").append(toIndentedString(enableRetrieve)).append("\n");
        sb.append("    enableExtract: ").append(toIndentedString(enableExtract)).append("\n");
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
        LongTermMemoryRuntime longTermMemoryRuntime = (LongTermMemoryRuntime) o;
        return Objects.equals(this.memoryRepoId, longTermMemoryRuntime.memoryRepoId) && Objects.equals(
            this.enableRetrieve, longTermMemoryRuntime.enableRetrieve) && Objects.equals(this.enableExtract,
            longTermMemoryRuntime.enableExtract);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryRepoId, enableRetrieve, enableExtract);
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
