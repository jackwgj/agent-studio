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
class ValidDependencyNameConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ValidDependencyName validDependencyName;

    private ValidDependencyNameConstraintValidator validator;

    @BeforeEach
    void setUp() {
        when(validDependencyName.emptyAble()).thenReturn(true);
        validator = new ValidDependencyNameConstraintValidator();
        validator.initialize(validDependencyName);
    }

    @Test
    void testIsValid_ValidName() {
        assertTrue(validator.isValid("myPackage", context));
    }

    @Test
    void testIsValid_ValidNameWithUnderscore() {
        assertTrue(validator.isValid("my_package", context));
    }

    @Test
    void testIsValid_ValidNameWithNumbers() {
        assertTrue(validator.isValid("package123", context));
    }

    @Test
    void testIsValid_MinLength() {
        assertTrue(validator.isValid("ab", context));
    }

    @Test
    void testIsValid_MaxLength() {
        assertTrue(validator.isValid("a" + "b".repeat(31), context));
    }

    @Test
    void testIsValid_TooShort() {
        assertFalse(validator.isValid("a", context));
    }

    @Test
    void testIsValid_TooLong() {
        assertFalse(validator.isValid("a" + "b".repeat(32), context));
    }

    @Test
    void testIsValid_StartsWithNumber() {
        assertFalse(validator.isValid("1package", context));
    }

    @Test
    void testIsValid_StartsWithUnderscore() {
        assertFalse(validator.isValid("_package", context));
    }

    @Test
    void testIsValid_ContainsHyphen() {
        assertFalse(validator.isValid("my-package", context));
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
        when(validDependencyName.emptyAble()).thenReturn(false);
        ValidDependencyNameConstraintValidator v = new ValidDependencyNameConstraintValidator();
        v.initialize(validDependencyName);
        assertFalse(v.isValid("", context));
    }

    @Test
    void testIsValid_NullValueNotAble() {
        when(validDependencyName.emptyAble()).thenReturn(false);
        ValidDependencyNameConstraintValidator v = new ValidDependencyNameConstraintValidator();
        v.initialize(validDependencyName);
        assertFalse(v.isValid(null, context));
    }

    @Test
    void testIsValid_UpperCaseStart() {
        assertTrue(validator.isValid("MyPackage", context));
    }
}
