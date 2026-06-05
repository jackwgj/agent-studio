/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.annotation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.annotation.ValidKnowledgeBaseName;
import com.openjiuwen.studio.agent.common.annotation.ValidKnowledgeBaseNameConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class ValidKnowledgeBaseNameConstraintValidatorTest {
    @InjectMocks
    private ValidKnowledgeBaseNameConstraintValidator validKnowledgeBaseNameConstraintValidator;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_initialize_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            ValidKnowledgeBaseName constraintAnnotation = mock(ValidKnowledgeBaseName.class,
                Answers.RETURNS_DEEP_STUBS);
            when(constraintAnnotation.canBeEmpty()).thenReturn(
                true);

            // When
            validKnowledgeBaseNameConstraintValidator.initialize(constraintAnnotation);
        });
    }


    @Test
    void test_isValid_should_return_true() throws Exception {
        // Given
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

        // When
        boolean result = validKnowledgeBaseNameConstraintValidator.isValid("aaa", context);

        // Then
        assertTrue(result);
    }

    @Test
    void test_isValid_should_return_true2() throws Exception {
        // Given
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

        // When
        boolean result = validKnowledgeBaseNameConstraintValidator.isValid("not_empty", context);

        // Then
        assertTrue(result);
    }


    @Test
    void test_isValid_should_return_true3() throws Exception {

        // When
        boolean result = validKnowledgeBaseNameConstraintValidator.isValid("aaa", null);

        // Then
        assertTrue(result);
    }
}
