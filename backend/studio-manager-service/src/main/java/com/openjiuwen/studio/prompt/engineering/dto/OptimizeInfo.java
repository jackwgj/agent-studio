/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户需要提供的关于优化的信息，主要是评测的case
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizeInfo {

    private List<ModelCase> cases;

    private Integer numIter;

}
