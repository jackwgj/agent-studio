/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.EN_LANGUAGE;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.X_AUTH_TOKEN;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.X_LANGUAGE;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;



/**
 * 功能描述
 *
 */
@Slf4j
public class HttpUtil {
    private HttpUtil() {
    }

    /**
     * 关闭连接
     *
     * @param closeable
     */
    public static void safeClose(Closeable closeable, String type) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException var2) {
                log.error("close {} stream fail, {}", type, var2.getMessage());
            }
        }
    }

    /**
     * 获取X-Auth-Token
     *
     * @return
     */
    public static String getXAuthToken() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
            RequestContextHolder.getRequestAttributes())).getRequest();
        return request.getHeader(X_AUTH_TOKEN);
    }

    /**
     * 获取X-Language
     *
     * @return string language
     */
    public static String getLanguage() {
        RequestAttributes servletRequestAttributes = RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            return EN_LANGUAGE;
        }
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
            servletRequestAttributes)).getRequest();
        return request.getHeader(X_LANGUAGE);
    }
}
