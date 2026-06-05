/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2020. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.annotation.quota;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配额注解，在method之前检查配额是否足够，在method后增加/减少配额
 */
@Target( {
    ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Quota {
    String type();

    int offset() default 0;
}