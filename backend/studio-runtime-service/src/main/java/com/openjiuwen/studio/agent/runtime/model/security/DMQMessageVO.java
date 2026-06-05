/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.Data;

/**
 * DMQMessageVO
 *
 */
@Data
public class DMQMessageVO {
    private String cluster;

    private String groupid;

    private String key;

    private String message;

    private String offset;

    private String partition;

    private String timestamp;

    private String topic;

    private String val;
}
