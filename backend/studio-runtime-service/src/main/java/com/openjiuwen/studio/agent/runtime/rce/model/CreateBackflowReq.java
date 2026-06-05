/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能描述 反馈数据回流请求体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBackflowReq {
    @Size(max = 1000)
    private List<BackflowVo> data;
}
