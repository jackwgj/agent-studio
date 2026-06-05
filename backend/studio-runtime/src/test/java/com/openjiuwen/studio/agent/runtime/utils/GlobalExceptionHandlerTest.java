/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.runtime.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;

/**
 * 功能描述
 *
 */
@MockitoSettings(strictness = Strictness.LENIENT)
public class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler globalExceptionHandler;

    private AutoCloseable mockitoCloseable;

    private I18nUtil i18nUtil;

    @BeforeEach
    public void setUp() throws Exception {
        i18nUtil = Mockito.mock(I18nUtil.class);
        globalExceptionHandler = new GlobalExceptionHandler(i18nUtil);
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_handleMethodArgumentNotValidException_should_succeed_when_test_data_combination() {
        // setup
        MethodParameter original = mock(MethodParameter.class);
        MethodParameter parameter = new MethodParameter(original);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors())
            .thenReturn(Collections.singletonList(new FieldError("objectA", "fieldA", " field error")));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        // run the test
        ResponseEntity<ErrorRsp> result = globalExceptionHandler.handleMethodArgumentNotValidException(exception);

        // verify the results
        assertEquals("fieldA field error", result.getBody().getErrorMsg());
    }

    @Test
    void test_handleNoResourceFoundException_should_succeed_when_test_data_combination() {
        // setup
        NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "fake_resource_path");

        // run the test
        ResponseEntity<ErrorRsp> result = globalExceptionHandler.handleNoResourceFoundException(exception);

        // verify the results
        assertEquals("No static resource fake_resource_path.", result.getBody().getErrorMsg());
    }
}
