/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.dto.VariableDto;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParamBodyUtil {
    /**
     * 检查模板请求参数中变量是否重复
     *
     * @param variableList<VariableDto> variableList
     */
    public static void checkUniqueVariable(List<VariableDto> variableList) {
        List<String> variables = variableList.stream().map(VariableDto::getKey).collect(Collectors.toList());
        long count = variables.stream().distinct().count();
        if (variables.size() != count) {
            throw new AgentStudioException(StudioError.VARIABLE_DUPLICATE);
        }
    }
}
