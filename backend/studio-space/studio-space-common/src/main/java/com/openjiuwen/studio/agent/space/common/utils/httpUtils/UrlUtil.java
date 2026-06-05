/**
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.utils.httpUtils;


import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UrlUtil {
    private static final Pattern ENCODED_CHARACTERS_PATTERN;

    public static String urlEncode(String value, boolean path) {
        if (StringUtil.isEmptyString(value)) {
            return "";
        }
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
        Matcher matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded);

        StringBuilder buffer;
        String replacement;
        for (buffer = new StringBuilder(encoded.length()); matcher.find();
             matcher.appendReplacement(buffer, replacement)) {
            replacement = matcher.group(0);
            if ("+".equals(replacement)) {
                replacement = "%20";
            }
            if ("*".equals(replacement)) {
                replacement = "%2A";
            }
            if ("%7E".equals(replacement)) {
                replacement = "~";
            }
            if (path && "%2F".equals(replacement)) {
                replacement = "/";
            }
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    static {
        ENCODED_CHARACTERS_PATTERN = Pattern.compile(Pattern.quote("+") + "|" + Pattern.quote("*") + "|"
                + Pattern.quote("%7E") + "|" + Pattern.quote("%2F"));
    }

    /**
     * 防止HTTP参数污染
     *
     * @param urlStr 请求url
     */
    public static void validUrlQueryParamInMultipleSameKey(String urlStr) {
        try {
            URL urlObj = new URL(urlStr);
            String queryUrl = urlObj.getQuery();
            if (StringUtil.isEmptyString(queryUrl)) {
                return;
            }
            String[] requestParams = queryUrl.split("&");
            List<String> keys = new ArrayList<>();
            for (String requestParam : requestParams) {
                String[] keyValues = requestParam.split("=");
                if (StringUtil.isEmptyString(keyValues[0])) {
                    throw new AgentSpaceException(AgentSpaceErrorCodes.URL_QUERY_PARAM_ERROR);
                }
                if (keys.contains(keyValues[0])) {
                    throw new AgentSpaceException(AgentSpaceErrorCodes.URL_QUERY_PARAM_DUPLICATE);
                } else {
                    keys.add(keyValues[0]);
                }

            }
        } catch (MalformedURLException e) {
            log.error("Failed to get param. Msg:{}", e.getMessage());
        }
    }
}
