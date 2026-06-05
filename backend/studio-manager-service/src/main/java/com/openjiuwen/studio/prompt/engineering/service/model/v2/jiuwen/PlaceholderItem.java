/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceholderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 占位符名称（如 placeholder_1）
     */
    @JsonProperty("name")
    private String name;

    /**
     * 占位符内容（如 可视化报告）
     */
    @JsonProperty("content")
    private String content;

    /**
     * 是否需要优化
     */
    @JsonProperty("need_optimize")
    private Boolean needOptimize;

}
