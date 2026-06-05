/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.model.skuinst;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class LicenseReportReq {

    @Size(max = 1000)
    private List<LicenseReport> list;
}
