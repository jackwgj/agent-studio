/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 用户画像记忆配置。
 */
@ApiModel(description = "用户画像记忆配置。")

@Validated

public class UserProfileMemoryConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("user_profile_enable")
    private Boolean userProfileEnable = false;

    @JsonProperty("time_window")
    @Valid
    private UserProfileTimeWindowConfig timeWindow = null;

    @JsonProperty("topics")
    @Valid
    @Size(max = 30)
    private List<UserProfileTopicInfo> topics = null;

    public UserProfileMemoryConfig setUserProfileEnable(Boolean userProfileEnable) {
        this.userProfileEnable = userProfileEnable;
        return this;
    }

    public Boolean isUserProfileEnable() {
        return userProfileEnable;
    }

    public UserProfileTimeWindowConfig getTimeWindow() {
        return timeWindow;
    }

    public UserProfileMemoryConfig setTimeWindow(UserProfileTimeWindowConfig timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }

    public List<UserProfileTopicInfo> getTopics() {
        return topics;
    }

    public UserProfileMemoryConfig setTopics(List<UserProfileTopicInfo> topics) {
        this.topics = topics;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserProfileMemoryConfig {\n");

        sb.append("    userProfileEnable: ").append(toIndentedString(userProfileEnable)).append("\n");
        sb.append("    timeWindow: ").append(toIndentedString(timeWindow)).append("\n");
        sb.append("    topics: ").append(toIndentedString(topics)).append("\n");
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
        UserProfileMemoryConfig userProfileMemoryConfig = (UserProfileMemoryConfig) o;
        return Objects.equals(this.userProfileEnable, userProfileMemoryConfig.userProfileEnable) && Objects.equals(
            this.timeWindow, userProfileMemoryConfig.timeWindow) && Objects.equals(this.topics,
            userProfileMemoryConfig.topics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userProfileEnable, timeWindow, topics);
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
