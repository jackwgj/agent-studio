/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity.md;

import lombok.Data;

/**
 * ProviderAuthBoundRecord
 *
 */
@Data
public class ProviderAuthBoundRecordPo {
    private String id;

    private String providerAuthId;

    private String domainId;

    private String providerId;

    private String createdByUser;

    private Long createdDate;
}
