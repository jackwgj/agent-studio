/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 九问优化任务信息
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationJobInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String desc;
}
