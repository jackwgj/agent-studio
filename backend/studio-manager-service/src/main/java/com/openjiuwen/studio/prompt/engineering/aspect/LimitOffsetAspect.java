/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.aspect;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ZERO;

import com.openjiuwen.studio.prompt.engineering.utils.ReflectionUtil;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;


/**
 * limitOffset初始化参数切面
 *
 */
@Component
@Scope
@Aspect
@Slf4j
public class LimitOffsetAspect {
    private static final String CONSTANT_LIMIT = "limit";

    private static final String CONSTANT_LIMIT_GET = "getLimit";

    private static final String CONSTANT_LIMIT_SET = "setLimit";

    private static final String CONSTANT_OFFSET = "offset";

    private static final String CONSTANT_OFFSET_GET = "getOffset";

    private static final String CONSTANT_OFFSET_SET = "setOffset";

    @Around("@annotation(com.openjiuwen.studio.prompt.engineering.annotation.LimitOffset)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > NUM_ZERO) {
                Arrays.stream(args).filter(arg -> {
                    Field[] fields = arg.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if (CONSTANT_LIMIT.equals(field.getName()) || CONSTANT_OFFSET.equals(field.getName())) {
                            return true;
                        }
                    }
                    return false;
                }).forEach(arg -> {
                    try {
                        if (ReflectionUtil.getFiled(arg, CONSTANT_LIMIT_GET) == null) {
                            Object defaultLimitOffset = arg.getClass().getDeclaredConstructor().newInstance();
                            ReflectionUtil.setFiled(arg, CONSTANT_LIMIT_SET,
                                ReflectionUtil.getFiled(defaultLimitOffset, CONSTANT_LIMIT_GET), Integer.class);
                        }
                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                    try {
                        if (ReflectionUtil.getFiled(arg, CONSTANT_OFFSET_GET) == null) {
                            Object defaultLimitOffset = arg.getClass().getDeclaredConstructor().newInstance();
                            ReflectionUtil.setFiled(arg, CONSTANT_OFFSET_SET,
                                ReflectionUtil.getFiled(defaultLimitOffset, CONSTANT_OFFSET_GET), Integer.class);
                        }
                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                });
            }
        } catch (Exception e) {
            log.error(this.getClass().getName() + " throw exception:  " + e.getLocalizedMessage());
        }
        return joinPoint.proceed();
    }

}
