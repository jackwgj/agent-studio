/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 创建生成AiPpt任务响应
 */
@Data
public class AiPptCreateTaskVo {
    @JsonProperty(value = "id")
    private String id;

    /**
     * ppt题目
     */
    @JsonProperty(value = "title")
    private String title;

    /**
     * 任务类型
     */
    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "api_key")
    private String apiKey;

    @JsonProperty(value = "create_at")
    private String createAt;
}
