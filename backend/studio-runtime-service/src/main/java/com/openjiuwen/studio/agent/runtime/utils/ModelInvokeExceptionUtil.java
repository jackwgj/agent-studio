/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import java.util.Collections;
import java.util.Locale;

public class ModelInvokeExceptionUtil {
    private ModelInvokeExceptionUtil() {
    }


    public static AgentStudioException modelArtsExceptionMapping(int statusCode, String message) {
        if (401 == statusCode) {
            if (message.contains("ModelArts.81004")) {
                // 未开通预置服务
                return new AgentStudioException(StudioError.MD_NO_MODEL_ACCESS_PERMISSION,
                    Collections.singletonList(message));
            }
            if (message.contains("ModelArts.81005")) {
                // 免费额度已经用完
                return new AgentStudioException(StudioError.MD_THIRD_FREE_QUOTA_USED_UP,
                    Collections.singletonList(message));
            }
            return new AgentStudioException(StudioError.MD_PROVIDER_AUTH_DATA_INVALID,
                Collections.singletonList(message));
        }

        if (message.contains("ModelArts.81011")) {
            // 风控拦截
            return new AgentStudioException(StudioError.MD_MSG_CONTAIN_SENSITIVE, Collections.singletonList(message));
        }
        if (message.contains("ModelArts.81009")) {
            // 模型不存在
            return new AgentStudioException(StudioError.MD_MODEL_SERVICE_NOT_EXIST, Collections.singletonList(message));
        }
        if (message.contains("ModelArts.81010")) {
            // 没有可用的下游推理服务实例
            return new AgentStudioException(StudioError.MD_MODEL_SERVICE_NOT_AVAILABLE,
                Collections.singletonList(message));
        }
        return new AgentStudioException(StudioError.MD_THIRD_MODEL_FAIL, Collections.singletonList(message));
    }

    /**
     * 通用的HTTP异常处理
     *
     * @param statusCode 状态码
     * @param message 响应消息
     * @return 封装后异常
     */
    public static AgentStudioException commonHttpExceptionHandler(int statusCode, String message) {
        if (message == null) {
            message = "";
        }
        if (message.contains("ModelArts.")) {
            return modelArtsExceptionMapping(statusCode, message);
        }
        if (401 == statusCode) {
            if (message.contains("not have access")) {
                return new AgentStudioException(StudioError.MD_NO_MODEL_ACCESS_PERMISSION,
                    Collections.singletonList(message));
            }
            return new AgentStudioException(StudioError.MD_PROVIDER_AUTH_DATA_INVALID,
                Collections.singletonList(message));
        }
        if (429 == statusCode) {
            if (message.toLowerCase(Locale.ROOT).contains("余额不足")) {
                return new AgentStudioException(StudioError.MD_OUTSTANDING_FEEDS, Collections.singletonList(message));
            }
        }
        if (402 == statusCode) {
            if (message.toLowerCase(Locale.ROOT).contains("insufficient balance")) {
                return new AgentStudioException(StudioError.MD_OUTSTANDING_FEEDS, Collections.singletonList(message));
            }
        }
        if (404 == statusCode) {
            if (message.toLowerCase(Locale.ROOT).contains("invalid model")) {
                return new AgentStudioException(StudioError.MD_MODEL_SERVICE_NOT_EXIST,
                    Collections.singletonList(message));
            }
            return new AgentStudioException(StudioError.MD_MODEL_SERVICE_NOT_AVAILABLE,
                Collections.singletonList(message));
        }
        if (message.contains("requested inference service does not exist")) {
            return new AgentStudioException(StudioError.MD_MODEL_SERVICE_NOT_EXIST, Collections.singletonList(message));
        }

        if (400 == statusCode) {
            return new AgentStudioException(StudioError.INVOKE_MODEL_BAD_REQUEST, Collections.singletonList(message));
        }

        return new AgentStudioException(StudioError.MD_THIRD_MODEL_FAIL, Collections.singletonList(message));
    }
}
