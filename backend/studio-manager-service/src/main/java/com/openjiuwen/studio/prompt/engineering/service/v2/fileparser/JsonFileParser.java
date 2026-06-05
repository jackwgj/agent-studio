/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.fileparser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class JsonFileParser extends DataSetFileParser {


    @Value("${prompt.evaldata.vars.num: 20}")
    private int varsNum;

    @Value("${prompt.evaldata.vars.namelength: 50}")
    private int varsNameLength;

    @Value("${prompt.evaldata.vars.contentlength: 2000}")
    private int varsContentLength;


    @Override
    public String type() {
        return "json";
    }

    @Override
    public List<List<PromptVar>> parseData(MultipartFile file, Map<String, PromptVar> varsConfig, String projectId,
        String workspaceId, String taskId, int restNum) {
        List<List<PromptVar>> promptVarsList = null;
        try {
            // 读取JSON文件内容为字符串
            String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (jsonContent.isBlank()) {
                throw new IllegalArgumentException("JSON文件内容为空");
            }
            // JSON反序列化为二维数组
            promptVarsList = JsonUtils.json2Obj(jsonContent,
                new TypeReference<List<List<PromptVar>>>() { });
            if (CollectionUtils.isEmpty(promptVarsList)) {
                throw new AgentStudioException(StudioError.PROMPT_EVAL_JSON_FILE_ERROR);
            }
        } catch (Exception e) {
            throw new AgentStudioException(StudioError.PROMPT_EVAL_JSON_FILE_ERROR);
        }
        // 校验提示词变量
        validPromptVarsList(promptVarsList, varsConfig, varsNum, varsContentLength, varsNameLength, restNum - promptVarsList.size());
        // 去除无关变量
        promptVarsList = clearInvalidVars(promptVarsList, varsConfig);

        return promptVarsList;
    }
}
