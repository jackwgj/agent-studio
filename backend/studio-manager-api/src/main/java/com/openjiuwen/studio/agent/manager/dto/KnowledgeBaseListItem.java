/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

/**
 * 知识库列表中的信息
 */
@ApiModel(description = "知识库列表中的信息")

@Validated
@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KnowledgeBaseListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Length(max = 64)
    private String knowledgeBaseId = null;

    @Length(max = 1024000)
    private String icon = null;

    private RepoTypeEnum repoType = null;

    private TypeEnum type = null;

    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @Length(max = 64)
    private String workspaceId = null;

    private ShareScopeEnum shareScope = null;

    @Valid
    private ExternalKnowledgeBaseSource source = null;

    @Length(max = 64)
    private String name = null;

    @Length(max = 100)
    private String description = null;

    private StatusEnum status = null;

    @Length(max = 100)
    private String createdUserId = null;

    @Length(max = 100)
    private String createdUserName = null;

    @Range(min = 0L, max = 253402214400000L)
    private Long createTime = null;

    @Length(max = 100)
    private String lastUpdateUserId = null;

    @Length(max = 100)
    private String lastUpdateUserName = null;

    @Range(min = 0L, max = 253402214400000L)
    private Long updateTime = null;

    @Length(max = 64)
    private String knowledgeBaseConnectionId = null;

    /**
     * 知识库类型 - share：共享 - exclusive：专享
     */
    public enum RepoTypeEnum {
        SHARE("share"),

        EXCLUSIVE("exclusive");

        private String value;

        RepoTypeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static RepoTypeEnum fromValue(String text) {
            for (RepoTypeEnum b : RepoTypeEnum.values()) {
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

    /**
     * 知识库类型，internal-默认，external-第三方知识库
     */
    public enum TypeEnum {
        INTERNAL("internal"),

        EXTERNAL("external");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
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

    /**
     * 知识库可见范围 - SELF：当前空间下可见。 - GLOBAL：当前租户下共享。 - ALL：包含当前空间下可见与当前租户下共享。
     */
    public enum ShareScopeEnum {
        SELF("SELF"),

        GLOBAL("GLOBAL"),

        ALL("ALL"),

        PARTIAL("PARTIAL");

        private String value;

        ShareScopeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ShareScopeEnum fromValue(String text) {
            for (ShareScopeEnum b : ShareScopeEnum.values()) {
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

    /**
     * 知识库状态，OPEN-启用，CLOSE-停用
     */
    public enum StatusEnum {
        OPEN("OPEN"),

        CLOSE("CLOSE");

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
