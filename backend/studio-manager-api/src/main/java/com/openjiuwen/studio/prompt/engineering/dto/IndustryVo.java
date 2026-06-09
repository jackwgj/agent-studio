/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Pattern;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * IndustryVo
 */

@Validated
public class IndustryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    private String id = null;

    @JsonProperty("name")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5\\w-]{0,30}[\\u4e00-\\u9fa5a-zA-Z0-9]$")
    private String name = null;

    @JsonProperty("name_en")
    private String nameEn = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("library_type")
    private String libraryType = null;

    @JsonProperty("created_on")
    private Date createdOn = null;

    @JsonProperty("updated_on")
    private Date updatedOn = null;

    public String getId() {
        return id;
    }

    public IndustryVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public IndustryVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameEn() {
        return nameEn;
    }

    public IndustryVo setNameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public IndustryVo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLibraryType() {
        return libraryType;
    }

    public IndustryVo setLibraryType(String libraryType) {
        this.libraryType = libraryType;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public IndustryVo setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public IndustryVo setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IndustryVo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameEn: ").append(toIndentedString(nameEn)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    libraryType: ").append(toIndentedString(libraryType)).append("\n");
        sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
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
        IndustryVo industryVo = (IndustryVo) o;
        return Objects.equals(this.id, industryVo.id) && Objects.equals(this.name, industryVo.name) && Objects.equals(
            this.nameEn, industryVo.nameEn) && Objects.equals(this.description, industryVo.description)
            && Objects.equals(this.libraryType, industryVo.libraryType) && Objects.equals(this.createdOn,
            industryVo.createdOn) && Objects.equals(this.updatedOn, industryVo.updatedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, nameEn, description, libraryType, createdOn, updatedOn);
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
