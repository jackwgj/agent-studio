/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NoMatchContentEntity {
    String prompt;

    List<String> template;
}
