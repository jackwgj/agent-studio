/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils.simple;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.constant.SimpleConstants;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;

public class ServletUtils {
    private static final Logger log = LoggerFactory.getLogger(ServletUtils.class);

    private static final String COOKIE_SECURITY_ENABLE = "cookie_security_enable";

    private static final String SERVER_SSL_ENABLED = "server.ssl.enabled";

    /**
     * 生成sid
     *
     * @return 返回sessionId
     */
    public static String genAgentSid() {
        String sid;
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] sidBytes = new byte[32];
            random.nextBytes(sidBytes);
            sid = HexFormat.of().withUpperCase().formatHex(sidBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("generate token id failed.");
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
        return sid;
    }

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
            agentSid =
                Arrays.stream(cookies).filter(c -> c.getName().equalsIgnoreCase(SimpleConstants.AGENT_SID)).findFirst();
        }
        if (agentSid.isPresent()) {
            return agentSid.get().getValue();
        } else {
            return null;
        }
    }

    /**
     * sid设置到cookie
     *
     * @param httpServletResponse http响应
     * @param sid agent sessionId
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
        // 该Cookie生效的路径范围:
        cookie.setPath("/");
        // 该Cookie有效期:
        cookie.setMaxAge(Integer.MAX_VALUE);
        return cookie;
    }

    /**
     * 删除sid cookie
     *
     * @param httpServletResponse 响应对象
     */
    public static void deleteSidCookie(HttpServletResponse httpServletResponse) {
        Cookie delCookie = new Cookie(SimpleConstants.AGENT_SID, "");
        delCookie.setMaxAge(0);
        delCookie.setPath("/");
        setCookieSecurity(delCookie);
        httpServletResponse.addCookie(delCookie);
    }

    private static void setCookieSecurity(Cookie cookie) {
        String sec = SpringBeanUtils.getProperty(COOKIE_SECURITY_ENABLE, Constants.TRUE);
        String ssl = SpringBeanUtils.getProperty(SERVER_SSL_ENABLED, Constants.TRUE);
        if (sec.equalsIgnoreCase(Constants.TRUE)) {
            cookie.setHttpOnly(true);
        }
        if (ssl.equalsIgnoreCase(Constants.TRUE)) {
            cookie.setSecure(true);
        }
    }

}
