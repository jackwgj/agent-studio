/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * McpDeployResult
 */

@Validated

public class McpDeployResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("originId")
    private String originId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("status")
    private StatusEnum status = null;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("errorCode")
    private String errorCode = null;

    @JsonProperty("errorMessage")
    private String errorMessage = null;

    public String getOriginId() {
        return originId;
    }

    public McpDeployResult setOriginId(String originId) {
        this.originId = originId;
        return this;
    }

    public String getName() {
        return name;
    }

    public McpDeployResult setName(String name) {
        this.name = name;
        return this;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public McpDeployResult setStatus(StatusEnum status) {
        this.status = status;
        return this;
    }

    public String getId() {
        return id;
    }

    public McpDeployResult setId(String id) {
        this.id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public McpDeployResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public McpDeployResult setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public McpDeployResult setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpDeployResult {\n");

        sb.append("    originId: ").append(toIndentedString(originId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
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
        McpDeployResult mcpDeployResult = (McpDeployResult) o;
        return Objects.equals(this.originId, mcpDeployResult.originId) && Objects.equals(this.name,
            mcpDeployResult.name) && Objects.equals(this.status, mcpDeployResult.status) && Objects.equals(this.id,
            mcpDeployResult.id) && Objects.equals(this.message, mcpDeployResult.message) && Objects.equals(
            this.errorCode, mcpDeployResult.errorCode) && Objects.equals(this.errorMessage,
            mcpDeployResult.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originId, name, status, id, message, errorCode, errorMessage);
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

    /**
     * 导入状态
     */
    public enum StatusEnum {
        SUCCESS("SUCCESS"),

        FAILED("FAILED");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static StatusEnum fromValue(String text) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
