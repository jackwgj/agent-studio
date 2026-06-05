/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2017-2021. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * List中字符串校验
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {
    ValidStringListValidator.class
})
public @interface ValidStringList {
    boolean emptyable() default true;

    int minSize() default 0;

    int maxSize() default 100;

    int maxLength() default 2000;

    /**
     * @return the error message template
     */
    String message() default "list size or list content is not allowed";

    /**
     * @return the Groups the constraint belongs to;
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload which is associated to the constraint
     */
    Class<? extends Payload>[] payload() default {};
}
