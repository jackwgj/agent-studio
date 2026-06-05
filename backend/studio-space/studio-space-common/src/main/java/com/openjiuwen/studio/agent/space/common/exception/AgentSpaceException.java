/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.exception;

import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import lombok.Getter;

import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.regex.Matcher;

@Getter
public class AgentSpaceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final String errorDesc;

    private HttpStatus httpStatus;

    public AgentSpaceException(String errorCode, String errorDesc) {
        super(errorDesc);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public AgentSpaceException(String errorCode, String errorDesc, HttpStatus httpStatus) {
        super(errorDesc);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
        this.httpStatus = httpStatus;
    }

    public AgentSpaceException(AgentSpaceErrorCodes errorCode, Object... arguments) {
        super(formatString(errorCode.errorMsg(), arguments));
        this.errorCode = errorCode.errorCode();
        this.errorDesc = formatString(errorCode.errorMsg(), arguments);
    }

    public AgentSpaceException(String errorDesc) {
        super(errorDesc);
        this.errorCode = AgentSpaceErrorCodes.SYSTEM_ERROR.errorCode();
        this.errorDesc = errorDesc;
    }

    public AgentSpaceException(AgentSpaceErrorCodes errorCode) {
        super(errorCode.errorMsg());
        this.errorCode = errorCode.errorCode();
        this.errorDesc = errorCode.errorMsg();
        this.httpStatus = errorCode.httpStatus();
    }

    private static String formatString(String template, Object... args) {
        if (StringUtil.isEmptyString(template)) {
            return "";
        }
        for (Object arg : args) {
            template = template.replaceFirst("\\{\\}", Matcher.quoteReplacement(arg.toString()));
        }

        template = template.replaceAll("\\{\\}", " ");
        return template.trim();
    }
}
