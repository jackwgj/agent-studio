/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述 str相关操作工具类
 *
 */
@Component
public class StringUtil {
    /**
     * 替换换行符
     *
     * @param str str
     * @return
     */
    public static String removeNewlineCharacter(String str) {
        return str.replaceAll(System.lineSeparator(), "");
    }

    public static boolean isStartWithSpecialCharacter(String data) {
        if(StringUtils.isBlank(data)) {
            return false;
        }
        String rex = "^[@=＝+\\-&\\\\].{0,}";
        Pattern p = Pattern.compile(rex);
        Matcher m =  p.matcher(data);
        return m.matches();
    }

    /**
     * 转义模糊查询参数中的特殊字符
     * @param param 原始查询参数
     * @return 转义后的参数
     */
    public static String escapeLikeParam(String param) {
        if (StringUtils.isBlank(param)) {
            return param;
        }
        // 按顺序转义：% → \% ，_ → \_ （MySQL 默认支持 \ 作为转义符）
        return param.replaceAll("%", "\\\\%")
            .replaceAll("_", "\\\\_");
    }
}
