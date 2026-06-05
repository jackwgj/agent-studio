/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.skuinst;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class LicenseRecord {
    private String resourceId;

    private String skuCode;

    private String attrCode;

    private String domainId;

    private int maxValue;

    private int curValue;

    private int incrementalValue;

    public LicenseRecord(String domainId, LicenseInst inst) {
        this.domainId = domainId;
        this.resourceId = inst.getResourceId();
        this.skuCode = inst.getSkuCode();
        this.attrCode = inst.getAttrCode();
        this.maxValue = StringUtils.isEmpty(inst.getMaxValue()) ? -1 : Integer.parseInt(inst.getMaxValue());
        this.curValue = StringUtils.isEmpty(inst.getCurrentValue()) ? 0 : Integer.parseInt(inst.getCurrentValue());
    }

    @JsonIgnore
    public boolean isAvailable() {
        return -1 == maxValue || curValue < maxValue;
    }

    public void incremental(int value) {
        this.incrementalValue += value;
    }

    public LicenseReport createLicenseReport() {
        return new LicenseReport().setQuantity(Integer.toString(incrementalValue))
                .setResourceId(resourceId)
                .setAttrCode(attrCode)
                .setOperType("add");
    }
}
