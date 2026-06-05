/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 模板自优化响应体
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateOptimizeResp implements Serializable {

    private int code;

    private String message;

    private OptimizationJobInfo jobInfo;

}
