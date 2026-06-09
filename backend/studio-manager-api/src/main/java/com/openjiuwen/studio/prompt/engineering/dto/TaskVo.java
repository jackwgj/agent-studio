/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * TaskVo
 */

@Validated
public class TaskVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("created_on")
    private Date createdOn = null;

    @JsonProperty("updater")
    private String updater = null;

    @JsonProperty("updated_on")
    private Date updatedOn = null;

    @JsonProperty("tag_list")
    @Valid
    private List<TagVo> tagList = null;

    @JsonProperty("iterationNum")
    private Integer iterationNum = null;

    @JsonProperty("industry")
    @Valid
    private IndustryVo industry = null;

    public String getId() {
        return id;
    }

    public TaskVo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TaskVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TaskVo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public TaskVo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public TaskVo setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public String getUpdater() {
        return updater;
    }

    public TaskVo setUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public TaskVo setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    public List<TagVo> getTagList() {
        return tagList;
    }

    public TaskVo setTagList(List<TagVo> tagList) {
        this.tagList = tagList;
        return this;
    }

    public Integer getIterationNum() {
        return iterationNum;
    }

    public TaskVo setIterationNum(Integer iterationNum) {
        this.iterationNum = iterationNum;
        return this;
    }

    public IndustryVo getIndustry() {
        return industry;
    }

    public TaskVo setIndustry(IndustryVo industry) {
        this.industry = industry;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskVo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
        sb.append("    updater: ").append(toIndentedString(updater)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
        sb.append("    tagList: ").append(toIndentedString(tagList)).append("\n");
        sb.append("    iterationNum: ").append(toIndentedString(iterationNum)).append("\n");
        sb.append("    industry: ").append(toIndentedString(industry)).append("\n");
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
        TaskVo taskVo = (TaskVo) o;
        return Objects.equals(this.id, taskVo.id) && Objects.equals(this.name, taskVo.name) && Objects.equals(
            this.description, taskVo.description) && Objects.equals(this.creator, taskVo.creator) && Objects.equals(
            this.createdOn, taskVo.createdOn) && Objects.equals(this.updater, taskVo.updater) && Objects.equals(
            this.updatedOn, taskVo.updatedOn) && Objects.equals(this.tagList, taskVo.tagList) && Objects.equals(
            this.iterationNum, taskVo.iterationNum) && Objects.equals(this.industry, taskVo.industry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, creator, createdOn, updater, updatedOn, tagList, iterationNum,
            industry);
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
