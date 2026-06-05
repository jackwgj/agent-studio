/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 知识库文档标签列表查询响应体
 */
@ApiModel(description = "知识库文档标签列表查询响应体")

@Validated

public class ListKnowledgeTagsResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    @NotNull
    @Range(min = 0L, max = 65535L)
    private Integer count = 0;

    @JsonProperty("tag_list")
    @Valid
    @NotNull
    @Size(min = 1, max = 250)
    private List<KnowledgeTagInfo> tagList = new ArrayList<KnowledgeTagInfo>();

    public Integer getCount() {
        return count;
    }

    public ListKnowledgeTagsResp setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<KnowledgeTagInfo> getTagList() {
        return tagList;
    }

    public ListKnowledgeTagsResp setTagList(List<KnowledgeTagInfo> tagList) {
        this.tagList = tagList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListKnowledgeTagsResp {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    tagList: ").append(toIndentedString(tagList)).append("\n");
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
        ListKnowledgeTagsResp listKnowledgeTagsResp = (ListKnowledgeTagsResp) o;
        return Objects.equals(this.count, listKnowledgeTagsResp.count) && Objects.equals(this.tagList,
            listKnowledgeTagsResp.tagList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, tagList);
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
}
