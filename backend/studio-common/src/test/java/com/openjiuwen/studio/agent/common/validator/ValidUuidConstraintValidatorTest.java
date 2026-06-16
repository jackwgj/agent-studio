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
class ValidUuidConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ValidUuid validUuid;

    private ValidUuidConstraintValidator validator;

    @BeforeEach
    void setUp() {
        when(validUuid.emptyAble()).thenReturn(false);
        when(validUuid.minSize()).thenReturn(1);
        when(validUuid.maxSize()).thenReturn(64);
        validator = new ValidUuidConstraintValidator();
        validator.initialize(validUuid);
    }

    @Test
    void testIsValid_ValidUuid() {
        assertTrue(validator.isValid("abc-123-DEF", context));
    }

    @Test
    void testIsValid_UuidWithOnlyAlphanumeric() {
        assertTrue(validator.isValid("abc123DEF", context));
    }

    @Test
    void testIsValid_UuidWithHyphens() {
        assertTrue(validator.isValid("a1b2c3d4-e5f6-7890-abcd-ef1234567890", context));
    }

    @Test
    void testIsValid_EmptyValueNotAble() {
        assertFalse(validator.isValid("", context));
    }

    @Test
    void testIsValid_NullValueNotAble() {
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void testIsValid_EmptyValueEmptyAble() {
        when(validUuid.emptyAble()).thenReturn(true);
        ValidUuidConstraintValidator v = new ValidUuidConstraintValidator();
        v.initialize(validUuid);
        assertTrue(v.isValid("", context));
    }

    @Test
    void testIsValid_NullValueEmptyAble() {
        when(validUuid.emptyAble()).thenReturn(true);
        ValidUuidConstraintValidator v = new ValidUuidConstraintValidator();
        v.initialize(validUuid);
        assertTrue(v.isValid(null, context));
    }

    @Test
    void testIsValid_InvalidCharactersUnderscore() {
        assertFalse(validator.isValid("abc_123", context));
    }

    @Test
    void testIsValid_InvalidCharactersSpace() {
        assertFalse(validator.isValid("abc 123", context));
    }

    @Test
    void testIsValid_InvalidCharactersSpecial() {
        assertFalse(validator.isValid("abc@123", context));
    }

    @Test
    void testIsValid_TooShort() {
        when(validUuid.minSize()).thenReturn(5);
        ValidUuidConstraintValidator v = new ValidUuidConstraintValidator();
        v.initialize(validUuid);
        assertFalse(v.isValid("abc", context));
    }

    @Test
    void testIsValid_TooLong() {
        when(validUuid.maxSize()).thenReturn(5);
        ValidUuidConstraintValidator v = new ValidUuidConstraintValidator();
        v.initialize(validUuid);
        assertFalse(v.isValid("abcdefgh", context));
    }

    @Test
    void testIsValid_ExactlyMinSize() {
        when(validUuid.minSize()).thenReturn(3);
        ValidUuidConstraintValidator v = new ValidUuidConstraintValidator();
        v.initialize(validUuid);
        assertTrue(v.isValid("abc", context));
    }

    @Test
    void testIsValid_ExactlyMaxSize() {
        when(validUuid.maxSize()).thenReturn(3);
        ValidUuidConstraintValidator v = new ValidUuidConstraintValidator();
        v.initialize(validUuid);
        assertTrue(v.isValid("abc", context));
    }

    @Test
    void testIsValid_SingleCharacter() {
        assertTrue(validator.isValid("a", context));
    }
}
