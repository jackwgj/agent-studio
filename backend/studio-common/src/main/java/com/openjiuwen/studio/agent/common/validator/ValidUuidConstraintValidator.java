/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * ValidUuidConstraintValidator
 *
 * @Date: 2024/5/14 15:10
 * @Description: 校验uuid，只能包含大小写字母、数字、中划线
 */

public class ValidUuidConstraintValidator implements ConstraintValidator<ValidUuid, String> {

    private static final Pattern VALID_UUID = Pattern.compile("^[a-zA-Z0-9-]+$");

    private boolean emptyAble;

    private int minSize;

    private int maxSize;

    @Override
    public void initialize(ValidUuid constraintAnnotation) {
        this.emptyAble = constraintAnnotation.emptyAble();
        this.minSize = constraintAnnotation.minSize();
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isEmpty(value)) {
            return emptyAble;
        }

        int length = value.length();
        if (length < minSize || length > maxSize) {
            return false;
        }
        return VALID_UUID.matcher(value).matches();
    }
}
