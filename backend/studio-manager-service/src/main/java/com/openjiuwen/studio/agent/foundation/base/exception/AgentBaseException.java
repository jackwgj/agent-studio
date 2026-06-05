/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.exception;

import com.openjiuwen.studio.agent.foundation.i18n.I18nUtils;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 功能描述 studio-agent-manager异常类
 *
 */
@Getter
@Setter
public class AgentBaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private ErrorCode errorCode;

    /**
     * 页面错误信息
     */
    private String errorMsg;

    /**
     * 页面错误信息原因
     */
    private String errorReason;

    /**
     * 页面错误信息建议
     */
    private String errorSuggestion;

    private List<ErrorDetail> details;

    /**
     * AgentManagerException handling constructor
     *
     * @param errorCode errorCode
     */
    public AgentBaseException(ErrorCode errorCode) {
        super(I18nUtils.getMessage(errorCode.getMsg()));
        this.errorCode = errorCode;
        this.errorMsg = I18nUtils.getMessage(errorCode.getCode());
        this.errorReason = I18nUtils.getMessage(errorCode.getCode() + ".reason");
        this.errorSuggestion = I18nUtils.getMessage(errorCode.getCode() + ".suggestion");
    }

    /**
     * AgentManagerException handling constructor
     *
     * @param errorCode errorCode
     * @param details 调用接口返回的错误信息
     */
    public AgentBaseException(ErrorCode errorCode, List<ErrorDetail> details) {
        super(I18nUtils.getMessage(errorCode.getMsg()));
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * AgentManagerException handling constructor
     *
     * @param errorCode errorCode
     */
    public AgentBaseException(ErrorCode errorCode, Exception e) {
        super(I18nUtils.getMessage(errorCode.getMsg()), e);
        this.errorCode = errorCode;
        this.errorMsg = I18nUtils.getMessage(errorCode.getCode());
        this.errorReason = I18nUtils.getMessage(errorCode.getCode() + ".reason");
        this.errorSuggestion = I18nUtils.getMessage(errorCode.getCode() + ".suggestion");
    }

    /**
     * AgentManagerException handling constructor
     *
     * @param errorCode errorCode
     * @param msg message
     * @param details 调用接口返回的错误信息
     */
    public AgentBaseException(ErrorCode errorCode, String msg, List<ErrorDetail> details) {
        super(StringUtils.isBlank(msg) ? errorCode.getMsg() : msg);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * AgentManagerException handling constructor
     *
     * @param errorCode errorCode
     * @param msg message
     */
    public AgentBaseException(ErrorCode errorCode, String msg, Exception e) {
        super(StringUtils.isBlank(msg) ? errorCode.getMsg() : msg, e);
        this.errorCode = errorCode;
        this.errorMsg = I18nUtils.getMessage(errorCode.getCode());
        this.errorReason = I18nUtils.getMessage(errorCode.getCode() + ".reason");
        this.errorSuggestion = I18nUtils.getMessage(errorCode.getCode() + ".suggestion");
    }

    /**
     * AgentManagerException handling constructor
     *
     * @param message message
     */
    public AgentBaseException(String message) {
        super(message);
        this.errorCode = ErrorCode.SERVER_INTERNAL_ERROR;
        this.errorMsg = I18nUtils.getMessage(ErrorCode.SERVER_INTERNAL_ERROR.getCode());
        this.errorReason = I18nUtils.getMessage(ErrorCode.SERVER_INTERNAL_ERROR.getCode() + ".reason");
        this.errorSuggestion = I18nUtils.getMessage(ErrorCode.SERVER_INTERNAL_ERROR.getCode() + ".suggestion");
    }

    public AgentBaseException(ErrorCode errorCode, Object... params) {
        super(I18nUtils.getMessage(errorCode.getMsg()));
        this.errorCode = errorCode;
        this.errorMsg = I18nUtils.getMessage(errorCode.getCode());
        this.errorReason = I18nUtils.getMessage(errorCode.getCode() + ".reason", params);
        this.errorSuggestion = I18nUtils.getMessage(errorCode.getCode() + ".suggestion", params);
    }
}
