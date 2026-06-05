/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.md;

import com.openjiuwen.studio.agent.runtime.enums.ModelStrategyEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ModelStrategy {
    private ModelStrategyEnum type;

    private String name;

    private List<ModelServiceDetail> models;

    private Integer retryCount;

    private Integer strategyTimeout;
}
