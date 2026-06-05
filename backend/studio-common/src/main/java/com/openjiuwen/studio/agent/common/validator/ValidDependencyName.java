/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * ValidDependencyName
 *
 * @Date: 2025/2/26 10:34
 * @Description:
 */

@Retention(RUNTIME)
@Constraint(validatedBy = {
    ValidDependencyNameConstraintValidator.class
})
@Target({FIELD, PARAMETER})
@Documented
public @interface ValidDependencyName {
    /**
     * EmptyAble boolean
     *
     * @return the boolean
     */
    boolean emptyAble() default true;

    /**
     * Message string
     *
     * @return the error message template
     */
    String message() default "invalid description";

    /**
     * Groups class [ ]
     *
     * @return the groups the constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * Payload class [ ]
     *
     * @return the payload associated to the constraint
     */
    Class<? extends Payload>[] payload() default {};
}
