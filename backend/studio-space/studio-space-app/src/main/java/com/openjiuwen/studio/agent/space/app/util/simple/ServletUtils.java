/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util.simple;

import com.openjiuwen.studio.agent.space.app.constant.SimpleConstants;
import com.openjiuwen.studio.agent.space.app.util.SpringBeanUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public class ServletUtils {
    private static final String COOKIE_SECURITY_ENABLE = "cookie_security_enable";

    private static final String SERVER_SSL_ENABLED = "server.ssl.enabled";

    /**
     * 获取 agentSessionId
     *
     * @param request 请求对象
     * @return agentSessionId
     */
    public static String getAgentSid(HttpServletRequest request) {
        // 请求中用户信息变化先从attribute取，第一次会设置到attribute，后续才会设置到cookie
        if (request.getAttribute(SimpleConstants.AGENT_SID) != null) {
            return request.getAttribute(SimpleConstants.AGENT_SID).toString();
        }
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> agentSid = Optional.empty();
        if (cookies != null && cookies.length > 0) {
            agentSid = Arrays.stream(cookies)
                .filter(c -> c.getName().equalsIgnoreCase(SimpleConstants.AGENT_SID))
                .findFirst();
        }
        return agentSid.map(Cookie::getValue).orElse(null);
    }

    /**
     * sid设置到cookie
     *
     * @param httpServletResponse http响应
     * @param sid                 agent sessionId
     */
    public static void setSidToCookie(HttpServletResponse httpServletResponse, String sid) {
        Cookie cookie = buildAgentSidCookie(sid);
        setCookieSecurity(cookie);
        // 将该Cookie添加到响应:
        httpServletResponse.addCookie(cookie);
    }

    /**
     * 构建agentSid cookie
     *
     * @param sid
     * @return
     */
    public static Cookie buildAgentSidCookie(String sid) {
        Cookie cookie = new Cookie(SimpleConstants.AGENT_SID, sid);
        setCookieSecurity(cookie);
        cookie.setHttpOnly(true);
        // 该Cookie生效的路径范围:
        cookie.setPath("/");
        // 该Cookie有效期:
        cookie.setMaxAge(Integer.MAX_VALUE);
        return cookie;
    }

    private static void setCookieSecurity(Cookie cookie) {
        String sec = SpringBeanUtils.getProperty(COOKIE_SECURITY_ENABLE, "true");
        String ssl = SpringBeanUtils.getProperty(SERVER_SSL_ENABLED, "true");
        if (sec.equalsIgnoreCase("true") && ssl.equalsIgnoreCase("true")) {
            cookie.setSecure(true);
        }
    }
}
