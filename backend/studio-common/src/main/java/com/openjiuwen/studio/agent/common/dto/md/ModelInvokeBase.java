/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.md;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelInvokeBase {
    @Size(max = 200)
    @NotNull(message = "Parameter 'model' must be not null, please check and try again later!")
    private String model;

    private String refresh;
}
