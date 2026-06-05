/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.nodes;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 功能描述 节点ID生成模板
 *
 */
public enum ParamExtractionIdFormatEnum {
    /**
     * 拆分节点ID模板
     */
    COMPOSITE_FORMAT("composite_%s_node_%s"),

    /**
     * 领域对象后处理ID模板
     */
    DOMAIN_OBJECTS("%s_domain_objects_%s"),

    /**
     * 预处理工作流ID模板
     */
    BEFORE_ENTRY("%s_extension_before_entry"),

    /**
     * 退出前工作流ID模板
     */
    AFTER_QUIT("%s_extension_before_judge_quit"),

    /**
     * 模型提惨后处理工作流ID模板
     */
    AFTER_EXTRACTION("%s_extension_after_extraction"),

    /**
     * 新一轮开始ID模板
     */
    CYCLE_BEGIN("%s_cycle_begin"),

    /**
     * 参数提取节点开始ID模板
     */
    START("%s_start"),

    /**
     * 参数提取节点结束ID模板
     */
    END("%s_end"),

    /**
     * 参数提取节点异常分支ID模板
     */
    EXCEPTION("%s_end_exception"),

    LLM("%s_llm");

    private final String format;

    ParamExtractionIdFormatEnum(String format) {
        this.format = format;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(format);
    }
}
