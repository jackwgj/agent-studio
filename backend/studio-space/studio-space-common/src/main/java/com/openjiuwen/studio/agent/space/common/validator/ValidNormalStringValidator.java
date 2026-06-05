package com.openjiuwen.studio.agent.space.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 通用字符串校验器
 */
public class ValidNormalStringValidator implements ConstraintValidator<ValidNormalString, String> {
    private static final Pattern VALID_NAME = Pattern.compile(
        "^[a-zA-Z0-9\\u4e00-\\u9fa5][a-zA-Z0-9_\\-\\u4e00-\\u9fa5]*$");

    @Override
    public void initialize(ValidNormalString constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return VALID_NAME.matcher(value).matches();
    }
}
