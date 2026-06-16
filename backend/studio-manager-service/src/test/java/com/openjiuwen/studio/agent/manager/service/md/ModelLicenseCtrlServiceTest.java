/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.md;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ModelLicenseCtrlServiceTest {

    @InjectMocks
    private ModelLicenseCtrlService modelLicenseCtrlService;

    @Test
    void testCanAccessIntegrationModel_NoException() {
        assertDoesNotThrow(() -> modelLicenseCtrlService.canAccessIntegrationModel());
    }

    @Test
    void testCanAccessIntegrationModel_VoidMethod() {
        modelLicenseCtrlService.canAccessIntegrationModel();
    }

    @Test
    void testCanUseFreeModel_ReturnsFalse() {
        assertFalse(modelLicenseCtrlService.canUseFreeModel());
    }

    @Test
    void testCanUseFreeModel_ConsistentResult() {
        boolean result1 = modelLicenseCtrlService.canUseFreeModel();
        boolean result2 = modelLicenseCtrlService.canUseFreeModel();
        assertEquals(result1, result2);
    }
}
