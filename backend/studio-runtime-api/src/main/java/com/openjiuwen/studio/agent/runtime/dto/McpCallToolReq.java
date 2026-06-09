/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.run.AuthInfo;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * mcp 运行态请求体
 */
@ApiModel(description = "mcp 运行态请求体")

@Validated

public class McpCallToolReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("url")
    @Length(max = 256)
    private String url = null;

    @JsonProperty("name")
    @NotBlank
    private String name = null;

    @JsonProperty("params")
    @Valid
    @NotNull
    @Size()
    private Map<String, Object> params = new HashMap<String, Object>();

    @JsonProperty("auth")
    @Valid
    private AuthInfo auth = null;

    public String getUrl() {
        return url;
    }

    public McpCallToolReq setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getName() {
        return name;
    }

    public McpCallToolReq setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public McpCallToolReq setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public AuthInfo getAuth() {
        return auth;
    }

    public McpCallToolReq setAuth(AuthInfo auth) {
        this.auth = auth;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpCallToolReq {\n");

        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    params: ").append(toIndentedString(params)).append("\n");
        sb.append("    auth: ").append(toIndentedString(auth)).append("\n");
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
        McpCallToolReq mcpCallToolReq = (McpCallToolReq) o;
        return Objects.equals(this.url, mcpCallToolReq.url) && Objects.equals(this.name, mcpCallToolReq.name)
            && Objects.equals(this.params, mcpCallToolReq.params) && Objects.equals(this.auth, mcpCallToolReq.auth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, params, auth);
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
