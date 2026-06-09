/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * DependResponsePackageInfo
 */

@Validated

public class DependResponsePackageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("dep_id")
    private String depId = null;

    @JsonProperty("owner")
    private String owner = null;

    @JsonProperty("runtime")
    private String runtime = null;

    @JsonProperty("link")
    private String link = null;

    @JsonProperty("size")
    @Length(max = 32)
    private String size = null;

    @JsonProperty("name")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_\\\\.\\\\-]{0,95}[a-zA-Z0-9]$")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("file_name")
    private String fileName = null;

    @JsonProperty("version")
    private Integer version = null;

    @JsonProperty("version_id")
    private String versionId = null;

    @JsonProperty("scope")
    private ScopeEnum scope = ScopeEnum.ALL;

    @JsonProperty("valid")
    private Integer valid = null;

    public String getId() {
        return id;
    }

    public DependResponsePackageInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getDepId() {
        return depId;
    }

    public DependResponsePackageInfo setDepId(String depId) {
        this.depId = depId;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public DependResponsePackageInfo setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getRuntime() {
        return runtime;
    }

    public DependResponsePackageInfo setRuntime(String runtime) {
        this.runtime = runtime;
        return this;
    }

    public String getLink() {
        return link;
    }

    public DependResponsePackageInfo setLink(String link) {
        this.link = link;
        return this;
    }

    public String getSize() {
        return size;
    }

    public DependResponsePackageInfo setSize(String size) {
        this.size = size;
        return this;
    }

    public String getName() {
        return name;
    }

    public DependResponsePackageInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DependResponsePackageInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public DependResponsePackageInfo setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public DependResponsePackageInfo setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public String getVersionId() {
        return versionId;
    }

    public DependResponsePackageInfo setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public ScopeEnum getScope() {
        return scope;
    }

    public DependResponsePackageInfo setScope(ScopeEnum scope) {
        this.scope = scope;
        return this;
    }

    public Integer getValid() {
        return valid;
    }

    public DependResponsePackageInfo setValid(Integer valid) {
        this.valid = valid;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DependResponsePackageInfo {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    depId: ").append(toIndentedString(depId)).append("\n");
        sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
        sb.append("    runtime: ").append(toIndentedString(runtime)).append("\n");
        sb.append("    link: ").append(toIndentedString(link)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
        sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
        sb.append("    valid: ").append(toIndentedString(valid)).append("\n");
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
        DependResponsePackageInfo dependResponsePackageInfo = (DependResponsePackageInfo) o;
        return Objects.equals(this.id, dependResponsePackageInfo.id) && Objects.equals(this.depId,
            dependResponsePackageInfo.depId) && Objects.equals(this.owner, dependResponsePackageInfo.owner)
            && Objects.equals(this.runtime, dependResponsePackageInfo.runtime) && Objects.equals(this.link,
            dependResponsePackageInfo.link) && Objects.equals(this.size, dependResponsePackageInfo.size)
            && Objects.equals(this.name, dependResponsePackageInfo.name) && Objects.equals(this.description,
            dependResponsePackageInfo.description) && Objects.equals(this.fileName, dependResponsePackageInfo.fileName)
            && Objects.equals(this.version, dependResponsePackageInfo.version) && Objects.equals(this.versionId,
            dependResponsePackageInfo.versionId) && Objects.equals(this.scope, dependResponsePackageInfo.scope)
            && Objects.equals(this.valid, dependResponsePackageInfo.valid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, depId, owner, runtime, link, size, name, description, fileName, version, versionId,
            scope, valid);
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
     * Gets or Sets scope
     */
    public enum ScopeEnum {
        PRIVATE("private"),

        PUBLIC("public"),

        ALL("all");

        private String value;

        ScopeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ScopeEnum fromValue(String text) {
            for (ScopeEnum b : ScopeEnum.values()) {
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
