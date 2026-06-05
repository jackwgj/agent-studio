/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2017-2021. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class ValidStringListValidator implements ConstraintValidator<ValidStringList, List<String>> {
    private static final Pattern VALID_STRING = Pattern.compile(
        "^[a-zA-Z0-9\\u4e00-\\u9fa5][a-zA-Z0-9_\\-\\u4e00-\\u9fa5]*$");

    private boolean emptyable;

    private int maxSize;

    private int minSize;

    private int maxLength;

    @Override
    public void initialize(ValidStringList constraintAnnotation) {
        emptyable = constraintAnnotation.emptyable();
        maxSize = constraintAnnotation.maxSize();
        minSize = constraintAnnotation.minSize();
        maxLength = constraintAnnotation.maxLength();
    }

    @Override
    public boolean isValid(List<String> list, ConstraintValidatorContext constraintValidatorContext) {
        if (CollectionUtils.isEmpty(list)) {
            return emptyable;
        }
        if (list.size() > maxSize || list.size() < minSize) {
            log.error("content size {} is large than limit {} or less than limit {}", list.size(), maxSize, minSize);
            return false;
        }
        for (String content : list) {
            if (StringUtils.isEmpty(content)) {
                continue;
            }
            if (content.length() > maxLength) {
                log.error("content length {} is large than limit {}", content.length(), maxSize);
                return false;
            }
            if (!VALID_STRING.matcher(content).matches()) {
                log.error("string content not match regular.");
                return false;
            }
        }
        return true;
    }
}
