/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 功能描述
 *
 */
@Data
@Builder
public class AskModelReq {
    private ModelConfig model;

    @Size(max = 1000)
    private List<MessageEntity> messages;
}
