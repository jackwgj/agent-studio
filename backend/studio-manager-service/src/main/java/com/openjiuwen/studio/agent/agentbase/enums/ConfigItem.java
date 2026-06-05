/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ConfigItem {
    KNOWLEDGE_BASE_NUM("knowledgeBase", "知识库数量"),

    KNOWLEDGE_BASE_DATASET_NUM("knowledgeBaseDateset", "数据集数量"),

    KNOWLEDGE_BASE_DATASET_STORAGE("knowledgeBaseDatesetStorage", "数据集存储容量"),

    THIRD_PARTY_KNOWLEDGE_BASE_NUM("thirdPartyKnowledgeBase", "第三方知识库数量"),

    THIRD_PARTY_KNOWLEDGE_BASE_CONNECTION_NUM("thirdPartyKnowledgeBaseConnection", "第三方知识库连接数量");

    private final String value;

    private final String description;

    ConfigItem(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Optional<ConfigItem> fromName(String name) {
        return Arrays.stream(values()).filter(item -> item.value.equals(name)).findFirst();
    }
}
