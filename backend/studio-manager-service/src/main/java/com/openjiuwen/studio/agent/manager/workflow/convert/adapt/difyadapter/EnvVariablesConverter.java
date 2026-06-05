/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.enums.DifyEnvVariablesType;
import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.models.DifyEnvironmentVariableVO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnvVariablesConverter {
    /**
     * 解析 Dify 的环境变量
     */
    @SuppressWarnings("unchecked")
    public static Map<String, DifyEnvironmentVariableVO> parseEnvVariables(List<Map<String, Object>> envVariablesList) {
        if (envVariablesList == null || envVariablesList.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, DifyEnvironmentVariableVO> result = new HashMap<>();

        for (Map<String, Object> rawEnvVariable : envVariablesList) {
            DifyEnvironmentVariableVO envVariable = new DifyEnvironmentVariableVO();

            // 提取并设置基础属性
            envVariable.setName(String.valueOf(rawEnvVariable.get("name")));
            envVariable.setDescription(String.valueOf(rawEnvVariable.getOrDefault("description", "")));
            envVariable.setValueType(DifyEnvVariablesType.valueOf(rawEnvVariable.get("value_type").toString().toUpperCase(
                Locale.ROOT)));
            envVariable.setValue(rawEnvVariable.get("value"));

            // 处理 Selector
            Object selectorObj = rawEnvVariable.get("selector");
            if (selectorObj instanceof List) {
                List<String> selector = (List<String>) selectorObj;

                // 生成唯一引用键
                String refKey = String.join(".", selector);
                envVariable.setRefKey(refKey);

                result.put(refKey, envVariable);
            }
        }
        return result;
    }
}
