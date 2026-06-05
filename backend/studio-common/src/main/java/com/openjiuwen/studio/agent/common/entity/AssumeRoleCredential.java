/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssumeRoleCredential {
    private String domain_id;

    private String agency_name;
}
