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
class ValidEnumValueConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ValidEnumValue validEnumValue;

    private ValidEnumValueConstraintValidator validator;

    @BeforeEach
    void setUp() {
        when(validEnumValue.emptyAble()).thenReturn(true);
        when(validEnumValue.ignoreCase()).thenReturn(false);
        when(validEnumValue.values()).thenReturn(new String[]{"VALUE_A", "VALUE_B", "VALUE_C"});
        validator = new ValidEnumValueConstraintValidator();
        validator.initialize(validEnumValue);
    }

    @Test
    void testIsValid_MatchingValue() {
        assertTrue(validator.isValid("VALUE_A", context));
    }

    @Test
    void testIsValid_MatchingValueB() {
        assertTrue(validator.isValid("VALUE_B", context));
    }

    @Test
    void testIsValid_MatchingValueC() {
        assertTrue(validator.isValid("VALUE_C", context));
    }

    @Test
    void testIsValid_NonMatchingValue() {
        assertFalse(validator.isValid("VALUE_D", context));
    }

    @Test
    void testIsValid_CaseSensitiveMismatch() {
        assertFalse(validator.isValid("value_a", context));
    }

    @Test
    void testIsValid_CaseInsensitiveMatch() {
        when(validEnumValue.ignoreCase()).thenReturn(true);
        ValidEnumValueConstraintValidator v = new ValidEnumValueConstraintValidator();
        v.initialize(validEnumValue);
        assertTrue(v.isValid("value_a", context));
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
        when(validEnumValue.emptyAble()).thenReturn(false);
        ValidEnumValueConstraintValidator v = new ValidEnumValueConstraintValidator();
        v.initialize(validEnumValue);
        assertFalse(v.isValid("", context));
    }

    @Test
    void testIsValid_NullValueNotAble() {
        when(validEnumValue.emptyAble()).thenReturn(false);
        ValidEnumValueConstraintValidator v = new ValidEnumValueConstraintValidator();
        v.initialize(validEnumValue);
        assertFalse(v.isValid(null, context));
    }

    @Test
    void testIsValid_PartialMatch() {
        assertFalse(validator.isValid("VALUE", context));
    }
}
