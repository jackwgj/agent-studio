/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.v2.fileparser;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptVar;
import com.openjiuwen.studio.prompt.engineering.utils.HttpUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class DataSetFileParser {
    abstract public String type();

    abstract public List<List<PromptVar>> parseData(MultipartFile file, Map<String, PromptVar> varsConfig, String projectId, String workspaceId, String taskId, int restNum);

    public void validPromptVarsList(List<List<PromptVar>> promptVarsList, Map<String, PromptVar> varsConfig, int varsNum, int varsContentLength, int varsNameLength, int restNum) {
        if (restNum < 0) {
            log.error("eval data item size is more than MAX !");
            throw new AgentStudioException(StudioError.DATASET_NUMBER_LIMIT);
        }
        if (CollectionUtils.isEmpty(promptVarsList)) {
            return;
        }
        for (List<PromptVar> promptVars : promptVarsList) {
            if (CollectionUtils.isEmpty(promptVars)) {
                log.error("eval data item is empty!");
                throw new AgentStudioException(StudioError.IMPORT_DATA_EMPTY);
            }
            Set<String> varNamesSet = promptVars.stream().map(PromptVar::getName).collect(Collectors.toSet());

            for (Map.Entry<String, PromptVar> entry : varsConfig.entrySet()) {
                if (Strings.CI.equals(CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT, entry.getKey())) {
                    if (varNamesSet.contains(CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT_CN) || varNamesSet.contains(
                        CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT_EN) || varNamesSet.contains(
                        CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT)) {
                        continue;
                    } else {
                        String key;
                        if (Strings.CI.equals(HttpUtil.getLanguage(), CommonConstant.EN_LANGUAGE)) {
                            key = CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT_EN;
                        } else {
                            key = CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT_CN;
                        }
                        log.error("Missing {} variable!", key);
                        throw new AgentStudioException(StudioError.VARS_MISSING, key);
                    }
                }
                if (!varNamesSet.contains(entry.getKey())) {
                    log.error("Missing {} variable!", entry.getKey());
                    throw new AgentStudioException(StudioError.VARS_MISSING, entry.getKey());
                }
            }

            validPromptVars(promptVars, varsNum, varsContentLength, varsNameLength);
        }
    }

    public void validPromptVars(List<PromptVar> promptVars, int varNum, int varsContentLength, int varsNameLength) {
        if (!CollectionUtils.isEmpty(promptVars)) {
            if (promptVars.size() > varNum) {
                log.error("prompt var nums is invalid");
                throw new AgentStudioException(StudioError.VARS_NUM_EXCEED, varNum);
            }
            Set<String> varNamesSet = new HashSet<>();
            for (PromptVar promptVar : promptVars) {
                if (StringUtils.isEmpty(promptVar.getContent())
                    || promptVar.getContent().length() > varsContentLength) {
                    log.error("prompt var content length is invalid");
                    throw new AgentStudioException(StudioError.VARS_CONTENT_LENGTH_EXCEED, varsContentLength);
                }
                if (StringUtils.isEmpty(promptVar.getName()) || promptVar.getName().length() > varsNameLength) {
                    log.error("prompt var is invalid");
                    throw new AgentStudioException(StudioError.VARS_NAME_LENGTH_EXCEED, varsNameLength);
                }
                // 重名校验
                if (varNamesSet.contains(promptVar.getName())) {
                    log.error("prompt var name is Duplicate");
                    throw new AgentStudioException(StudioError.VARS_NAME_DUPLICATE, promptVar.getName());
                }
                varNamesSet.add(promptVar.getName());
            }
        } else {
            log.error("prompt var is invalid");
            throw new AgentStudioException(StudioError.UPLOAD_EVAL_DATA_FAILED);
        }
    }

    public List<List<PromptVar>> clearInvalidVars(List<List<PromptVar>> promptVarsList, Map<String, PromptVar> varConfigMap) {
        List<List<PromptVar>> newPromptVarsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(promptVarsList) && !CollectionUtils.isEmpty(varConfigMap)) {
            for (List<PromptVar> promptVars : promptVarsList) {
                List<PromptVar> newPromptVars = new ArrayList<>();
                if (CollectionUtils.isEmpty(promptVars)) {
                    newPromptVarsList.add(newPromptVars);
                    continue;
                }
                for (PromptVar promptVar : promptVars) {
                    if (Strings.CI.equals(CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT_CN, promptVar.getName())
                        || Strings.CI.equals(CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT_EN,
                        promptVar.getName())) {
                        promptVar.setName(CommonConstant.AGENT_BUILDER_PROMPT_OUTPUT);
                    }
                    if (varConfigMap.containsKey(promptVar.getName())) {
                        newPromptVars.add(promptVar);
                    }
                }
                newPromptVarsList.add(newPromptVars);
            }
        }
        return newPromptVarsList;
    }

}
