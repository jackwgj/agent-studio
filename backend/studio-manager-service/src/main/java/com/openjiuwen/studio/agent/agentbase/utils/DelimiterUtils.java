/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.utils;

import java.util.Arrays;
import java.util.List;

public class DelimiterUtils {

    public static String delimiterListToString(List<String> delimiter_list) {
        if (delimiter_list == null || delimiter_list.isEmpty()) {
            return "/n";
        }

        StringBuilder result = new StringBuilder();
        result.append(DelimiterEnum.convert(delimiter_list.get(0)));
        for (int i = 1; i < delimiter_list.size(); i++) {
            result.append("`");
            result.append(DelimiterEnum.convert(delimiter_list.get(i)));
        }

        return result.toString();
    }

    public static List<String> delimiterStringToList(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        // 使用分隔符分割字符串
        String[] parts = input.split("`");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = DelimiterEnum.converse_convert(parts[i]);
        }

        // 将数组转换为列表
        return Arrays.asList(parts);
    }

    public enum DelimiterEnum {

        period_zh("。"),
        period_en("."),
        exclamation_mark_zh("！"),
        exclamation_mark_en("!"),
        question_mark_zh("？"),
        question_mark_en("?"),
        comma_zh("，"),
        comma_en(","),
        wrap_en("/n"),
        space_en("/t");

        private String value;

        DelimiterEnum(String value) {
            this.value = value;
        }

        public static String convert(String delimiter) {
            for (DelimiterEnum item : DelimiterEnum.values()) {
                if (item.name().equals(delimiter)) {
                    return item.value;
                }
            }
            return null;
        }

        public static String converse_convert(String delimiter_symbol) {
            for (DelimiterEnum item : DelimiterEnum.values()) {
                if (item.value.equals(delimiter_symbol)) {
                    return item.name();
                }
            }
            return null;
        }
    }
}
