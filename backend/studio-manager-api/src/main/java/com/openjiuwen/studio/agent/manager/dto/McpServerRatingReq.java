/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * McpServerRatingReq
 */

@Validated

public class McpServerRatingReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("server_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(max = 64)
    private String serverId = null;

    @JsonProperty("score")
    @Length(max = 2)
    private String score = null;

    public String getServerId() {
        return serverId;
    }

    public McpServerRatingReq setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public String getScore() {
        return score;
    }

    public McpServerRatingReq setScore(String score) {
        this.score = score;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpServerRatingReq {\n");

        sb.append("    serverId: ").append(toIndentedString(serverId)).append("\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
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
        McpServerRatingReq mcpServerRatingReq = (McpServerRatingReq) o;
        return Objects.equals(this.serverId, mcpServerRatingReq.serverId) && Objects.equals(this.score,
            mcpServerRatingReq.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverId, score);
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
