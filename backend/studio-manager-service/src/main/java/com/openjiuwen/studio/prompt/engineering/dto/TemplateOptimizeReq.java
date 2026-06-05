/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * 模板自优化请求体
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateOptimizeReq {
    private String name;

    private String desc;

    private List<String> rawTemplates;

    private OptimizeInfo optimizeInfo;

    private ModelProperties modelInfo;

    private ModelProperties assistantInfo;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        TemplateOptimizeReq req = (TemplateOptimizeReq) object;
        return Objects.equals(rawTemplates, req.rawTemplates) && Objects.equals(optimizeInfo, req.optimizeInfo)
            && Objects.equals(modelInfo, req.modelInfo) && Objects.equals(assistantInfo, req.assistantInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawTemplates, optimizeInfo, modelInfo, assistantInfo);
    }
}
