/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.common.enums.IEnum;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.HashMap;
import java.util.Map;

public class EnumConverterFactory implements ConverterFactory<String, IEnum<?>> {
    @Override
    public <T extends IEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new EnumConverterFactory.EnumConverter<>(targetType);
    }

    private static class EnumConverter<T extends IEnum<?>> implements Converter<String, T> {
        private final Map<String, T> enumMap = new HashMap<>();

        public EnumConverter(Class<T> enumType) {
            T[] enums = enumType.getEnumConstants();
            for (T e : enums) {
                enumMap.put(String.valueOf(e.getValue()), e);
            }
        }

        @Override
        public T convert(String source) {
            return enumMap.get(source);
        }
    }
}