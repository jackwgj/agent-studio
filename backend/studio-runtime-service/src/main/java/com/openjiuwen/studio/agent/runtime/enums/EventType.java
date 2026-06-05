/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 流式接口event类型
 *
 */
public enum EventType {
    /**
     * 开始节点
     */
    START("start"),

    /**
     * 消息节点
     */
    MESSAGE("message"),

    /**
     * 插件调用开始
     */
    PLUGIN_START("plugin_start"),

    /**
     * 插件调用结束
     */
    PLUGIN_END("plugin_end"),

    /**
     * 调用过程指标
     */
    STATIC_DATA("statistic_data"),

    /**
     * 模型总结
     */
    SUMMARY_RESPONSE("summary_response"),

    /**
     * 知识库文档
     */
    KNOWLEDGE_BASE_FILE("knowledge_base_files_info"),

    /**
     * 流式调用结束
     */
    DONE("done"),
    NO_MATCH("no-match"),
    CREATING("creating"),
    CREATED("created"),

    /**
     * 自定义类型
     */
    MEMORY_UPDATED("MEMORY_UPDATED"),

    /**
     * 多智能体结束事件
     */
    TASK_END("task_end"),

    /**
     * 执行报错
     */
    ERROR("error");

    private final String type;

    EventType(String type) {
        this.type = type;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(type);
    }
}
