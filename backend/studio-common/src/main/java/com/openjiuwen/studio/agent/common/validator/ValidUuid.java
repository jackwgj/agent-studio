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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.TYPE_USE, FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {
    ValidUuidConstraintValidator.class
})
public @interface ValidUuid {
    boolean emptyAble() default false;

    /**
     * @return the error message template
     */
    String message() default "must be uuid";

    /**
     * @return the Groups the constraint belongs to;
     */
    Class<?>[] groups() default {};

    int minSize() default 1;

    int maxSize() default 64;

    /**
     * @return the payload which is associated to the constraint
     */
    Class<? extends Payload>[] payload() default {};
}
