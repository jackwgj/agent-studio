/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 功能描述
 *
 */
@Service
public class CommonService {
    // 包含汉字、英文字母、数字、下划线、连字符(-)、英文括号()、中文括号（）、空格、英文句点(.)，长度2-64个字符。
    private static final String SEARCH_NAME_PATTERN = "^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5()（）\\s.]{1,64}$";

    //以汉字、英文字母开头（不能以下划线、连字符或数字开头），可以包含汉字、英文字母、数字、下划线、连字符(-)、英文括号()、中文括号（）、空格、英文句点(.)，长度2-64个字符。
    private static final String NAME_PATTERN = "^(?!_)(?!-)(?!\\d)[a-zA-Z0-9_\\-\\u4e00-\\u9fa5()（）\\s.]{2,64}$";

    /**
     * 校验查询名称是否匹配正则约束
     * 以汉字、英文大小写或数字开头，可以包含多个汉字、英文字母、数字、下划线、连字符(-)、左括号( 或 右括号)
     * @param name 服务名称
     */
    public void validSearchNameStartWithChineseEnglishLettersNumber(String name) {
        if (StringUtils.isBlank(name)) {
            return;
        }

        if (!Pattern.matches(SEARCH_NAME_PATTERN, name)) {
            throw new AgentStudioException(StudioError.SEARCH_NAME_FORMAT);
        }
    }

    /**
     * 校验名称是否匹配正则约束
     * 以中文、英文字母开头，只能包含中文、英文字母、数字、下划线 _ 和连字符 -，长度在2 到 64 个字符之间
     * @param name 服务名称
     */
    public void validNameStartWithChineseEnglishLetters(String name) {
        if (StringUtils.isBlank(name) || !Pattern.matches(NAME_PATTERN, name)) {
            throw new AgentStudioException(StudioError.NAME_FORMAT);
        }
    }
}
