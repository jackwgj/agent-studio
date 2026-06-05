/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import com.openjiuwen.studio.agent.space.app.service.session.AccessUrlService;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.model.BaseResp;
import com.openjiuwen.studio.agent.space.common.utils.JacksonUtils;
import com.openjiuwen.studio.agent.space.common.utils.SpringContextUtils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * SessionFilter错误响应类
 */
public class BuildResponseUtil {
    public static final int NOT_LOGIN_CODE = 430;

    public static final int LOGOUT_CODE = 440;

    private static AccessUrlService getAccessUrlService() {
        return SpringContextUtils.getBean(AccessUrlService.class);
    }

    /**
     * 创建系统错误响应
     *
     * @param response 接口响应
     */
    public static void createSystemErrResponse(HttpServletResponse response) {
        try {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                AgentSpaceErrorCodes.SYSTEM_ERROR.errorCode());
        } catch (IOException e) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }
    }

    /**
     * 创建系统错误响应
     *
     * @param response 接口响应
     */
    public static void createOpenApiErrResponse(HttpServletResponse response, int status, String errorMsg) {
        try {
            sendSpecialId(response, status, BaseResp.fail(errorMsg, null));
        } catch (IOException e) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }
    }

    /**
     * 创建OrgId认证失败响应
     */
    public static void createNotLoginResponse(HttpServletResponse response) {
        try {
            sendSpecialId(response, NOT_LOGIN_CODE,
                BaseResp.fail(AgentSpaceErrorCodes.NOT_LOGIN, getAccessUrlService().getLoginUrl()));
        } catch (IOException e) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }
    }

    /**
     * 创建OrgId认证失败响应
     */
    public static void createLogoutResponse(HttpServletResponse response) {
        try {
            CookieUtil.clearCookie(response);
            sendSpecialId(response, LOGOUT_CODE,
                BaseResp.fail(AgentSpaceErrorCodes.USER_LOGOUT, getAccessUrlService().getLogoutUrl()));
        } catch (IOException e) {
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }
    }

    private static void sendSpecialId(HttpServletResponse response, int code, BaseResp<String> data)
        throws IOException {
        response.setStatus(code);
        writeInfoToResponce(response, data);
    }

    private static void writeInfoToResponce(HttpServletResponse response, BaseResp<String> data) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        byte[] dataByteArr = Objects.requireNonNull(JacksonUtils.obj2json(data)).getBytes(StandardCharsets.UTF_8);
        outputStream.write(dataByteArr);
    }
}
