/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)  // 仅作用于方法
@Retention(RetentionPolicy.RUNTIME)  // 运行时生效
@Documented
public @interface RedisRateLimit {
    /**
     * 限流 key（url + domainId）
     */
    String key() default "";

    /**
     * 令牌生成速率（个/秒）
     */
    double rate() default 5;

    /**
     * 令牌桶最大容量
     */
    int capacity() default 10;

}
