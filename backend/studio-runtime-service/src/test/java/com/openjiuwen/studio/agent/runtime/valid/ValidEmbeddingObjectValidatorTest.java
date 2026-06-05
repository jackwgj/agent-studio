/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.valid;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openjiuwen.studio.agent.common.validator.ValidEmbeddingObjectValidator;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class ValidEmbeddingObjectValidatorTest {

    @Test
    public void test() {
        ConstraintValidatorContext.ConstraintViolationBuilder builder = Mockito.mock(
            ConstraintValidatorContext.ConstraintViolationBuilder.class);

        ValidEmbeddingObjectValidator
            validator = new ValidEmbeddingObjectValidator();
        validator.initialize(null);

        ConstraintValidatorContext context = new ConstraintValidatorContext() {
            @Override
            public void disableDefaultConstraintViolation() {
            }

            @Override
            public String getDefaultConstraintMessageTemplate() {
                return "";
            }

            @Override
            public ClockProvider getClockProvider() {
                return null;
            }

            @Override
            public ConstraintViolationBuilder buildConstraintViolationWithTemplate(String s) {
                return builder;
            }

            @Override
            public <T> T unwrap(Class<T> aClass) {
                return null;
            }
        };

        Mockito.when(builder.addConstraintViolation()).thenReturn(context);

        boolean valid = validator.isValid(null, context);
        assertFalse(valid);

        char[] chars = new char[26 * 1024 * 1024];
        Arrays.fill(chars, 'a');
        valid = validator.isValid(new String(chars), context);
        assertFalse(valid);

        valid = validator.isValid(new HashMap<>(), context);
        assertFalse(valid);

        valid = validator.isValid(Collections.singletonList(new String(chars)), context);
        assertFalse(valid);

        valid = validator.isValid(Arrays.asList(new String[1001]), context);
        assertFalse(valid);

        valid = validator.isValid(new String(chars, 0, 25 * 1024 * 1024), context);
        assertTrue(valid);

        valid = validator.isValid(Collections.singletonList(new String(chars, 0, 25 * 1024 * 1024)), context);
        assertTrue(valid);
    }
}
