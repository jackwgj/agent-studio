/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * TagVo
 */

@Validated
public class TagVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("name_en")
    private String nameEn = null;

    @JsonProperty("created_on")
    private Date createdOn = null;

    @JsonProperty("updated_on")
    private Date updatedOn = null;

    public String getId() {
        return id;
    }

    public TagVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TagVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameEn() {
        return nameEn;
    }

    public TagVo setNameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public TagVo setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public TagVo setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TagVo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameEn: ").append(toIndentedString(nameEn)).append("\n");
        sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TagVo tagVo = (TagVo) obj;
        return Objects.equals(this.id, tagVo.id) && Objects.equals(this.name, tagVo.name) && Objects.equals(this.nameEn,
            tagVo.nameEn) && Objects.equals(this.createdOn, tagVo.createdOn) && Objects.equals(this.updatedOn,
            tagVo.updatedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, nameEn, createdOn, updatedOn);
    }

    private String toIndentedString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", "\n    ");
    }
}
