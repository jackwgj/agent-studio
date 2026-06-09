/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 关联插件对象。
 */
@ApiModel(description = "关联插件对象。")

@Validated

public class ToolReference implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("tool_id")
    private String toolId = null;

    @JsonProperty("plugin_display_name")
    private String pluginDisplayName = null;

    @JsonProperty("plugin_chinese_name")
    private String pluginChineseName = null;

    @JsonProperty("tool_display_name")
    private String toolDisplayName = null;

    @JsonProperty("tool_chinese_name")
    private String toolChineseName = null;

    @JsonProperty("tool_parameter")
    private String toolParameter = null;

    @JsonProperty("tool_icon")
    private String toolIcon = null;

    @JsonProperty("limit")
    private Integer limit = null;

    @JsonProperty("usage")
    private Integer usage = null;

    @JsonProperty("is_free")
    private Integer isFree = null;

    @JsonProperty("credential_status")
    private String credentialStatus = null;

    @JsonProperty("desc")
    @Length(min = 1, max = 600)
    private String desc = null;

    @JsonProperty("auth_info")
    @Valid
    private AuthInfo authInfo = null;

    @JsonProperty("metadata")
    private String metadata = null;

    @JsonProperty("last_version_id")
    private String lastVersionId = null;

    @JsonProperty("last_version_name")
    private String lastVersionName = null;

    public String getToolId() {
        return toolId;
    }

    public ToolReference setToolId(String toolId) {
        this.toolId = toolId;
        return this;
    }

    public String getPluginDisplayName() {
        return pluginDisplayName;
    }

    public ToolReference setPluginDisplayName(String pluginDisplayName) {
        this.pluginDisplayName = pluginDisplayName;
        return this;
    }

    public String getPluginChineseName() {
        return pluginChineseName;
    }

    public ToolReference setPluginChineseName(String pluginChineseName) {
        this.pluginChineseName = pluginChineseName;
        return this;
    }

    public String getToolDisplayName() {
        return toolDisplayName;
    }

    public ToolReference setToolDisplayName(String toolDisplayName) {
        this.toolDisplayName = toolDisplayName;
        return this;
    }

    public String getToolChineseName() {
        return toolChineseName;
    }

    public ToolReference setToolChineseName(String toolChineseName) {
        this.toolChineseName = toolChineseName;
        return this;
    }

    public String getToolParameter() {
        return toolParameter;
    }

    public ToolReference setToolParameter(String toolParameter) {
        this.toolParameter = toolParameter;
        return this;
    }

    public String getToolIcon() {
        return toolIcon;
    }

    public ToolReference setToolIcon(String toolIcon) {
        this.toolIcon = toolIcon;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ToolReference setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getUsage() {
        return usage;
    }

    public ToolReference setUsage(Integer usage) {
        this.usage = usage;
        return this;
    }

    public Integer getIsFree() {
        return isFree;
    }

    public ToolReference setIsFree(Integer isFree) {
        this.isFree = isFree;
        return this;
    }

    public String getCredentialStatus() {
        return credentialStatus;
    }

    public ToolReference setCredentialStatus(String credentialStatus) {
        this.credentialStatus = credentialStatus;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public ToolReference setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public AuthInfo getAuthInfo() {
        return authInfo;
    }

    public ToolReference setAuthInfo(AuthInfo authInfo) {
        this.authInfo = authInfo;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public ToolReference setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getLastVersionId() {
        return lastVersionId;
    }

    public ToolReference setLastVersionId(String lastVersionId) {
        this.lastVersionId = lastVersionId;
        return this;
    }

    public String getLastVersionName() {
        return lastVersionName;
    }

    public ToolReference setLastVersionName(String lastVersionName) {
        this.lastVersionName = lastVersionName;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ToolReference {\n");

        sb.append("    toolId: ").append(toIndentedString(toolId)).append("\n");
        sb.append("    pluginDisplayName: ").append(toIndentedString(pluginDisplayName)).append("\n");
        sb.append("    pluginChineseName: ").append(toIndentedString(pluginChineseName)).append("\n");
        sb.append("    toolDisplayName: ").append(toIndentedString(toolDisplayName)).append("\n");
        sb.append("    toolChineseName: ").append(toIndentedString(toolChineseName)).append("\n");
        sb.append("    toolParameter: ").append(toIndentedString(toolParameter)).append("\n");
        sb.append("    toolIcon: ").append(toIndentedString(toolIcon)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    usage: ").append(toIndentedString(usage)).append("\n");
        sb.append("    isFree: ").append(toIndentedString(isFree)).append("\n");
        sb.append("    credentialStatus: ").append(toIndentedString(credentialStatus)).append("\n");
        sb.append("    desc: ").append(toIndentedString(desc)).append("\n");
        sb.append("    authInfo: ").append(toIndentedString(authInfo)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    lastVersionId: ").append(toIndentedString(lastVersionId)).append("\n");
        sb.append("    lastVersionName: ").append(toIndentedString(lastVersionName)).append("\n");
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
        ToolReference toolReference = (ToolReference) o;
        return Objects.equals(this.toolId, toolReference.toolId) && Objects.equals(this.pluginDisplayName,
            toolReference.pluginDisplayName) && Objects.equals(this.pluginChineseName, toolReference.pluginChineseName)
            && Objects.equals(this.toolDisplayName, toolReference.toolDisplayName) && Objects.equals(
            this.toolChineseName, toolReference.toolChineseName) && Objects.equals(this.toolParameter,
            toolReference.toolParameter) && Objects.equals(this.toolIcon, toolReference.toolIcon) && Objects.equals(
            this.limit, toolReference.limit) && Objects.equals(this.usage, toolReference.usage) && Objects.equals(
            this.isFree, toolReference.isFree) && Objects.equals(this.credentialStatus, toolReference.credentialStatus)
            && Objects.equals(this.desc, toolReference.desc) && Objects.equals(this.authInfo, toolReference.authInfo)
            && Objects.equals(this.metadata, toolReference.metadata) && Objects.equals(this.lastVersionId,
            toolReference.lastVersionId) && Objects.equals(this.lastVersionName, toolReference.lastVersionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolId, pluginDisplayName, pluginChineseName, toolDisplayName, toolChineseName,
            toolParameter, toolIcon, limit, usage, isFree, credentialStatus, desc, authInfo, metadata, lastVersionId,
            lastVersionName);
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
