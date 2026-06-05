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
 * Iam token 相关信息
 */
@ApiModel(description = "Iam token 相关信息")

@Validated

public class IamTokenInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("token")
    @Valid
    private IamTokenInfoToken token = null;

    public IamTokenInfoToken getToken() {
        return token;
    }

    public IamTokenInfo setToken(IamTokenInfoToken token) {
        this.token = token;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IamTokenInfo {\n");

        sb.append("    token: ").append(toIndentedString(token)).append("\n");
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
        IamTokenInfo iamTokenInfo = (IamTokenInfo) o;
        return Objects.equals(this.token, iamTokenInfo.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
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
