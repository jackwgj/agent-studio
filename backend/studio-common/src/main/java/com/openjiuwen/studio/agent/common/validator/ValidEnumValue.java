/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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

@Retention(RUNTIME)
@Constraint(validatedBy = {
    ValidEnumValueConstraintValidator.class
})
@Target({FIELD, PARAMETER})
@Documented
public @interface ValidEnumValue {
    /**
     * emptyAble boolean
     *
     * @return the boolean
     */
    boolean emptyAble() default true;

    /**
     * ignoreCase boolean
     *
     * @return the boolean
     */
    boolean ignoreCase() default false;

    String[] values();

    /**
     * @return the error message template
     */
    String message() default "invalid param";

    /**
     * @return the Groups the constraint belongs to;
     */
    Class<?>[] groups() default {};

    /**
     * @return the payload which is associated to the constraint
     */
    Class<? extends Payload>[] payload() default {};
}
