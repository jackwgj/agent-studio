/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2020. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 枚举校验器
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {
    private Set<String> enums;

    /**
     * 初始化注解校验器
     */
    @Override
    public void initialize(EnumValue constraintAnnotation) {
        enums = new HashSet<>(Arrays.asList(constraintAnnotation.value()));
    }

    /**
     * 校验执行器
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return StringUtils.isEmpty(value) || enums.contains(value);
    }
}
