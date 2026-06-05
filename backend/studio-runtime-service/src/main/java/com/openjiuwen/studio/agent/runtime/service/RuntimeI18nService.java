/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.ErrorInfo;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.ErrorEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenAgentEventData;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventData;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
public class RuntimeI18nService {
    private static final String SYSTEM_ERROR_CODE = "999999";

    private static final String TOKEN_USE_UP_ERROR_CODE = "openjiuwen.02501051";

    private static final String INPUTS_NOT_MATCH_DEFINITION_MSG
        = "the inputs structure of the components does not match its definition";

    private static final String INPUTS_NOT_MATCH_DEFINITION_ERROR = "800001";

    private static final String JIUWEN = "(?i)(九问|jiuwen)";

    @Autowired
    @Qualifier("runtimeMessageResource")
    private MessageSource messageSource;

    @Autowired
    private I18nUtil i18nUtil;

    /**
     * 异常事件封装
     *
     * @param workflowId 工作流ID
     * @param eventData 九问error事件
     * @return 封装后error事件
     */
    public ErrorEvent createWorkflowErrorEvent(String workflowId, JiuwenEventData eventData) {
        ErrorEvent errorEvent = new ErrorEvent();
        errorEvent.setNodeId(eventData.getNodeId());
        errorEvent.setNodeType(NodeType.fromJiuwen(eventData.getNodeType()).getEiType());
        errorEvent.setNodeName(eventData.getNodeName());
        errorEvent.setCode(eventData.getCode().toString());
        errorEvent.setErrorCode("openjiuwen." + eventData.getCode().toString());

        String msg = eventData.getMessage() + " node:" + errorEvent.getNodeName() + " nodeType:"
            + errorEvent.getNodeType();
        errorEvent.setMessage(msg);
        errorEvent.setWorkflowId(workflowId);
        errorEvent.setWorkflowName(eventData.getWorkflowName());

        return setErrorMessages(eventData.getCode(), errorEvent);
    }

    private @NotNull ErrorEvent setErrorMessages(Integer code, ErrorEvent errorEvent) {
        if (errorEvent.getMessage().contains(TOKEN_USE_UP_ERROR_CODE)) {
            errorEvent.setErrorCode(TOKEN_USE_UP_ERROR_CODE);
            errorEvent.setErrorMsg(i18nUtil.getMessage(TOKEN_USE_UP_ERROR_CODE));
            errorEvent.setErrorSuggestion(i18nUtil.getMessage(TOKEN_USE_UP_ERROR_CODE + ".suggestion"));
            errorEvent.setErrorReason(i18nUtil.getMessage(TOKEN_USE_UP_ERROR_CODE + ".reason"));
        } else if (errorEvent.getMessage().contains(INPUTS_NOT_MATCH_DEFINITION_MSG)) {
            Locale locale = LanguageUtils.getLanguageLocale();
            errorEvent.setErrorMsg(safeGetMessage(INPUTS_NOT_MATCH_DEFINITION_ERROR, locale));
            errorEvent.setErrorSuggestion(safeGetMessage(INPUTS_NOT_MATCH_DEFINITION_ERROR + ".suggestion", locale));
            errorEvent.setErrorReason(safeGetMessage(INPUTS_NOT_MATCH_DEFINITION_ERROR + ".reason", locale));
        } else {
            Locale locale = LanguageUtils.getLanguageLocale();
            String errorCode = code == null ? SYSTEM_ERROR_CODE : code.toString();
            errorEvent.setErrorMsg(safeGetMessage(errorCode, locale));
            errorEvent.setErrorSuggestion(safeGetMessage(errorCode + ".suggestion", locale));
            errorEvent.setErrorReason(safeGetMessage(errorCode + ".reason", locale));
        }
        return errorEvent;
    }

    public ErrorEvent createErrorEvent(JiuwenAgentEventData eventData) {
        ErrorEvent errorEvent = new ErrorEvent();
        errorEvent.setNodeId(eventData.getNodeId());
        errorEvent.setNodeType(NodeType.fromJiuwen(eventData.getNodeType()).getEiType());
        errorEvent.setNodeName(eventData.getNodeName());
        errorEvent.setCode(eventData.getCode().toString());
        errorEvent.setErrorCode("openjiuwen." + eventData.getCode().toString());
        String msg = eventData.getMessage() + " node:" + errorEvent.getNodeName() + " nodeType:"
            + errorEvent.getNodeType();
        if (StringUtils.isNotBlank(msg)) {
            msg = msg.replaceAll(JIUWEN, "Runtime");
        }
        errorEvent.setMessage(msg);
        errorEvent.setWorkflowId(eventData.getWorkflowId());
        errorEvent.setWorkflowName(eventData.getWorkflowName());

        return setErrorMessages(eventData.getCode(), errorEvent);
    }

    private String safeGetMessage(String key, Locale locale) {
        try {
            return messageSource.getMessage(key, new Object[] {0}, locale);
        } catch (Exception e) {
            return "";
        }
    }

    private ErrorEvent createWorkflowErrorEvent(String code, String message) {
        ErrorEvent errorEvent = new ErrorEvent();
        errorEvent.setCode(code);
        errorEvent.setErrorCode("openjiuwen." + code);
        if (StringUtils.isNotBlank(message)) {
            message = message.replaceAll(JIUWEN, "Runtime");
        }
        errorEvent.setMessage(message);

        Locale locale = LanguageUtils.getLanguageLocale();
        String errorCode = StringUtils.isEmpty(code) ? SYSTEM_ERROR_CODE : code;
        errorEvent.setErrorMsg(safeGetMessage(errorCode, locale));
        errorEvent.setErrorSuggestion(safeGetMessage(errorCode + ".suggestion", locale));
        errorEvent.setErrorReason(safeGetMessage(errorCode + ".reason", locale));
        return errorEvent;
    }

    public ErrorEvent createErrorEvent(Throwable t, Response response) {
        if (response != null) {
            String errorMessage = null;
            String errorCode = null;
            try {
                JSONObject error = null;
                if (response.body() != null) {
                    String responseErrorMessage = response.body().string();
                    log.error("invoke jiuwen original code:{} ; error:{}", response.code(), responseErrorMessage);
                    error = JSONObject.parseObject(responseErrorMessage);
                }
                if (error != null) {
                    errorCode = error.getString(Constant.Jiuwen.ERROR_CODE_FIELD);
                    errorMessage = error.getString(Constant.Jiuwen.ERROR_MSG_FIELD);
                } else {
                    log.warn("unknown jiuwen error code {}", response.code());
                }
            } catch (JSONException e) {
                log.error("get jiuwen error failed.");
            } catch (Exception e) {
                log.error("get jiuwen error failed.", e);
            }
            return createWorkflowErrorEvent(errorCode, errorMessage);
        } else if (t != null) {
            if (t instanceof AgentStudioException) {
                return createErrEvent((AgentStudioException) t);
            }
            if (t instanceof java.net.ConnectException) {
                return createErrEvent((java.net.ConnectException) t, StudioError.JIUWEN_CONNECTION_EXCEPTION);
            }
            if (t instanceof java.net.SocketTimeoutException) {
                return createErrEvent((java.net.SocketTimeoutException) t, StudioError.JIUWEN_CONNECTION_TIMEOUT);
            }
            return createErrEvent(t, StudioError.JIUWEN_CONNECTION_EXCEPTION);
        }
        return createWorkflowErrorEvent("999999", "unknown error");
    }

    public ErrorEvent createErrEvent(Throwable studioException, StudioError error) {
        String code = "openjiuwen." + error.getModule().getSubCode() + error.getCode();
        ErrorInfo errorInfo = i18nUtil.getMessage(new AgentStudioException(error));
        return new ErrorEvent().setCode(code)
            .setErrorCode(code)
            .setMessage(studioException.getMessage())
            .setErrorMsg(errorInfo.getMessage())
            .setErrorReason(errorInfo.getReason())
            .setErrorSuggestion(errorInfo.getSuggestion());
    }

    private ErrorEvent createErrEvent(AgentStudioException studioException) {
        String code = "openjiuwen." + studioException.getErrorCode().getModule().getSubCode()
            + studioException.getErrorCode().getCode();
        ErrorInfo errorInfo = i18nUtil.getMessage(studioException);
        return new ErrorEvent().setCode(code)
            .setErrorCode(code)
            .setMessage(errorInfo.getMessage())
            .setErrorMsg(errorInfo.getMessage())
            .setErrorReason(errorInfo.getReason())
            .setErrorSuggestion(errorInfo.getSuggestion());
    }
}
