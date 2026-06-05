/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.context;

import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;

import java.util.Optional;

/**
 * 获取上下文对象
 */
public interface AddAuthorizationContextHolder {
    /**
     * 用户Id
     */
    static void userId(String userId) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_USER_ID, userId));
    }

    /**
     * 用户Id
     */
    static void userName(String userName) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_USER_NAME, userName));
    }

    /**
     * 用户角色
     */
    static void userRoles(String userRoles) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_USER_ROLES, userRoles));
    }

    /**
     * 用户邮箱
     */
    static void userEmail(String email) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_USER_EMAIL, email));
    }

    /**
     * 客户端ip
     */
    static void clientIp(String clientIp) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_CLIENT_IP, clientIp));
    }

    static void language(String language) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_LANGUAGE, language));
    }

    /**
     * SessionId，登录时生成
     */
    static void sessionId(String sessionId) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_SID, sessionId));
    }

    static void domainId(String domainId) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_DOMAIN_ID, domainId));
    }

    static void domainName(String domainName) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_DOMAIN_NAME, domainName));
    }

    static void projectId(String projectId) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_PROJECT_ID, projectId));
    }

    static void iamToken(String IamToken) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_IAM_TOKEN, IamToken));
    }

    static void ticket(String ticket) {
        Optional.ofNullable(ContextUtils.getInvocationContext())
            .ifPresent(context -> context.addContext(ContextConstants.AGENT_SPACE_TICKET, ticket));
    }
}
