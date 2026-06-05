/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.TypeEnum;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * 功能描述
 *
 */
@Slf4j
public class StrUtils {
    private static final String[] CHARS = new String[] {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
        "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
        "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
        "V", "W", "X", "Y", "Z"};

    /**
     * 生成8位短UUID
     *
     * @return uuid
     */
    public static String generateShortUuid() {
        StringBuilder shortId = new StringBuilder();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortId.append(CHARS[x % 0x3E]);
        }
        return shortId.toString();

    }

    /**
     * 类型转换
     *
     * @param obj 原文本
     * @param type 类型
     * @return 处理后类型
     */
    public static Object toObjByType(String obj, String type) {
        if (obj == null) {
            return null;
        }
        TypeEnum typeEnum = TypeEnum.fromValue(type);
        switch (typeEnum) {
            case INTEGER -> {
                return Integer.valueOf(obj);
            }
            case NUMBER -> {
                return Double.valueOf(obj);
            }
            case BOOLEAN -> {
                return Boolean.valueOf(obj);
            }
            case OBJECT -> {
                return JSONObject.parseObject(obj);
            }
            case ARRAY -> {
                return JSONArray.parseArray(obj);
            }
            default -> {
                return obj;
            }
        }
    }

    /**
     * 根据最大长度截断文本
     *
     * @param text 原文本
     * @param maxSize 最大长度
     * @return 处理后的文本
     */
    public static String truncateText(String text, int maxSize) {
        if (StringUtils.isEmpty(text) || text.length() <= maxSize) {
            return text;
        } else {
            return text.substring(0, maxSize);
        }
    }

    /**
     * 对list接口 like concat 类型参数过滤，新增转义结合
     *
     * @param text 原文本
     * @return 处理后的文本
     */
    public static String filterSpecialWords(String text) {
        return StringUtils.isEmpty(text) ? text : text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
