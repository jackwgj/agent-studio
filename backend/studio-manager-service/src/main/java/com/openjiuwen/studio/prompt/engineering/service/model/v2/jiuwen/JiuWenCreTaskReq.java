/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.entity.v2.ExecConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiuWenCreTaskReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 描述
     */
    @JsonProperty("desc")
    private String desc;

    /**
     * 原始模板内容（Markdown 格式）
     */
    @JsonProperty("rawTemplates")
    private String rawTemplates;

    /**
     * 模型信息配置
     */
    @JsonProperty("modelInfo")
    private ExecConfig modelInfo;

    /**
     * 助手模型信息配置
     */
    @JsonProperty("assistantInfo")
    private ExecConfig assistantInfo;

    /**
     * 优化信息配置
     */
    @JsonProperty("optimizeInfo")
    private JiuWenOptimizeInfo optimizeInfo;
}
