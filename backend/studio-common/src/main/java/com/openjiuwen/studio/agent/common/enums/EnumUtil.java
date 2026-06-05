/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;


/**
 * 枚举工具类
 */
public class EnumUtil {

    public static <E extends Enum<?> & IEnum<?>> E valueOf(Class<E> layeredPackage, String value) {
        for (E enumConstant : layeredPackage.getEnumConstants()) {
            if (value.equals(String.valueOf(enumConstant.getValue()))) {
                return enumConstant;
            }
        }
        return null;
    }

    public static <E extends Enum<?> & IEnum<?>> E nameOf(Class<E> layeredPackage, String name) {
        for (E enumConstant : layeredPackage.getEnumConstants()) {
            if (name.equals(enumConstant.name())) {
                return enumConstant;
            }
        }
        return null;
    }
}