/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Api {
    private String url;

    private String method;
}
