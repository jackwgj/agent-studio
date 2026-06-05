/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.license.models;

import lombok.Data;

/**
 * License 详细信息
 *
 */
@Data
public class LicenseDetail {
    private String filePrdName;

    private String filePrdVer;

    private String esn;

    private LicenseState state;

    private long expireIn;

    private boolean isPerm;

    private long lastUpdate;

    private float cpu;

    private long bbom;

    public boolean getIsPerm() {
        return isPerm;
    }
}
