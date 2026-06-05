/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import static com.openjiuwen.studio.agent.manager.constant.CommonConstant.ModelParam;
import static com.openjiuwen.studio.agent.manager.enums.ModelTypeV2.IMAGE_TO_TEXT;

import com.openjiuwen.studio.agent.common.utils.SecureRandomUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * IR转换工具类
 *
 */
@Slf4j
public class IRUtils {
    /**
     * 下划线转换驼峰
     *
     * @param originKey 下划线命名法
     * @return 小驼峰命名法
     */
    public static String adaptKey(String originKey) {
        if (StringUtils.isBlank(originKey)) {
            return "";
        }
        int len = originKey.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = Character.toLowerCase(originKey.charAt(i));
            if (c == '_') {
                if (++i < len) {
                    sb.append(Character.toUpperCase(originKey.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 嵌套变量转驼峰，只转换第一个’.‘前的部分
     *
     * @param originKey 下划线命名法
     * @return 小驼峰命名法
     */
    public static String adaptPreDefinedKey(String originKey) {
        if (StringUtils.isBlank(originKey)) {
            return "";
        }
        int len = originKey.length();
        StringBuilder sb = new StringBuilder(len);

        // 转换新增逻辑，对于嵌套变量, abc_sss.sss_abc只转换前半部分，abcSss.sss_abc
        boolean shouldConvert = true;
        for (int i = 0; i < len; i++) {
            char c = originKey.charAt(i);
            if (c == '_' && shouldConvert) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(originKey.charAt(i)));
                }
            } else if (c == '.') {
                shouldConvert = false;
                sb.append(c);
            } else {
                sb.append(shouldConvert ? Character.toLowerCase(c) : c);
            }
        }
        return sb.toString();
    }

    /**
     * 下划线转换驼峰 Map
     *
     * @param mapConfig 下划线命名法列表Map
     * @return 小驼峰命名法Map
     */
    public static Map<String, Object> adaptMapConfig(Map<String, Object> mapConfig) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : mapConfig.entrySet()) {
            result.put(IRUtils.adaptKey(entry.getKey()), mapConfig.get(entry.getKey()));
        }
        return result;
    }

    /**
     * 下划线转换驼峰 List
     *
     * @param listConfig 下划线命名法列表
     * @return 小驼峰命名法列表
     */
    public static List<Map<String, Object>> adaptListConfig(List<Map<String, Object>> listConfig) {
        List<Map<String, Object>> results = new ArrayList<>();
        listConfig.forEach(m -> results.add(IRUtils.adaptMapConfig(m)));
        return results;
    }

    /**
     * 生成一个非绝对安全的随机串（不能用于安全产场景）
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String generateRandomLetters(int length) {
        SecureRandom secureRandom = SecureRandomUtils.getInstance();
        StringBuilder sb = new StringBuilder(length);
        String letters = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(letters.length());
            sb.append(letters.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 将Object转成float
     *
     * @param value 待转换对象
     * @param defaultValue 默认值
     * @return float
     */
    public static float toFloat(Object value, float defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 适配模型extension
     *
     * @param modelConf
     */
    public static void adaptModelExtension(Map<String, Object> modelConf) {
        if (!MapUtils.isEmpty(modelConf)) {
            String modelType = (String) modelConf.get(IRUtils.adaptKey(ModelParam.MODEL_TYPE));
            if (IMAGE_TO_TEXT.toString().equalsIgnoreCase(modelType)) {
                Map<String, Object> extensionMap;
                Object extension = modelConf.get(ModelParam.EXTENSION);
                if (extension != null) {
                    extensionMap = JsonUtils.objectToClass(extension);
                } else {
                    extensionMap = new HashMap<>();
                }
                // 是否视觉模型放到vl_enable
                extensionMap.put(ModelParam.VL_ENABLE, true);
                // vl_enable放到model参数
                modelConf.put(ModelParam.EXTENSION, extensionMap);
            }
        }
    }
}
