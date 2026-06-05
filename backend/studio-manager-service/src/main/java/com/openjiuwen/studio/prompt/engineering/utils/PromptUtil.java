/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ONE;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_TWO;
import static com.openjiuwen.studio.prompt.engineering.constant.CommonConstant.NUM_ZERO;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * prompt信息拼装工具类
 *
 */
@Component
public class PromptUtil {
    /**
     * 组装信息
     *
     * @param str 原始信息
     * @param regex 占位符正则
     * @param startStr 原始信息中的占位符开头
     * @param endStr 原始信息中的占位符结尾
     * @param map 存储待替换信息
     * @return 返回拼装好的信息
     */
    public static String assemblePrompt(String str, String regex, String startStr, String endStr,
            Map<String, String> map) {
        List<String> placeholders = getPlaceholder(str, regex);
        for (String s : placeholders) {
            if (map.containsKey(s) && StringUtils.isNotEmpty(map.get(s))) {
                str = str.replace(startStr + s + endStr, map.get(s));
            }
        }
        return str;
    }

    /**
     * 根据正则，提取字符串中所有符合条件的占位符
     *
     * @param str 原始信息
     * @param regex 特殊占位符正则
     * @return 符合正则的所有子串
     */
    private static List<String> getPlaceholder(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group(1));
        }
        return list;
    }

    /**
     * 格式化double，保留三位小数
     *
     * @param num num
     * @return double
     */
    public static double formatDouble(double num) {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        return Double.parseDouble(decimalFormat.format(num));
    }

    /**
     * temperature [0,1], top_p (0,1], presence_penalty [-2,2]
     *
     * @param modelConfig
     * @return
     */
    public static void validateModelConfig(ModelConfig modelConfig) {
        Double temperature = modelConfig.getTemperature();
        Double topP = modelConfig.getTopP();
        if (topP != null && (topP <= NUM_ZERO || topP > NUM_ONE)) {
            throw new AgentStudioException(StudioError.TEMPLATE_MODEL_CONFIG_VALIDATE_TOP_P, NUM_ZERO, NUM_ONE);
        }
        if (temperature != null && (temperature < NUM_ZERO || temperature > NUM_ONE)) {
            throw new AgentStudioException(StudioError.TEMPLATE_MODEL_CONFIG_VALIDATE_TEMPERATURE, NUM_ZERO, NUM_ONE);
        }
        if (modelConfig.getPresencePenalty() != null && (modelConfig.getPresencePenalty() > NUM_TWO
            || modelConfig.getPresencePenalty() < -NUM_TWO)) {
            throw new AgentStudioException(StudioError.TEMPLATE_MODEL_CONFIG_VALIDATE_PRESENCE_PENALTY, -NUM_TWO,
                NUM_TWO);
        }
    }

}
