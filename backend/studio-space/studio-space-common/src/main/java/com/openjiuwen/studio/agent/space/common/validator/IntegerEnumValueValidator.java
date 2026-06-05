/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2013-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description: int枚举值校验注解实现
 * author: l30030913
 * create: 2024-06-27
 **/
public class IntegerEnumValueValidator implements ConstraintValidator<IntegerEnumValue, Integer> {
    private Set<Integer> enums;

    public IntegerEnumValueValidator() {
    }

    public void initialize(IntegerEnumValue constraintAnnotation) {
        this.enums = Arrays.stream(constraintAnnotation.value()).boxed().collect(Collectors.toSet());
    }

    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        } else {
            return this.enums.contains(value);
        }
    }
}
