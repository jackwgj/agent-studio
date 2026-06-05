/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.exception;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.foundation.i18n.I18nUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.sql.SQLException;

/**
 * 功能描述 异常捕获类
 *
 * @since 2024-08-05
 */
@Slf4j
@ControllerAdvice
public class AgentBaseExceptionHandler {

    /**
     * Exception handling AgentBaseException
     *
     * @param agentBaseException AgentBaseException
     * @return Return result
     */
    @ExceptionHandler(com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleAgentBaseException(AgentBaseException agentBaseException) {
        log.error("throw AgentBaseException exception: {}", agentBaseException.getMessage(), agentBaseException);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(agentBaseException.getErrorCode().getCode());
        errorResponse.setErrorMsg(agentBaseException.getErrorMsg());
        errorResponse.setErrorReason(agentBaseException.getErrorReason());
        errorResponse.setErrorSuggestion(agentBaseException.getErrorSuggestion());
        return new ResponseEntity<>(errorResponse,
            ResponseModel.num2HttpStatus(Integer.toString(agentBaseException.getErrorCode().getHttpCode())));
    }

    /**
     * Processor exception
     *
     * @param exception Unknown exception
     * @return Return result
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("exception: {}", exception.getMessage(), exception);
        return handleAgentBaseException(new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR));
    }

    /**
     * Processor exception
     *
     * @param exception Unknown exception
     * @return Return result
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleSqlErrorException(BadSqlGrammarException exception) {
        log.error("exception: {}", exception.getMessage(), exception);
        return handleAgentBaseException(new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, "Sql execute failed!"));
    }

    /**
     * 处理方法参数校验异常使用
     *
     * @param exception MethodArgumentNotValidException
     * @return Return result
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException exception) {
        String errMessage = null;
        String errField = null;

        // 获取校验异常参数
        BindingResult bindingResult = exception.getBindingResult();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errField = fieldError.getField();
            errMessage = fieldError.getField() + fieldError.getDefaultMessage();
        }
        log.error("throw MethodArgumentNotValidException: {}", exception.getMessage(), exception);
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode(ErrorCode.METHOD_ARGUMENT_INVALID.getCode())
            .errorMsg(I18nUtils.getMessage(ErrorCode.METHOD_ARGUMENT_INVALID.getCode()))
            .errorReason(I18nUtils.getMessage(ErrorCode.METHOD_ARGUMENT_INVALID.getCode() + ".reason", errMessage))
            .errorSuggestion(
                I18nUtils.getMessage(ErrorCode.METHOD_ARGUMENT_INVALID.getCode() + ".suggestion", errField))
            .build();
        return new ResponseEntity<>(errorResponse,
            ResponseModel.num2HttpStatus(Integer.toString(exception.getStatusCode().value())));
    }

    /**
     * Processor exception
     *
     * @param exception 流式接口超时异常
     * @return Return result
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(AsyncRequestTimeoutException exception) {
        log.error("throw AsyncRequestTimeoutException: {}", exception.getMessage(), exception);
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode(ErrorCode.STREAM_INTERFACE_EXECUTE_TIMEOUT.getCode())
            .errorMsg(I18nUtils.getMessage(ErrorCode.STREAM_INTERFACE_EXECUTE_TIMEOUT.getCode()))
            .errorReason(I18nUtils.getMessage(ErrorCode.STREAM_INTERFACE_EXECUTE_TIMEOUT.getCode() + ".reason"))
            .errorSuggestion(I18nUtils.getMessage(ErrorCode.STREAM_INTERFACE_EXECUTE_TIMEOUT.getCode() + ".suggestion"))
            .build();
        return new ResponseEntity<>(errorResponse,
            ResponseModel.num2HttpStatus(Integer.toString(ErrorCode.STREAM_INTERFACE_EXECUTE_TIMEOUT.getHttpCode())));
    }

    /**
     * Processor exception
     *
     * @param exception 数据库执行异常（防止接口报错打印暴露数据库信息）
     * @return Return result
     */
    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(SQLException exception) {
        log.error("throw SQLException: {}", exception.getMessage(), exception);
        return handleAgentBaseException(new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, "Sql execute failed!"));
    }
}
