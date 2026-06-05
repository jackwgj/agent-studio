/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 功能描述
 *
 */
@Data
@Accessors(chain = true)
public class ScasImageInfo {
    private String name;

    private String url;

    private String digestValue;

}
