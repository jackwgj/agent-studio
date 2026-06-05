/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户反馈原因
 */
@ApiModel(description = "用户反馈原因")
@Validated
public class FeedbackReason {
    @JsonProperty("tags")
    @Valid
    @NotNull
    @Size(min = 1, max = 10)
    private List<@Length(max = 64) String> tags = new ArrayList<>();

    @JsonProperty("content")
    @Length(max = 256)
    private String content = null;

    public List<String> getTags() {
        return tags;
    }

    public FeedbackReason setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public String getContent() {
        return content;
    }

    public FeedbackReason setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FeedbackReason {\n");

        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
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
        FeedbackReason feedbackReason = (FeedbackReason) o;
        return Objects.equals(this.tags, feedbackReason.tags) && Objects.equals(this.content, feedbackReason.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags, content);
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
