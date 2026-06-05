/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UrlUtil {

    private static String reg = "\\{[a-zA-Z0-9_]{1,30}\\}";
    private static Pattern pattern = Pattern.compile(reg);

    public static String handleUrl(String url, String... params) {
        Matcher matcher = pattern.matcher(url);
        int index = 0;
        while (matcher.find()) {
            String group = matcher.group();
            url = url.replace(group, params[index]);
            index++;
        }
        return url;
    }

}
