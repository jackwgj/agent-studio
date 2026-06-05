/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * RetrieveConversationQo: converted from multi query params
 */
@ApiModel(description = "RetrieveConversationQo: converted from multi query params")
@Validated
public class RetrieveConversationQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("version_id")
    @Length(max = 32)
    private String versionId = null;

    public String getVersionId() {
        return versionId;
    }

    public RetrieveConversationQo setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RetrieveConversationQo {\n");

        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
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
        RetrieveConversationQo retrieveConversationQo = (RetrieveConversationQo) o;
        return Objects.equals(this.versionId, retrieveConversationQo.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionId);
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