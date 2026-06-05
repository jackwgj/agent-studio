/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description EmbeddingObjectValidator
 * @Since 2024/9/9
 */
public class ValidEmbeddingObjectValidator implements ConstraintValidator<ValidEmbeddingObject, Object> {
    @Override
    public void initialize(ValidEmbeddingObject constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (object == null) {
            return false;
        }
        if (object instanceof String) {
            int maxEmbeddingStringSize = 25 * 1024 * 1024;
            if (((String) object).length() > maxEmbeddingStringSize) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("The value of input is too large.")
                    .addConstraintViolation();
                return false;
            }
            return true;
        } else if (object instanceof List) {
            List<String> strings = castList(object, String.class);
            if (strings == null) {
                return false;
            }
            int maxEmbeddingListSize = 1000;
            if (strings.size() > maxEmbeddingListSize) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Too many input parameters.").addConstraintViolation();
                return false;
            }
            int countSize = 0;
            for (String string : strings) {
                countSize += string.length();
                int maxEmbeddingInputSize = 25 * 1024 * 1024;
                if (countSize > maxEmbeddingInputSize) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("The value of input is too large.")
                        .addConstraintViolation();
                    return false;
                }
            }
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("The value of input is not string or list!")
            .addConstraintViolation();
        return false;
    }

    private static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }
}
