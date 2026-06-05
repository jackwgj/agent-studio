/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.enums.v2.PtTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提示词优化任务变量
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptVar {
    /**
     * 提示词优化任务变量名
     */
    @JsonProperty("name")
    private String name;

    /**
     * 提示词优化任务变量值
     */
    @JsonProperty("content")
    private String content;

    /**
     * 提示词优化任务变类型
     */
    @JsonProperty("type")
    private PtTypeEnum type;
}
