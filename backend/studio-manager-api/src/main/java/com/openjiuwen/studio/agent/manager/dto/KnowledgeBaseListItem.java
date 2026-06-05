/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 知识库列表中的信息
 */
@ApiModel(description = "知识库列表中的信息")

@Validated

public class KnowledgeBaseListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("knowledge_base_id")
    @Length(max = 64)
    private String knowledgeBaseId = null;

    @JsonProperty("icon")
    @Length(max = 1024000)
    private String icon = null;

    @JsonProperty("repo_type")
    private RepoTypeEnum repoType = null;

    @JsonProperty("type")
    private TypeEnum type = null;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @Length(max = 64)
    private String workspaceId = null;

    @JsonProperty("share_scope")
    private ShareScopeEnum shareScope = null;

    @JsonProperty("source")
    @Valid
    private ExternalKnowledgeBaseSource source = null;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("description")
    @Length(max = 100)
    private String description = null;

    @JsonProperty("status")
    private StatusEnum status = null;

    @JsonProperty("created_user_id")
    @Length(max = 100)
    private String createdUserId = null;

    @JsonProperty("created_user_name")
    @Length(max = 100)
    private String createdUserName = null;

    @JsonProperty("create_time")
    @Range(min = 0L, max = 253402214400000L)
    private Long createTime = null;

    @JsonProperty("last_update_user_id")
    @Length(max = 100)
    private String lastUpdateUserId = null;

    @JsonProperty("last_update_user_name")
    @Length(max = 100)
    private String lastUpdateUserName = null;

    @JsonProperty("update_time")
    @Range(min = 0L, max = 253402214400000L)
    private Long updateTime = null;

    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public KnowledgeBaseListItem setKnowledgeBaseId(String knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public KnowledgeBaseListItem setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public RepoTypeEnum getRepoType() {
        return repoType;
    }

    public KnowledgeBaseListItem setRepoType(RepoTypeEnum repoType) {
        this.repoType = repoType;
        return this;
    }

    public TypeEnum getType() {
        return type;
    }

    public KnowledgeBaseListItem setType(TypeEnum type) {
        this.type = type;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public KnowledgeBaseListItem setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public ShareScopeEnum getShareScope() {
        return shareScope;
    }

    public KnowledgeBaseListItem setShareScope(ShareScopeEnum shareScope) {
        this.shareScope = shareScope;
        return this;
    }

    public ExternalKnowledgeBaseSource getSource() {
        return source;
    }

    public KnowledgeBaseListItem setSource(ExternalKnowledgeBaseSource source) {
        this.source = source;
        return this;
    }

    public String getName() {
        return name;
    }

    public KnowledgeBaseListItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public KnowledgeBaseListItem setDescription(String description) {
        this.description = description;
        return this;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public KnowledgeBaseListItem setStatus(StatusEnum status) {
        this.status = status;
        return this;
    }

    public String getCreatedUserId() {
        return createdUserId;
    }

    public KnowledgeBaseListItem setCreatedUserId(String createdUserId) {
        this.createdUserId = createdUserId;
        return this;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public KnowledgeBaseListItem setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
        return this;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public KnowledgeBaseListItem setCreateTime(Long createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getLastUpdateUserId() {
        return lastUpdateUserId;
    }

    public KnowledgeBaseListItem setLastUpdateUserId(String lastUpdateUserId) {
        this.lastUpdateUserId = lastUpdateUserId;
        return this;
    }

    public String getLastUpdateUserName() {
        return lastUpdateUserName;
    }

    public KnowledgeBaseListItem setLastUpdateUserName(String lastUpdateUserName) {
        this.lastUpdateUserName = lastUpdateUserName;
        return this;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public KnowledgeBaseListItem setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class KnowledgeBaseListItem {\n");

        sb.append("    knowledgeBaseId: ").append(toIndentedString(knowledgeBaseId)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    repoType: ").append(toIndentedString(repoType)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    shareScope: ").append(toIndentedString(shareScope)).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    createdUserId: ").append(toIndentedString(createdUserId)).append("\n");
        sb.append("    createdUserName: ").append(toIndentedString(createdUserName)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    lastUpdateUserId: ").append(toIndentedString(lastUpdateUserId)).append("\n");
        sb.append("    lastUpdateUserName: ").append(toIndentedString(lastUpdateUserName)).append("\n");
        sb.append("    updateTime: ").append(toIndentedString(updateTime)).append("\n");
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
        KnowledgeBaseListItem knowledgeBaseListItem = (KnowledgeBaseListItem) o;
        return Objects.equals(this.knowledgeBaseId, knowledgeBaseListItem.knowledgeBaseId) && Objects.equals(this.icon,
            knowledgeBaseListItem.icon) && Objects.equals(this.repoType, knowledgeBaseListItem.repoType)
            && Objects.equals(this.type, knowledgeBaseListItem.type) && Objects.equals(this.workspaceId,
            knowledgeBaseListItem.workspaceId) && Objects.equals(this.shareScope, knowledgeBaseListItem.shareScope)
            && Objects.equals(this.source, knowledgeBaseListItem.source) && Objects.equals(this.name,
            knowledgeBaseListItem.name) && Objects.equals(this.description, knowledgeBaseListItem.description)
            && Objects.equals(this.status, knowledgeBaseListItem.status) && Objects.equals(this.createdUserId,
            knowledgeBaseListItem.createdUserId) && Objects.equals(this.createdUserName,
            knowledgeBaseListItem.createdUserName) && Objects.equals(this.createTime, knowledgeBaseListItem.createTime)
            && Objects.equals(this.lastUpdateUserId, knowledgeBaseListItem.lastUpdateUserId) && Objects.equals(
            this.lastUpdateUserName, knowledgeBaseListItem.lastUpdateUserName) && Objects.equals(this.updateTime,
            knowledgeBaseListItem.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knowledgeBaseId, icon, repoType, type, workspaceId, shareScope, source, name, description,
            status, createdUserId, createdUserName, createTime, lastUpdateUserId, lastUpdateUserName, updateTime);
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
