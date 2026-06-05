/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Collection集合工具类
 */
public final class CollectionUtil {
    private static final Logger LOG = LoggerFactory.getLogger(CollectionUtil.class);

    /**
     * 私有构造方法
     */
    private CollectionUtil() {

    }

    /**
     * 数组是否为空(判断是否为null 或 长度为0)
     *
     * @param str 输入的数组
     * @return 是否为空 true：为空， false：非空
     */
    public static boolean isEmpty(final String[] str) {
        return (null == str) || (0 == str.length);
    }

    /**
     * 数组是否为空(判断是否为null 或 长度为0)
     *
     * @param array 输入的数组
     * @return 是否为空 true：为空， false：非空
     */
    public static boolean isEmpty(final Object[] array) {
        return (null == array) || (0 == array.length);
    }

    /**
     * 数组是否为空(判断是否为null 或 长度为0)
     *
     * @param array 输入的数组
     * @return 是否为空 true：为空， false：非空
     */
    public static boolean isEmpty(final byte[] array) {
        return (null == array) || (0 == array.length);
    }

    /**
     * 数组是否为空(判断是否为null 或 长度为0)
     *
     * @param collection 输入集合对象
     * @return 是否为空 true：为空， false：非空
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return (null == collection) || collection.isEmpty();
    }

    /**
     * 数组是否不为空(判断是否为null 或 长度为0)
     *
     * @param collection 输入集合对象
     * @return 是否为空 true：为空， false：非空
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * MAP是否为空(判断是否为null 或 长度为0)
     *
     * @param map 输入集合对象
     * @return 是否为空 true：为空， false：非空
     */
    public static boolean isEmpty(final Map<?, ?> map) {
        return (null == map) || map.isEmpty();
    }

    /**
     * MAP是否为空(判断是否为null 或 长度为0)
     *
     * @param map 输入集合对象
     * @return 是否为空 true：为空， false：非空
     */
    public static boolean isNotEmpty(final Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 拼接数组参数
     *
     * @param prefix 数组前半部分
     * @param suffix 数组后半部分
     * @return 完整新数组
     */
    public static Object[] append(Object[] prefix, Object... suffix) {
        // 获取后缀长度信息
        if (ArrayUtils.isEmpty(prefix)) {
            return suffix;
        }

        // 判断前者参数是否为空
        if (ArrayUtils.isEmpty(suffix)) {
            return prefix;
        }

        // 初始化新的数组参数
        Object[] newArray = new Object[prefix.length + suffix.length];

        // 进行数组拷贝处理--拷贝前缀参数
        System.arraycopy(prefix, 0, newArray, 0, prefix.length);

        // 进行数组拷贝处理--拷贝后缀参数
        System.arraycopy(suffix, 0, newArray, prefix.length, suffix.length);

        // 返回新的数组参数
        return newArray;
    }
}
