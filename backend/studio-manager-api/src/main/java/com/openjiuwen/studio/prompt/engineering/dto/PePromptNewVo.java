/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * PePromptNewVo
 */

@Validated
public class PePromptNewVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    private String id = null;

    @JsonProperty("name")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5\\w-()（）]{0,32}[\\u4e00-\\u9fa5a-zA-Z0-9()（）]$")
    private String name = null;

    @JsonProperty("content")
    @Length(min = 1, max = 10000)
    private String content = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("updater")
    private String updater = null;

    @JsonProperty("created_on")
    private Date createdOn = null;

    @JsonProperty("updated_on")
    private Date updatedOn = null;

    public String getId() {
        return id;
    }

    public PePromptNewVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PePromptNewVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PePromptNewVo setContent(String content) {
        this.content = content;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public PePromptNewVo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getUpdater() {
        return updater;
    }

    public PePromptNewVo setUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public PePromptNewVo setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public PePromptNewVo setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PePromptNewVo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    updater: ").append(toIndentedString(updater)).append("\n");
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
        PePromptNewVo pePromptNewVo = (PePromptNewVo) o;
        return Objects.equals(this.id, pePromptNewVo.id) && Objects.equals(this.name, pePromptNewVo.name)
            && Objects.equals(this.content, pePromptNewVo.content) && Objects.equals(this.creator,
            pePromptNewVo.creator) && Objects.equals(this.updater, pePromptNewVo.updater) && Objects.equals(
            this.createdOn, pePromptNewVo.createdOn) && Objects.equals(this.updatedOn, pePromptNewVo.updatedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, content, creator, updater, createdOn, updatedOn);
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
