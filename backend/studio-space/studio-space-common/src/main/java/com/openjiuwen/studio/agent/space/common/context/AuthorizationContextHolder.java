/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.context;

import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 获取上下文
 */
public interface AuthorizationContextHolder {
    /**
     * orgid中的用户Id
     */
    static String userId() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_USER_ID))
            .orElse(null);
    }

    /**
     * 客户端ip
     */
    static String clientIp() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_CLIENT_IP))
            .orElse(null);
    }

    static String language() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_LANGUAGE))
            .orElse(null);
    }

    /**
     * SessionId，登录时生成
     */
    static String sessionId() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_SID))
            .orElse(null);
    }

    static String userName() {
        return Optional.ofNullable(ContextUtils.getInvocationContext()).map(context -> {
            String userName = context.getContext(ContextConstants.AGENT_SPACE_USER_NAME);
            if (!(userName == null || userName.isEmpty())) {
                userName = URLDecoder.decode(userName, StandardCharsets.UTF_8);
            }
            return userName;
        }).orElse(null);
    }

    static String domainId() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_DOMAIN_ID))
            .orElse(null);
    }

    static String domainName() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_DOMAIN_NAME))
            .orElse(null);
    }

    static String projectId() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_PROJECT_ID))
            .orElse(null);
    }

    static String iamToken() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_IAM_TOKEN))
            .orElse(null);
    }

    static String ticket() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_TICKET))
            .orElse(null);
    }

    static String roles() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_USER_ROLES))
            .orElse(null);
    }

    static String email() {
        return Optional.ofNullable(ContextUtils.getInvocationContext())
            .map(context -> context.getContext(ContextConstants.AGENT_SPACE_USER_EMAIL))
            .orElse(null);
    }
}
