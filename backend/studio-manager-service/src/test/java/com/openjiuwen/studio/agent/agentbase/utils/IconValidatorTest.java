/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import org.junit.jupiter.api.Test;

class IconValidatorTest {

    private static final long MAX_IMAGE_SIZE_KB = 1024;

    @Test
    void testValidate_nullBase64_shouldThrowException() {
        // Arrange
        String base64 = null;
        String allowedFormats = "png,jpg";

        // Act & Assert
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            IconValidator.validate(base64, MAX_IMAGE_SIZE_KB, allowedFormats);
        });

        assertEquals(ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
    }

    @Test
    void testValidate_emptyBase64_shouldThrowException() {
        // Arrange
        String base64 = "";
        String allowedFormats = "png,jpg";

        // Act & Assert
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            IconValidator.validate(base64, MAX_IMAGE_SIZE_KB, allowedFormats);
        });

        assertEquals(ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
    }

    @Test
    void testValidate_allowedFormatsNull_shouldThrowException() {
        // Arrange
        String validDataUri
            = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mWQMQoAIAhDdYtdQmYKv8r6fzXQjXoDQXs6Qc10fd0KcI4WciJxNlH/9QwA4KqRTQAAAABJRU5ErkJggg==";
        String allowedFormats = null;

        // Act & Assert
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            IconValidator.validate(validDataUri, MAX_IMAGE_SIZE_KB, allowedFormats);
        });

        assertEquals(ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
    }

    @Test
    void testValidate_allowedFormatsEmpty_shouldThrowException() {
        // Arrange
        String validDataUri
            = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mWQMQoAIAhDdYtdQmYKv8r6fzXQjXoDQXs6Qc10fd0KcI4WciJxNlH/9QwA4KqRTQAAAABJRU5ErkJggg==";
        String allowedFormats = "";

        // Act & Assert
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            IconValidator.validate(validDataUri, MAX_IMAGE_SIZE_KB, allowedFormats);
        });

        assertEquals(ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
    }

    @Test
    void testValidate_emptyAllowedFormats_shouldThrowException() {
        // Arrange
        String validDataUri
            = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mWQMQoAIAhDdYtdQmYKv8r6fzXQjXoDQXs6Qc10fd0KcI4WciJxNlH/9QwA4KqRTQAAAABJRU5ErkJggg==";
        String allowedFormats = "  ";

        // Act & Assert
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            IconValidator.validate(validDataUri, MAX_IMAGE_SIZE_KB, allowedFormats);
        });

        assertEquals(ErrorCode.INVALID_PARAMETER, exception.getErrorCode());
    }

}
