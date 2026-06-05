/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射工具类
 */
public class ReflectionUtil {
    public static Object getFiled(Object object, String methodName, Class<?>... parameterTypes)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        return method.invoke(object);
    }

    public static void setFiled(Object object, String methodName, Object params, Class<?>... parameterTypes)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.invoke(object, params);
    }
}
