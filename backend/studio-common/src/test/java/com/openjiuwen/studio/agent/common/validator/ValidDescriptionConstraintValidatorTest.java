/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.validator;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidDescriptionConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ValidDescription validDescription;

    private ValidDescriptionConstraintValidator validator;

    @BeforeEach
    void setUp() {
        when(validDescription.emptyAble()).thenReturn(true);
        validator = new ValidDescriptionConstraintValidator();
        validator.initialize(validDescription);
    }

    @Test
    void testIsValid_ValidEnglish() {
        assertTrue(validator.isValid("Hello World", context));
    }

    @Test
    void testIsValid_ValidChinese() {
        assertTrue(validator.isValid("你好世界", context));
    }

    @Test
    void testIsValid_ValidMixed() {
        assertTrue(validator.isValid("Hello 你好 test-123_test", context));
    }

    @Test
    void testIsValid_ValidSpecialChars() {
        assertTrue(validator.isValid("test,.?:;\"'，。？、()（）/@!！*%#", context));
    }

    @Test
    void testIsValid_InvalidChars() {
        assertFalse(validator.isValid("test<script>", context));
    }

    @Test
    void testIsValid_InvalidBrackets() {
        assertFalse(validator.isValid("test[0]", context));
    }

    @Test
    void testIsValid_EmptyValueEmptyAble() {
        assertTrue(validator.isValid("", context));
    }

    @Test
    void testIsValid_NullValueEmptyAble() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testIsValid_EmptyValueNotAble() {
        when(validDescription.emptyAble()).thenReturn(false);
        ValidDescriptionConstraintValidator v = new ValidDescriptionConstraintValidator();
        v.initialize(validDescription);
        assertFalse(v.isValid("", context));
    }

    @Test
    void testIsValid_NullValueNotAble() {
        when(validDescription.emptyAble()).thenReturn(false);
        ValidDescriptionConstraintValidator v = new ValidDescriptionConstraintValidator();
        v.initialize(validDescription);
        assertFalse(v.isValid(null, context));
    }

    @Test
    void testIsValid_NumbersAndUnderscore() {
        assertTrue(validator.isValid("test_123", context));
    }

    @Test
    void testIsValid_HyphenAndSpace() {
        assertTrue(validator.isValid("test-case description", context));
    }
}
