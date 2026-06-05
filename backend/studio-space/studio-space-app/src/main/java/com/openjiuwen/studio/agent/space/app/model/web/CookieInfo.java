/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.model.web;

import com.openjiuwen.studio.agent.space.app.util.CookieUtil;
import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookieInfo {
    public static final int LIMIT_COOKIE_LENGTH = 100;

    private String wiseSessionId;

    private String language;

    private String wiseUserId;

    private String csrfToken;

    private String deviceType;

    public static CookieInfo buildCookieInfo(HttpServletRequest request) {
        return CookieInfo.builder()
            .wiseSessionId(getCookieValueByName(request, ContextConstants.AGENT_SPACE_SID))
            .wiseUserId(getCookieValueByName(request, ContextConstants.AGENT_SPACE_USER_ID))
            .csrfToken(request.getHeader(ContextConstants.AGENT_SPACE_CSRF_TOKEN))
            .build();
    }

    private static String getCookieValueByName(HttpServletRequest request, String cookieName) {
        return CookieUtil.getCookieValueByName(request, cookieName);
    }
}
