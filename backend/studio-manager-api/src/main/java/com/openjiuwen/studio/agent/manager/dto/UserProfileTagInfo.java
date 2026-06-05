/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 用户画像记忆标签信息。
 */
@ApiModel(description = "用户画像记忆标签信息。")

@Validated

public class UserProfileTagInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("tag_name")
    @NotBlank
    @Length(max = 100)
    private String tagName = null;

    @JsonProperty("tag_description")
    @Length(max = 500)
    private String tagDescription = null;

    public String getTagName() {
        return tagName;
    }

    public UserProfileTagInfo setTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public String getTagDescription() {
        return tagDescription;
    }

    public UserProfileTagInfo setTagDescription(String tagDescription) {
        this.tagDescription = tagDescription;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserProfileTagInfo {\n");

        sb.append("    tagName: ").append(toIndentedString(tagName)).append("\n");
        sb.append("    tagDescription: ").append(toIndentedString(tagDescription)).append("\n");
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
        UserProfileTagInfo userProfileTagInfo = (UserProfileTagInfo) o;
        return Objects.equals(this.tagName, userProfileTagInfo.tagName) && Objects.equals(this.tagDescription,
            userProfileTagInfo.tagDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName, tagDescription);
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
