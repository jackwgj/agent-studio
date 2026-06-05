/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ResolveAudioFileRsp
 */

@Validated

public class ResolveAudioFileRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("data")
    @Valid
    @Size()
    private List<ResolveAudioFileInfo> data = null;

    public List<ResolveAudioFileInfo> getData() {
        return data;
    }

    public ResolveAudioFileRsp setData(List<ResolveAudioFileInfo> data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResolveAudioFileRsp {\n");

        sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
        ResolveAudioFileRsp resolveAudioFileRsp = (ResolveAudioFileRsp) o;
        return Objects.equals(this.data, resolveAudioFileRsp.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
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
