/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * CookieUtil  cookie 工具类
 */
public class CookieUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CookieUtil.class);

    private static final int LIMIT_COOKIE_LENGTH = 999;

    public static String getCookieValueByName(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null || cookies.length > LIMIT_COOKIE_LENGTH) {
            LOGGER.error("failed to get cookie value . cookie length is {}",
                Objects.nonNull(cookies) ? cookies.length : 0);
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void addCookie(HttpServletResponse response, String cookieName, String cookieValue, boolean secure,
        boolean httpOnly) {
        if (response == null || StringUtil.isEmptyString(cookieName)) {
            LOGGER.error("Cookie Name Or Response is empty");
            return;
        }
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static void clearCookie(HttpServletResponse response, String cookieName, boolean secure, boolean httpOnly) {
        if (response == null || StringUtil.isEmptyString(cookieName)) {
            LOGGER.error("Cookie Name Or Response is empty");
            return;
        }
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static void clearCookie(HttpServletResponse response) {
        clearCookie(response, ContextConstants.AGENT_SPACE_SID, false, true);
        clearCookie(response, ContextConstants.AGENT_SPACE_DOMAIN_ID, false, false);
        clearCookie(response, ContextConstants.AGENT_SPACE_USER_ID, false, true);
        clearCookie(response, ContextConstants.AGENT_SPACE_EXP_TIME, false, false);
        clearCookie(response, ContextConstants.AGENT_SPACE_CSRF_TOKEN, false, false);
    }
}
