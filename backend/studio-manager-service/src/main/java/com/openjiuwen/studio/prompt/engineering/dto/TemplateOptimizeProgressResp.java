/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板自优化进度响应体
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateOptimizeProgressResp {
    private Integer code;

    private String message;

    private ProgressInfo progress;

}
