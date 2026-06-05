/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

public class ValidKnowledgeBaseNameConstraintValidator implements ConstraintValidator<ValidKnowledgeBaseName, String> {

    public static final String KNOWLEDGE_BASE_NAME_REGEX
        = "^[a-zA-Z0-9\\u4e00-\\u9fa5][a-zA-Z0-9\\u4e00-\\u9fa5-_]{0,49}$";

    private boolean canBeEmpty = false;

    @Override
    public void initialize(ValidKnowledgeBaseName constraintAnnotation) {
        canBeEmpty = constraintAnnotation.canBeEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return canBeEmpty;
        }
        return value.matches(KNOWLEDGE_BASE_NAME_REGEX);
    }
}
