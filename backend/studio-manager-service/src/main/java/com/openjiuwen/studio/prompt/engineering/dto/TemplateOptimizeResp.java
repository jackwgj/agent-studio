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

    // 静态代码检查G.SER.02：实现Serializable的类应显式声明serialVersionUID，避免类变更后反序列化兼容性问题
    private static final long serialVersionUID = 1L;

    private int code;

    private String message;

    private OptimizationJobInfo jobInfo;

}
