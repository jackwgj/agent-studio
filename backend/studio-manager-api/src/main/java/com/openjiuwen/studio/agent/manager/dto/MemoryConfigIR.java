/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 记忆配置的IR对象。
 */
@ApiModel(description = "记忆配置的IR对象。")

@Validated

public class MemoryConfigIR implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memory_repo_id")
    private String memoryRepoId = null;

    @JsonProperty("extract_config")
    private ExtractConfig extractConfig = null;

    @JsonProperty("strategies")
    private List<LongTermMemoryStrategy> strategies = null;

    public String getMemoryRepoId() {
        return memoryRepoId;
    }

    public MemoryConfigIR setMemoryRepoId(String memoryRepoId) {
        this.memoryRepoId = memoryRepoId;
        return this;
    }

    public ExtractConfig getExtractConfig() {
        return extractConfig;
    }

    public MemoryConfigIR setExtractConfig(ExtractConfig extractConfig) {
        this.extractConfig = extractConfig;
        return this;
    }

    public List<LongTermMemoryStrategy> getStrategies() {
        return strategies;
    }

    public MemoryConfigIR setStrategies(List<LongTermMemoryStrategy> strategies) {
        this.strategies = strategies;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MemoryConfigIR {\n");
        sb.append("    memoryRepoId: ").append(toIndentedString(memoryRepoId)).append("\n");
        sb.append("    extractConfig: ").append(toIndentedString(extractConfig)).append("\n");
        sb.append("    strategies: ").append(toIndentedString(strategies)).append("\n");
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
        MemoryConfigIR memoryConfigIR = (MemoryConfigIR) o;
        return Objects.equals(this.memoryRepoId, memoryConfigIR.memoryRepoId)
            && Objects.equals(this.extractConfig, memoryConfigIR.extractConfig)
            && Objects.equals(this.strategies, memoryConfigIR.strategies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryRepoId, extractConfig, strategies);
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * 提取触发配置，对应 Python IR 中的 configs.memory.extractConfig。
     */
    public static class ExtractConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("max_chat_turn")
        private Integer maxChatTurn = null;

        @JsonProperty("time_window")
        private Integer timeWindow = null;

        public Integer getMaxChatTurn() {
            return maxChatTurn;
        }

        public ExtractConfig setMaxChatTurn(Integer maxChatTurn) {
            this.maxChatTurn = maxChatTurn;
            return this;
        }

        public Integer getTimeWindow() {
            return timeWindow;
        }

        public ExtractConfig setTimeWindow(Integer timeWindow) {
            this.timeWindow = timeWindow;
            return this;
        }

        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExtractConfig that = (ExtractConfig) o;
            return Objects.equals(this.maxChatTurn, that.maxChatTurn)
                && Objects.equals(this.timeWindow, that.timeWindow);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maxChatTurn, timeWindow);
        }

        @Override
        public String toString() {
            return "ExtractConfig{maxChatTurn=" + maxChatTurn + ", timeWindow=" + timeWindow + "}";
        }
    }
}
