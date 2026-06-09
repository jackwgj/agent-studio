/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * UploadSessionDataReq
 */

@Validated

public class UploadSessionDataReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("session_info_list")
    @Valid
    @NotNull
    @Size(max = 100)
    private List<SessionInfo> sessionInfoList = new ArrayList<SessionInfo>();

    public List<SessionInfo> getSessionInfoList() {
        return sessionInfoList;
    }

    public UploadSessionDataReq setSessionInfoList(List<SessionInfo> sessionInfoList) {
        this.sessionInfoList = sessionInfoList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UploadSessionDataReq {\n");

        sb.append("    sessionInfoList: ").append(toIndentedString(sessionInfoList)).append("\n");
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
        UploadSessionDataReq uploadSessionDataReq = (UploadSessionDataReq) o;
        return Objects.equals(this.sessionInfoList, uploadSessionDataReq.sessionInfoList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionInfoList);
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
