/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class IconNameCheckUtilsTest {
    @Test
    void test_validaIconName_should_throw_exception_when_input_is_null() throws Exception {
        // When & Then
        assertThrows(AgentStudioException.class, () -> {
            IconNameCheckUtils.validaIconName("");
        });
    }

    @Test
    void test_validaIconName_should_throw_exception_when_uuid_is_invalid() throws Exception {
        // When & Then
        assertThrows(AgentStudioException.class, () -> {
            IconNameCheckUtils.validaIconName("invalid-uuid.jpg");
        });
    }

    @Test
    void test_validaIconName_should_pass_when_uuid_is_valid() throws Exception {
        assertDoesNotThrow(() -> {
            // When
            IconNameCheckUtils.validaIconName("3f2504e0-4f89-11d3-9a0c-0305e82c3301.jpg");
        });
    }
}
