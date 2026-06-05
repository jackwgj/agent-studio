/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * ValidDescriptionConstraintValidator
 *
 * @Date: 2024/6/3 16:38
 * @Description: 校验描述信息，只能包含英文，中文，数字，下划线，中划线，空格及,.?:;"'：；“”‘’，。？、()（）/@!！*%#
 */

public class ValidDescriptionConstraintValidator implements ConstraintValidator<ValidDescription, String> {
    private static final Pattern VALID_DESCRIPTION = Pattern.compile("^[\\u4e00-\\u9fa5_a-zA-Z0-9\\-,.?:;\"'：；“”‘’，。？、()（）/@!！*%# ]*$");

    private boolean emptyAble = false;

    @Override
    public void initialize(ValidDescription constraintAnnotation) {
        this.emptyAble = constraintAnnotation.emptyAble();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return emptyAble;
        }
        return VALID_DESCRIPTION.matcher(value).matches();
    }
}

