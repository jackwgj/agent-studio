/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 用户画像的tag信息。
 */
@ApiModel(description = "用户画像的tag信息。")

@Validated

public class MemoryUserProfileTagInfoIR implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("updatePolicy")
    private UpdatePolicyEnum updatePolicy = UpdatePolicyEnum.OVERWRITE;

    public String getName() {
        return name;
    }

    public MemoryUserProfileTagInfoIR setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public MemoryUserProfileTagInfoIR setDescription(String description) {
        this.description = description;
        return this;
    }

    public UpdatePolicyEnum getUpdatePolicy() {
        return updatePolicy;
    }

    public MemoryUserProfileTagInfoIR setUpdatePolicy(UpdatePolicyEnum updatePolicy) {
        this.updatePolicy = updatePolicy;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MemoryUserProfileTagInfoIR {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    updatePolicy: ").append(toIndentedString(updatePolicy)).append("\n");
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
        MemoryUserProfileTagInfoIR memoryUserProfileTagInfoIR = (MemoryUserProfileTagInfoIR) o;
        return Objects.equals(this.name, memoryUserProfileTagInfoIR.name) && Objects.equals(this.description,
            memoryUserProfileTagInfoIR.description) && Objects.equals(this.updatePolicy,
            memoryUserProfileTagInfoIR.updatePolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, updatePolicy);
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

    /**
     * 更新策略，APPEND-追加，OVERWRITE-覆写。
     */
    public enum UpdatePolicyEnum {
        APPEND("APPEND"),

        OVERWRITE("OVERWRITE");

        private String value;

        UpdatePolicyEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static UpdatePolicyEnum fromValue(String text) {
            for (UpdatePolicyEnum b : UpdatePolicyEnum.values()) {
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
