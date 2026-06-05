/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums;

import lombok.Getter;

/**
 * 节点输出变量的映射关系
 * 第三方知识库类型_转换后的节点类型_第三方知识库的引用变量名称  ->   转换后的引用变量名称
 *
 */
@Getter
public enum OutputParamsDiffType {

    DIFY_KNOWLEDGE_REPO_RESULT("Dify_KnowledgeRepo_result", "output_list"),

    DIFY_INTENT_DETECTION_CLASS_NAME("Dify_IntentDetection_class_name", "name"),

    DIFY_AGGREGATION_OUTPUT("Dify_Aggregation_output", "group1"),

    DIFY_AGENT_TEXT("Dify_Agent_text", "output");

    private final String rawName;

    private final String convertName;

    OutputParamsDiffType(String rawName, String convertName) {
        this.rawName = rawName;
        this.convertName = convertName;
    }

    /**
     * 获取转换后的变量名
     * 不存在映射关系则直接返回原有名称
     *
     * @param rawName
     * @return
     */
    public static String getVariableName(String rawName) {
        for (OutputParamsDiffType outputParamsDiffType : OutputParamsDiffType.values()) {
            if (outputParamsDiffType.rawName.equalsIgnoreCase(rawName)) {
                return outputParamsDiffType.convertName;
            }
        }
        return rawName.replaceFirst("^[^_]*_[^_]*_(.*)", "$1");
    }
}
