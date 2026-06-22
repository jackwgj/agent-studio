/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 插件信息
 */
@ApiModel(description = "插件信息")

@Validated

public class FunctionCall implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("arguments")
    private String arguments = "agent";

    @JsonProperty("is_mcp")
    private Boolean isMcp = false;

    @JsonProperty("server_name")
    private String serverName = null;

    public String getName() {
        return name;
    }

    public FunctionCall setName(String name) {
        this.name = name;
        return this;
    }

    public String getArguments() {
        return arguments;
    }

    public FunctionCall setArguments(String arguments) {
        this.arguments = arguments;
        return this;
    }

    public FunctionCall setIsMcp(Boolean isMcp) {
        this.isMcp = isMcp;
        return this;
    }

    public Boolean isIsMcp() {
        return isMcp;
    }

    public String getServerName() {
        return serverName;
    }

    public FunctionCall setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FunctionCall {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    arguments: ").append(toIndentedString(arguments)).append("\n");
        sb.append("    isMcp: ").append(toIndentedString(isMcp)).append("\n");
        sb.append("    serverName: ").append(toIndentedString(serverName)).append("\n");
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
        FunctionCall functionCall = (FunctionCall) o;
        return Objects.equals(this.name, functionCall.name) && Objects.equals(this.arguments, functionCall.arguments)
                && Objects.equals(this.isMcp, functionCall.isMcp) && Objects.equals(this.serverName,
                functionCall.serverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arguments, isMcp, serverName);
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
