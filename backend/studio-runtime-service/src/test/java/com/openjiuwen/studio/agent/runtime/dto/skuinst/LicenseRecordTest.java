/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.skuinst;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
public class LicenseRecordTest {
    @Test
    public void test() {
        LicenseRecord record = new LicenseRecord().setDomainId("d")
            .setResourceId("r")
            .setMaxValue(10)
            .setCurValue(1)
            .setIncrementalValue(1);

        record.incremental(1);
        Assertions.assertEquals(2, record.getIncrementalValue());
        LicenseReport report = record.createLicenseReport();
        Assertions.assertEquals("2", report.getQuantity());

        LicenseInst inst = new LicenseInst();
        record = new LicenseRecord("", inst);
    }
}
