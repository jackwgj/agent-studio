/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 转换为JavaScript的encodeURIComponent
 * 由于URLEncoder.encode与JavaScript的encode有区别，需要进行转换
 *
 *
 * @since 2024-07-24
 */
public class EncodingUtil {
    /**
     * 转换为JavaScript的encodeURIComponent
     *
     * @param url 需要encoded的url
     * @return encode string
     */
    public static String encodeURIComponent(String url) {
        String result;
        try {
            result =
                URLEncoder.encode(url, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = url;
        }
        return result;
    }

    public static String decodeURIComponent(String url) {
        String result;
        try {
            result =
                    URLDecoder.decode(url, "UTF-8")
                            .replaceAll("%20","\\+")
                            .replaceAll("!","\\%21")
                            .replaceAll("'","\\%27")
                            .replaceAll("\\)","\\%29")
                            .replaceAll("\\(","\\%28")
                            .replaceAll("~","\\%7E");
        } catch (UnsupportedEncodingException e) {
            result = url;
        }
        return result;
    }
}
