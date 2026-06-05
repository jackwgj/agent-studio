/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 功能描述
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class ValidEmbeddingObjectValidatorTest {
    @InjectMocks
    private ValidEmbeddingObjectValidator validEmbeddingObjectValidator;

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
    public void test_isValid_with_null_object() throws Exception {
        // Given
        Object object = null;
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        // When
        boolean result = validEmbeddingObjectValidator.isValid(object, context);

        // Then
        assertFalse(result);
    }

    @Test
    public void test_isValid_with_valid_string_list() throws Exception {
        // Given
        List<String> validStringList = Arrays.asList("small", "strings");
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        // When
        boolean result = validEmbeddingObjectValidator.isValid(validStringList, context);

        // Then
        assertTrue(result);
    }

    @Test
    public void test_isValid_with_large_string_list() throws Exception {
        // Given
        List<String> largeStringList = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            largeStringList.add("small");
        }
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintViolationBuilder builder = mock(ConstraintViolationBuilder.class);

        // When
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        boolean result = validEmbeddingObjectValidator.isValid(largeStringList, context);

        // Then
        assertFalse(result);
    }

    @Test
    public void test_isValid_with_invalid_type() throws Exception {
        // Given
        Object invalidObject = new Object();
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintViolationBuilder builder = mock(ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        // When
        boolean result = validEmbeddingObjectValidator.isValid(invalidObject, context);

        // Then
        assertFalse(result);
    }

    @Test
    void test_isValid_should_return_true() throws Exception {
        // Given
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class,
            Answers.RETURNS_DEEP_STUBS);
        when(context.buildConstraintViolationWithTemplate(eq("Too many input parameters."))
            .addConstraintViolation()).thenReturn(constraintValidatorContext);
        ConstraintValidatorContext constraintValidatorContext1 = mock(ConstraintValidatorContext.class,
            Answers.RETURNS_DEEP_STUBS);
        when(context.buildConstraintViolationWithTemplate(eq("The value of input is too large."))
            .addConstraintViolation()).thenReturn(constraintValidatorContext1);
        ConstraintValidatorContext constraintValidatorContext2 = mock(ConstraintValidatorContext.class,
            Answers.RETURNS_DEEP_STUBS);
        when(context.buildConstraintViolationWithTemplate(eq("The value of input is not string or list!"))
            .addConstraintViolation()).thenReturn(constraintValidatorContext2);
        ConstraintViolationBuilder constraintViolationBuilder = mock(ConstraintViolationBuilder.class,
            Answers.RETURNS_DEEP_STUBS);
        when(context.buildConstraintViolationWithTemplate(eq("The value of input is too large."))).thenReturn(
            constraintViolationBuilder);

        // When
        boolean result = validEmbeddingObjectValidator.isValid(null, context);

        // Then
        assertFalse(result);
    }

    @Test
    void test_isValid_should_return_true1() throws Exception {
        // Given
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class,
            Answers.RETURNS_DEEP_STUBS);
        when(context.buildConstraintViolationWithTemplate(eq("Too many input parameters."))
            .addConstraintViolation()).thenReturn(constraintValidatorContext);
        ConstraintValidatorContext constraintValidatorContext1 = mock(ConstraintValidatorContext.class,
            Answers.RETURNS_DEEP_STUBS);
        when(context.buildConstraintViolationWithTemplate(eq("The value of input is too large."))
            .addConstraintViolation()).thenReturn(constraintValidatorContext1);
        ConstraintValidatorContext constraintValidatorContext2 = mock(ConstraintValidatorContext.class,
            Answers.RETURNS_DEEP_STUBS);
        when(context.buildConstraintViolationWithTemplate(eq("The value of input is not string or list!"))
            .addConstraintViolation()).thenReturn(constraintValidatorContext2);
        ConstraintViolationBuilder constraintViolationBuilder = mock(ConstraintViolationBuilder.class,
            Answers.RETURNS_DEEP_STUBS);
        when(context.buildConstraintViolationWithTemplate(eq("The value of input is too large."))).thenReturn(
            constraintViolationBuilder);

        // When
        boolean result = validEmbeddingObjectValidator.isValid("not_empty", context);

        // Then
        assertTrue(result);
    }

    @Test
    public void test_isValid_should_not_throw_exception() throws Exception {
        // Given
        Object object = new Object();
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintViolationBuilder builder = mock(ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        // When
        boolean result = validEmbeddingObjectValidator.isValid(object, context);

        // Then
        assertFalse(result);
    }

    @Test
    public void test_isValid_with_list_at_size_limit() throws Exception {
        // Given
        List<String> listAtLimit = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            listAtLimit.add("test");
        }
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        // When
        boolean result = validEmbeddingObjectValidator.isValid(listAtLimit, context);

        // Then
        assertTrue(result);
    }

    @Test
    public void test_isValid_with_empty_string() throws Exception {
        // Given
        String emptyString = "";
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        // When
        boolean result = validEmbeddingObjectValidator.isValid(emptyString, context);

        // Then
        assertTrue(result);
    }

    @Test
    public void test_isValid_with_single_element_list() throws Exception {
        // Given
        List<String> singleElementList = List.of("test");
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        // When
        boolean result = validEmbeddingObjectValidator.isValid(singleElementList, context);

        // Then
        assertTrue(result);
    }
}
