/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 会话信息表
 */
@ApiModel(description = "会话信息表")
@Validated
public class ConversionQueries {
    @JsonProperty("count")
    @NotNull
    private Integer count = null;

    @JsonProperty("conversation_infos")
    @Valid
    @NotNull
    @Size()
    private List<ConversationInfo> conversationInfos = new ArrayList<>();

    public Integer getCount() {
        return count;
    }

    public ConversionQueries setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<ConversationInfo> getConversationInfos() {
        return conversationInfos;
    }

    public ConversionQueries setConversationInfos(List<ConversationInfo> conversationInfos) {
        this.conversationInfos = conversationInfos;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConversionQueries {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    conversationInfos: ").append(toIndentedString(conversationInfos)).append("\n");
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
        ConversionQueries conversionQueries = (ConversionQueries) o;
        return Objects.equals(this.count, conversionQueries.count) && Objects.equals(this.conversationInfos,
            conversionQueries.conversationInfos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, conversationInfos);
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
