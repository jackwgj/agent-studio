/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.common.validator;

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
    ValidMcpNameConstraintValidator.class
})
@Target({FIELD, PARAMETER})
@Documented
public @interface ValidMcpName {
    /**
     * EmptyAble boolean
     *
     * @return the boolean
     */
    boolean emptyAble() default false;

    /**
     * Message string
     *
     * @return the error message template
     */
    String message() default "Invalid Name";

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
