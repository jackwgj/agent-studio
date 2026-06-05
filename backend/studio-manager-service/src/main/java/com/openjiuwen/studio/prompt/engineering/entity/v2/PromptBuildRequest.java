/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PromptBuildRequest {

    private String instruct;

    private List<Tool> tools;

    private ExecConfig modelInfo;

    private boolean stream;

    /**
     * 工具信息内部类
     */
    @Data
    public static class Tool {
        // 工具名称（如：duckduckgo）
        private String name;

        // 工具描述（如：网页搜索工具）
        private String description;
    }

    /**
     * 模型信息内部类
     */
    @Data
    public static class ModelInfo {

        private String model;

        private String model_source;

        private Object headers;
    }
}
