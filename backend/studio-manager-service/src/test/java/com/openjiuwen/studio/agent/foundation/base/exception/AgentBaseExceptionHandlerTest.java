/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.foundation.i18n.I18nUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class AgentBaseExceptionHandlerTest {

    @Mock
    private I18nUtils i18nUtils;

    @InjectMocks
    private com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseExceptionHandler agentBaseExceptionHandler;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testHandleException() {
        try (MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS)) {
            mockedStaticI18nUtils.when(() -> I18nUtils.getMessage(eq(ErrorCode.SERVER_INTERNAL_ERROR.getCode())))
                .thenReturn("not_empty");
            Exception exception = new Exception("Test exception");
            when(i18nUtils.getMessage(eq(ErrorCode.SERVER_INTERNAL_ERROR.getCode()), anyString())).thenReturn(
                "An unexpected error occurred, please contact the administrator.");
            ResponseEntity<ErrorResponse> responseEntity = agentBaseExceptionHandler.handleException(exception);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
            ErrorResponse errorResponse = responseEntity.getBody();
            assertNotNull(errorResponse);
        }
    }
}
