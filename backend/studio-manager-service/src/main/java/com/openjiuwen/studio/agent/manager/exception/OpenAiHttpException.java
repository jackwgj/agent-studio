/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.io.Serial;

@Slf4j
public class OpenAiHttpException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 656826109626533052L;

    @Getter
    private HttpStatusCode statusCode;

    @Getter
    private final String errorResponse;

    public OpenAiHttpException(int statusCode, String errorResponse) {
        try {
            this.statusCode = HttpStatusCode.valueOf(statusCode);
        } catch (Exception e) {
            log.warn("OpenAiHttpException status is invalid. {}", statusCode);
            this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        this.errorResponse = errorResponse;
    }
}
