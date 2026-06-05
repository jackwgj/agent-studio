/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
@Documented
@Constraint(validatedBy = {
    ValidKnowledgeBaseNameConstraintValidator.class
})
public @interface ValidKnowledgeBaseName {
    /**
     * Emptyable boolean
     *
     * @return the boolean
     */
    boolean canBeEmpty() default true;

    /**
     * Message string
     *
     * @return the error message template
     */
    String message() default "{error.knowledgeBase.name.pattern}";

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

    @Target({FIELD, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        ValidKnowledgeBaseName[] value();
    }
}
