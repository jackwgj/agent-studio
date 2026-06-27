/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.exception;

import com.openjiuwen.studio.agent.common.dto.ErrorDetail;
import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.ErrorInfo;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 功能描述 全局异常捕获类
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    final I18nUtil i18nUtil;

    public GlobalExceptionHandler(I18nUtil i18nUtil) {
        this.i18nUtil = i18nUtil;
    }

    /**
     * 处理方法参数校验异常使用
     *
     * @param exception MethodArgumentNotValidException
     * @return Return result
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorRsp> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String errMessage = null;

        // 获取校验异常参数
        BindingResult bindingResult = exception.getBindingResult();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errMessage = fieldError.getField() + fieldError.getDefaultMessage();
        }
        log.error("throw MethodArgumentNotValidException: {}", errMessage);
        ErrorRsp errorRsp = new ErrorRsp().setErrorCode(StudioError.METHOD_ARGUMENT_NOT_VALID.getCode())
            .setErrorMsg(errMessage);
        return new ResponseEntity<>(errorRsp, StudioError.STATIC_RESOURCE_NOT_EXIST.getHttpStatus());
    }

    /**
     * 处理 @PathVariable / @RequestParam 上的校验注解（@Size, @Pattern 等）失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<ErrorRsp> handleConstraintViolationException(ConstraintViolationException exception) {
        StringBuilder errMsg = new StringBuilder();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String paramName = violation.getPropertyPath().toString();
            if (paramName.contains(".")) {
                paramName = paramName.substring(paramName.lastIndexOf('.') + 1);
            }
            errMsg.append(paramName).append(violation.getMessage()).append("; ");
        }
        log.error("throw ConstraintViolationException: {}", errMsg);
        ErrorRsp errorRsp = new ErrorRsp().setErrorCode(StudioError.METHOD_ARGUMENT_NOT_VALID.getFullCode())
            .setErrorMsg(errMsg.toString());
        return new ResponseEntity<>(errorRsp, StudioError.METHOD_ARGUMENT_NOT_VALID.getHttpStatus());
    }

    /**
     * NoResourceFoundException handling injection
     *
     * @param exception NoResourceFoundException
     * @return Return result
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorRsp> handleNoResourceFoundException(Exception exception) {
        log.error("exception: {}", exception.getMessage());
        ErrorRsp errorRsp = new ErrorRsp().setErrorCode(String.valueOf(StudioError.STATIC_RESOURCE_NOT_EXIST.getCode()))
            .setErrorMsg(exception.getMessage());
        return new ResponseEntity<>(errorRsp, ResponseModel.num2HttpStatus(
            Integer.toString(StudioError.STATIC_RESOURCE_NOT_EXIST.getHttpStatus().ordinal())));
    }

    /**
     * 处理AgentStudioException，根据ErrorCode从i18n读取对应的错误信息
     *
     * @param agentStudioException AgentStudioException
     * @return ResponseEntity<ErrorRsp>
     */
    @ExceptionHandler(AgentStudioException.class)
    @ResponseBody
    public ResponseEntity<ErrorRsp> handleAgentManagerException(AgentStudioException agentStudioException) {
        StudioError errorCode = agentStudioException.getErrorCode();
        String code = "openjiuwen." + errorCode.getModule().getSubCode() + errorCode.getCode();
        ErrorInfo errorInfo = i18nUtil.getMessage(agentStudioException);

        log.error("handle AgentStudioException, code = {}", code, agentStudioException);
        ErrorRsp rsp = new ErrorRsp().setErrorCode(code).setErrorMsg(errorInfo.getMessage()).setErrorReason(errorInfo
            .getReason()).setErrorSuggestion(errorInfo.getSuggestion());
        if (!CollectionUtils.isEmpty(agentStudioException.getDetails())) {
            rsp.setDetails(agentStudioException.getDetails()
                .stream()
                .map(msg -> new ErrorDetail().setErrorMsg(msg))
                .collect(Collectors.toList()));
        }
        return new ResponseEntity<>(rsp, agentStudioException.getErrorCode().getHttpStatus());
    }

    /**
     * api入参校验异常
     * @param exception
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ErrorRsp> handleNotReadableException(Exception exception) {
        log.error("NotReadableException: {}", exception.getMessage());
        ErrorRsp errorRsp = new ErrorRsp().setErrorCode(StudioError.METHOD_ARGUMENT_NOT_VALID.getFullCode())
            .setErrorMsg(i18nUtil.getMessage(StudioError.METHOD_ARGUMENT_NOT_VALID));
        return new ResponseEntity<>(errorRsp, ResponseModel.num2HttpStatus(
            Integer.toString(StudioError.METHOD_ARGUMENT_NOT_VALID.getHttpStatus().ordinal())));
    }
}
