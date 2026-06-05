/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.utils;

import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * String工具类
 */
@Slf4j
public class StringUtil {
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("[0-9]+");

    public static final String EMPTY_STRING = "";

    public static final String DOTS = ".";

    public static final String SLASHES_STRING = "/";

    public static final String NON_SLASHES_STRING = "\\";

    public static final String DOTS_STRING = "\\.";

    public static final char DOUBLE_QUOTE = '"';

    public static final String COLON = ":";

    public static final String COMMA = ",";

    public static final String UNDER_LINE = "_";

    public static final String MIDDLE_LINE = "-";

    public static final String QUESTION_MARK = "?";

    public static final char LINE_FEED = '\n';

    public static final char CARRIAGE_RETURN = '\r';

    public static final char TAB = '\t';

    public static final String SPACE = " ";

    public static final String STAR = "*";

    public static final int uuidLength = 36;

    public static final int seqIdLength = 20;

    public static final int recordIdLength = 32;

    public static final String SSE_START = "data:";

    public static final int SSE_START_LEN = 5;

    public static int length(String s) {
        return s == null ? 0 : s.length();
    }

    public static boolean isEmptyString(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmptyString(String s) {
        return !isEmptyString(s);
    }

    public static boolean stringEquals(String right, String left) {
        if (right == null) {
            return left == null;
        }
        return right.equals(left);
    }

    public static boolean stringEqualsIgnoreCase(String right, String left) {
        if (right == null) {
            return left == null;
        }
        return right.equalsIgnoreCase(left);
    }

    public static String join(final Iterable<?> iterable, final String separator) {
        return StringUtils.join(iterable, ",");
    }

    public static String removeEnd(String originalString, String suffix) {
        return StringUtils.removeEnd(originalString, suffix);
    }

    public static boolean isEmpty(@Nullable Object str) {
        return (str == null || "".equals(str));
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);

        for (byte b : data) {
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                sb.append("0");
            } else if (hex.length() == 8) {
                hex = hex.substring(6);
            }

            sb.append(hex);
        }

        return sb.toString().toLowerCase(Locale.getDefault());
    }

    /**
     * 判断字符串是数字组成
     *
     * @param str 传入字符串
     * @return boolean字符串是否由数字组成
     */
    public static boolean isBigDecimal(String str) {
        if (isEmptyString(str)) {
            return false;
        }
        try {
            new BigDecimal(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String object2String(Object obj, String defaultStr) {
        return Optional.ofNullable(obj).map(Object::toString).orElse(defaultStr);
    }

    public static String object2String(Object obj) {
        return object2String(obj, null);
    }

    public static String generateUUID(int idLength) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] randomBytes = new byte[idLength / 2];
            StringBuilder randomString = new StringBuilder();
            secureRandom.nextBytes(randomBytes);
            for (byte b : randomBytes) {
                randomString.append(String.format("%02x", b));
            }
            return randomString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("generateUUID failed! e = {}", e.getMessage());
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }
    }

    /**
     * 带有特殊字符的字符串，在模糊搜索前将转义字符解码
     *
     * @param name 传入字符串
     * @return String 处理后的字符串
     */
    public static String handleBeforeFuzzySearch(String name) {
        if (StringUtil.isEmptyString(name)) {
            return name;
        }

        String newValue = name;

        // 转义字符解码
        try {
            newValue = URLDecoder.decode(name, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("the param decode fail: ", e.getMessage());
        }

        // mybatis模糊搜索需单独处理%_/，表示是转义字符不是sql关键字
        return newValue.replaceAll("/", "//").replaceAll("%", "/%").replaceAll("_", "/_");
    }

    /**
     * 将'a,b,c'形式得字符串，转为['a', 'b', 'c']形式的数组
     */
    public static String[] getCommaStringList(String commaStr) {
        if (StringUtil.isEmptyString(commaStr)) {
            return null;
        }

        return Arrays.stream(commaStr.split(StringUtil.COMMA)).map(StringUtils::trim).toArray(String[]::new);
    }

    /**
     * 如果target不为null，判断orign是否和target相等
     * 如果target为null，直接返回true
     */
    public static boolean isEqualIfPresent(String origin, String target) {
        if (target == null) {
            return true;
        }

        return origin.equals(target);
    }

    /**
     * 去除字符串前后空格
     */
    public static String trim(String str) {
        if (str == null) {
            return null;
        }

        return str.trim();
    }

    public static String trimWhitespace(String str) {
        return isEmptyString(str) ? str : str.strip();
    }

    public static String[] split(@Nullable String toSplit, @Nullable String delimiter) {
        return StringUtils.split(toSplit, delimiter);
    }

    public static List<String> split2List(String toSplit, String delimiter) {
        if (isEmptyString(toSplit)) {
            return new ArrayList<>();
        }
        if (isEmpty(delimiter)) {
            return Collections.singletonList(toSplit);
        }
        String[] resultStr = StringUtils.split(toSplit, delimiter);
        if (resultStr == null || resultStr.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.asList(resultStr);
    }

    /**
     * 检查字符串是否包含列表中的任何一个元素（忽略大小写）
     */
    public static boolean containsAnyIgnoreCase(String str, List<String> searchList) {
        if (str == null || searchList == null || searchList.isEmpty()) {
            return false;
        }
        String lowerStr = str.toLowerCase(Locale.ROOT);
        return searchList.stream().map(String::toLowerCase).anyMatch(lowerStr::contains);
    }
}