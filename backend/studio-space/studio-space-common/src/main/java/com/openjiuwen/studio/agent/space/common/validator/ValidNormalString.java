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
 * 字符串校验
 */
@Retention(RUNTIME)
@Constraint(validatedBy = {
    ValidNormalStringValidator.class
})
@Target({FIELD, PARAMETER})
@Documented
public @interface ValidNormalString {
    /**
     * Message string
     *
     * @return the error message template
     */
    String message() default "name";

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
