/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * 用户反馈请求体
 */
@ApiModel(description = "用户反馈请求体")
@Validated
public class Feedback {
    @JsonProperty("app_name")
    @NotBlank
    @Length(max = 64)
    private String appName = null;

    @JsonProperty("rating")
    @NotNull
    @Range(min = -1L, max = 1L)
    private Integer rating = null;

    @JsonProperty("reason")
    @Valid
    private FeedbackReason reason = null;

    public String getAppName() {
        return appName;
    }

    public Feedback setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public Integer getRating() {
        return rating;
    }

    public Feedback setRating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public FeedbackReason getReason() {
        return reason;
    }

    public Feedback setReason(FeedbackReason reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Feedback {\n");

        sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
        sb.append("    rating: ").append(toIndentedString(rating)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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
        Feedback feedback = (Feedback) o;
        return Objects.equals(this.appName, feedback.appName) && Objects.equals(this.rating, feedback.rating)
            && Objects.equals(this.reason, feedback.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, rating, reason);
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
