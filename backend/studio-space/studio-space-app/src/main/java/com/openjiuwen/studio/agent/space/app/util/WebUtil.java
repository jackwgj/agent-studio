/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import com.google.common.net.InetAddresses;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebUtil {
    private static final String REMOTE_IP_HEADER = "X-Forwarded-For";

    private static int clientIpPosition;

    @Value("${agent.apsce.remoteip.position}")
    public void setClientIpPosition(int position) {
        WebUtil.clientIpPosition = position;
    }

    public static String getRealIp(HttpServletRequest request) {
        String realIp;
        String remoteIp = request.getHeader(REMOTE_IP_HEADER);
        if (StringUtils.isNotBlank(remoteIp)) {
            realIp = getRealIp(remoteIp);
        } else {
            realIp = getRealIp(request.getRemoteAddr());
        }
        return realIp;
    }

    public static boolean ipCheck(String ipStr) {
        boolean result = false;
        if (StringUtils.isBlank(ipStr)) {
            return result;
        }

        if (isValidByGuava(ipStr)) {
            result = true;
        }

        return result;
    }

    public static boolean isValidByGuava(String ip) {
        return InetAddresses.isInetAddress(ip);
    }

    private static String getRealIp(String forward) {
        if (StringUtils.isBlank(forward)) {
            return "";
        }
        String[] forwardList = forward.split(", ");
        if (forwardList.length == 0 || clientIpPosition == 0 || forwardList.length < clientIpPosition) {
            return "";
        }

        String realIp = forwardList[forwardList.length - clientIpPosition];
        if (ipCheck(realIp)) {
            return realIp;
        }
        return "";
    }

}
