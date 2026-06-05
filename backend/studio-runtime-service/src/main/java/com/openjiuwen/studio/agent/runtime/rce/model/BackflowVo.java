/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能描述 数据回流信息
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackflowVo {
    private BackflowModel model;

    private List<BackflowMessage> messages;

    private BackflowSource source;
}
