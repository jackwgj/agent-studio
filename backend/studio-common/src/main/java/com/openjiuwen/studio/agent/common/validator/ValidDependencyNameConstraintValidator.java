/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * ValidDependencyNameConstraintValidator
 *
 * @Date: 2025/2/26 10:35
 * @Description: 依赖包名称校验，仅支持以大小写字母开头，包含大小写字母、数字、下划线，长度为2-32字符
 */

public class ValidDependencyNameConstraintValidator implements ConstraintValidator<ValidDependencyName, String> {
    private static final Pattern VALID_DEPENDENCY_NAME = Pattern.compile("[a-zA-Z][_a-zA-Z0-9]{1,31}");

    private boolean emptyAble = false;

    @Override
    public void initialize(ValidDependencyName constraintAnnotation) {
        this.emptyAble = constraintAnnotation.emptyAble();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return emptyAble;
        }
        return VALID_DEPENDENCY_NAME.matcher(value).matches();
    }
}
