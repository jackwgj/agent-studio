/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class MaskStringUtil {
    private static final List<String> PHONE_FIELDS = Arrays.asList("phone", "mobile", "tel", "telephone");

    private static final List<String> ID_CARD_FIELDS = Arrays.asList("idCard", "idNo", "identityCard", "cardNo");

    private static final List<String> EMAIL_FIELDS = Arrays.asList("email", "mail", "identityCard", "cardNo");

    private static final List<String> BANK_FIELDS = Arrays.asList("card", "bank", "account");

    private static final List<String> NAME_FIELDS = Arrays.asList("name", "realName");

    private static final List<String> PASSWORD_FIELDS = Arrays.asList("password", "sk", "pwd", "token", "secret");

    private static final List<String> NON_SENSITIVE_FIELDS = Arrays.asList("taskId", "task_id");

    public static String logMaskString(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 1) {
            return "*";
        }
        int leftLetterNumber = 1;
        int rightLetterNumber = str.length() > 6 ? 3 : 0;
        return maskString(str, leftLetterNumber, rightLetterNumber);
    }

    private static String maskString(String str, int leftLetterNumber, int rightLetterNumber) {
        return IntStream.range(0, str.length())
            .mapToObj(i -> i < leftLetterNumber || i >= str.length() - rightLetterNumber ? str.charAt(i) : '*')
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    /**
     * 敏感字段脱敏（忽略大小写）
     */
    public static String maskValue(String fieldName, String strValue) {
        if (StringUtils.isBlank(strValue)) {
            return StringUtils.EMPTY;
        }

        // 根据字段名进行脱敏
        if (NON_SENSITIVE_FIELDS.contains(fieldName)) {
            return strValue;
        } else if (StringUtil.containsAnyIgnoreCase(fieldName, PHONE_FIELDS)) {
            return maskPhone(strValue);
        } else if (StringUtil.containsAnyIgnoreCase(fieldName, ID_CARD_FIELDS)) {
            return maskIdCard(strValue);
        } else if (StringUtil.containsAnyIgnoreCase(fieldName, EMAIL_FIELDS)) {
            return maskEmail(strValue);
        } else if (StringUtil.containsAnyIgnoreCase(fieldName, BANK_FIELDS)) {
            return maskBankCard(strValue);
        } else if (StringUtil.containsAnyIgnoreCase(fieldName, NAME_FIELDS)) {
            return maskName(strValue);
        } else if (StringUtil.containsAnyIgnoreCase(fieldName, PASSWORD_FIELDS)) {
            return "******";
        } else {
            return strValue;
        }
    }

    /**
     * 脱敏手机号
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏身份证号
     */
    private static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return "****";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 脱敏邮箱
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }

        int atIndex = email.indexOf('@');
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);

        if (prefix.length() <= 1) {
            return "*" + suffix;
        }
        return prefix.charAt(0) + "***" + suffix;
    }

    /**
     * 脱敏银行卡号
     */
    private static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return "****";
        }
        return bankCard.substring(0, 6) + "******" + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 脱敏姓名
     */
    private static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return "*";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*" + name.charAt(name.length() - 1);
    }
}
