/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2013-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.validator;

import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字符串枚举校验
 */
public class StringEnumValueValidator implements ConstraintValidator<StringEnumValue, String> {
    private List<String> values = new ArrayList<>();

    @Override
    public void initialize(StringEnumValue constraintAnnotation) {
        String[] strList = constraintAnnotation.values();
        values = Arrays.stream(strList).collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtil.isEmptyString(value)) {
            return true;
        }
        return values.contains(value.trim());
    }
}
