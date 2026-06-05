/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.run.AuthInfo;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * mcp 运行态请求体
 */
@ApiModel(description = "mcp 运行态请求体")
@Validated
public class McpValidationReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("url")
    @NotBlank
    @Length(max = 256)
    private String url = null;

    @JsonProperty("auth")
    @Valid
    private AuthInfo auth = null;

    public String getUrl() {
        return url;
    }

    public McpValidationReq setUrl(String url) {
        this.url = url;
        return this;
    }

    public AuthInfo getAuth() {
        return auth;
    }

    public McpValidationReq setAuth(AuthInfo auth) {
        this.auth = auth;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpValidationReq {\n");

        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    auth: ").append(toIndentedString(auth)).append("\n");
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
        McpValidationReq mcpValidationReq = (McpValidationReq) o;
        return Objects.equals(this.url, mcpValidationReq.url) && Objects.equals(this.auth, mcpValidationReq.auth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, auth);
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