/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.AutoAddResultJsonObject;
import com.openjiuwen.studio.agent.manager.dto.ModelConfig;
import com.openjiuwen.studio.agent.manager.rce.service.JiuWenService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能生成服务
 *
 */
@Service
@Slf4j
public class AiGenService {
    @Autowired
    private AgentCommonService agentCommonService;

    @Autowired
    private JiuWenService jiuWenService;

    public AutoAddResultJsonObject generatePrologue(String modelDeploymentId, String modelType, String model,
        ModelConfig config, String query, String workspaceId) {
        // 调用模型并解析返回结果
        String prologue = null;
        try {
            for (int callCounts = 0; callCounts < CommonConstant.MODEL_CALLS; callCounts++) {
                prologue = jiuWenService.callModel(
                    agentCommonService.buildAskModelReq(modelDeploymentId, modelType, model, config, query),
                    workspaceId);
                if (!StringUtils.isBlank(prologue)) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Call model failed.", e);
            throw new AgentStudioException(StudioError.CALL_MODEL_ERROR);
        }

        if (StringUtils.isBlank(prologue)) {

            boolean isChinese = LanguageUtils.isChinese();

            return new AutoAddResultJsonObject()
                .setPrologue(isChinese ? CommonConstant.PROLOGUE : CommonConstant.PROLOGUE_EN);
        }
        prologue = prologue.trim();
        return new AutoAddResultJsonObject().setPrologue(prologue);
    }

    public AutoAddResultJsonObject recommendedQuestions(String modelDeploymentId, String modelType, String model,
        ModelConfig config, String query, String workspaceId) {
        // 调用模型并解析返回结果
        List<String> questionList = new ArrayList<>();
        try {
            for (int callCounts = 0; callCounts < CommonConstant.MODEL_CALLS; callCounts++) {
                questionList = JsonUtils.json2Obj(jiuWenService.callModel(
                    agentCommonService.buildAskModelReq(modelDeploymentId, modelType, model, config, query),
                    workspaceId), new TypeReference<List<String>>() {});
                if (!CollectionUtils.isEmpty(questionList)) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Call model failed.", e);
            throw new AgentStudioException(StudioError.CALL_MODEL_ERROR);
        }

        if (org.springframework.util.CollectionUtils.isEmpty(questionList)) {
            return new AutoAddResultJsonObject().setQuestions(new ArrayList<>());
        }
        if (questionList.size() > CommonConstant.MAX_QUESTION_NUMS) {
            questionList = questionList.subList(0, CommonConstant.MAX_QUESTION_NUMS);
        }

        return new AutoAddResultJsonObject().setQuestions(questionList);
    }
}
