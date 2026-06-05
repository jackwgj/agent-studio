/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

/**
 * 枚举校验
 *
 */

public class ValidEnumValueConstraintValidator implements ConstraintValidator<ValidEnumValue, String> {
    private boolean emptyAble = false;

    private boolean ignoreCase;

    private String[] values;

    @Override
    public void initialize(ValidEnumValue constraintAnnotation) {
        this.emptyAble = constraintAnnotation.emptyAble();
        this.ignoreCase = constraintAnnotation.ignoreCase();
        this.values = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isEmpty(value)) {
            return emptyAble;
        }
        for (String example : values) {
            if (ignoreCase ? value.equalsIgnoreCase(example) : value.equals(example)) {
                return true;
            }
        }
        return false;
    }
}
