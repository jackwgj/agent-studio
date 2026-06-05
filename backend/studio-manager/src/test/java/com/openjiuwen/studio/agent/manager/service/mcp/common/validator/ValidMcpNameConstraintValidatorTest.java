/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.common.validator;

import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class ValidMcpNameConstraintValidatorTest extends BaseTest {
    ValidMcpNameConstraintValidator validMcpNameConstraintValidator = new ValidMcpNameConstraintValidator();

    @Test
    void testInitialize() {
        Assertions.assertThrows(Exception.class,
            () -> validMcpNameConstraintValidator.initialize(null));
    }

    @Test
    void testIsValid() {
        boolean result = validMcpNameConstraintValidator.isValid("value", null);
        Assertions.assertEquals(true, result);
    }

    @Test
    void testIsValid2() {
        boolean result = validMcpNameConstraintValidator.isValid(null, null);
        Assertions.assertEquals(false, result);
    }
}

