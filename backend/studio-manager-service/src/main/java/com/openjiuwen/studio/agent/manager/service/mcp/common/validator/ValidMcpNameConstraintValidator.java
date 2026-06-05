/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * ValidNameConstraintValidator
 *
 * @Date: 2024/6/3 16:35
 * @Description: 正则：^(?!_)(?!-)(?!\\d)[a-zA-Z0-9_\\-\u4e00-\u9fa5]{2,64}$  不能以下划线、数字、连字符开头 字符串结束
 */

public class ValidMcpNameConstraintValidator implements ConstraintValidator<ValidMcpName, String> {
    private static final Pattern VALID_NAME = Pattern.compile(
        "^(?!_)(?!-)(?!\\d)[a-zA-Z0-9_\\-\u4e00-\u9fa5]{1,64}$");

    private boolean emptyAble = false;

    @Override
    public void initialize(ValidMcpName constraintAnnotation) {
        this.emptyAble = constraintAnnotation.emptyAble();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return emptyAble;
        }
        return VALID_NAME.matcher(value).matches();
    }
}


