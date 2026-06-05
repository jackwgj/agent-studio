/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 查询多智能体会话列表返回模型
 */
@ApiModel(description = "查询多智能体会话列表返回模型")

@Validated

public class ListControllerConversionsResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    @NotNull
    private Integer count = null;

    @JsonProperty("conversations")
    @Valid
    @NotNull
    @Size()
    private List<ControllerConversation> conversations = new ArrayList<ControllerConversation>();

    public Integer getCount() {
        return count;
    }

    public ListControllerConversionsResp setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<ControllerConversation> getConversations() {
        return conversations;
    }

    public ListControllerConversionsResp setConversations(List<ControllerConversation> conversations) {
        this.conversations = conversations;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListControllerConversionsResp {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    conversations: ").append(toIndentedString(conversations)).append("\n");
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
        ListControllerConversionsResp listControllerConversionsResp = (ListControllerConversionsResp) o;
        return Objects.equals(this.count, listControllerConversionsResp.count) && Objects.equals(this.conversations,
            listControllerConversionsResp.conversations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, conversations);
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
