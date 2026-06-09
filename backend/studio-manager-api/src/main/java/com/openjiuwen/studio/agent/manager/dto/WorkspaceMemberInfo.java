/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 团队空间成员描述信息。
 */
@ApiModel(description = "团队空间成员描述信息。")

@Validated

public class WorkspaceMemberInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(min = 1, max = 64)
    private String id = null;

    @JsonProperty("workspaceId")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    @JsonProperty("memberName")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5._-][a-zA-Z0-9\\u4e00-\\u9fa5._\\- ]{0,63}$")
    @Length(min = 1, max = 64)
    private String memberName = null;

    @JsonProperty("memberId")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(min = 1, max = 64)
    private String memberId = null;

    @JsonProperty("memberSource")
    @Length(min = 1, max = 128)
    private String memberSource = null;

    @JsonProperty("domainId")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(min = 1, max = 64)
    private String domainId = null;

    @JsonProperty("role")
    @Length(min = 1, max = 64)
    private String role = null;

    @JsonProperty("createdOn")
    private Date createdOn = null;

    @JsonProperty("updatedOn")
    private Date updatedOn = null;

    @JsonProperty("creator")
    @Length(min = 1, max = 128)
    private String creator = null;

    @JsonProperty("updater")
    @Length(min = 1, max = 128)
    private String updater = null;

    @JsonProperty("creatorId")
    @Length(min = 1, max = 128)
    private String creatorId = null;

    @JsonProperty("updaterId")
    @Length(min = 1, max = 128)
    private String updaterId = null;

    public String getId() {
        return id;
    }

    public WorkspaceMemberInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public WorkspaceMemberInfo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getMemberName() {
        return memberName;
    }

    public WorkspaceMemberInfo setMemberName(String memberName) {
        this.memberName = memberName;
        return this;
    }

    public String getMemberId() {
        return memberId;
    }

    public WorkspaceMemberInfo setMemberId(String memberId) {
        this.memberId = memberId;
        return this;
    }

    public String getMemberSource() {
        return memberSource;
    }

    public WorkspaceMemberInfo setMemberSource(String memberSource) {
        this.memberSource = memberSource;
        return this;
    }

    public String getDomainId() {
        return domainId;
    }

    public WorkspaceMemberInfo setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getRole() {
        return role;
    }

    public WorkspaceMemberInfo setRole(String role) {
        this.role = role;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public WorkspaceMemberInfo setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public WorkspaceMemberInfo setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public WorkspaceMemberInfo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getUpdater() {
        return updater;
    }

    public WorkspaceMemberInfo setUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public WorkspaceMemberInfo setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public String getUpdaterId() {
        return updaterId;
    }

    public WorkspaceMemberInfo setUpdaterId(String updaterId) {
        this.updaterId = updaterId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkspaceMemberInfo {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    memberName: ").append(toIndentedString(memberName)).append("\n");
        sb.append("    memberId: ").append(toIndentedString(memberId)).append("\n");
        sb.append("    memberSource: ").append(toIndentedString(memberSource)).append("\n");
        sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    updater: ").append(toIndentedString(updater)).append("\n");
        sb.append("    creatorId: ").append(toIndentedString(creatorId)).append("\n");
        sb.append("    updaterId: ").append(toIndentedString(updaterId)).append("\n");
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
        WorkspaceMemberInfo workspaceMemberInfo = (WorkspaceMemberInfo) o;
        return Objects.equals(this.id, workspaceMemberInfo.id) && Objects.equals(this.workspaceId,
            workspaceMemberInfo.workspaceId) && Objects.equals(this.memberName, workspaceMemberInfo.memberName)
            && Objects.equals(this.memberId, workspaceMemberInfo.memberId) && Objects.equals(this.memberSource,
            workspaceMemberInfo.memberSource) && Objects.equals(this.domainId, workspaceMemberInfo.domainId)
            && Objects.equals(this.role, workspaceMemberInfo.role) && Objects.equals(this.createdOn,
            workspaceMemberInfo.createdOn) && Objects.equals(this.updatedOn, workspaceMemberInfo.updatedOn)
            && Objects.equals(this.creator, workspaceMemberInfo.creator) && Objects.equals(this.updater,
            workspaceMemberInfo.updater) && Objects.equals(this.creatorId, workspaceMemberInfo.creatorId)
            && Objects.equals(this.updaterId, workspaceMemberInfo.updaterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, workspaceId, memberName, memberId, memberSource, domainId, role, createdOn, updatedOn,
            creator, updater, creatorId, updaterId);
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
