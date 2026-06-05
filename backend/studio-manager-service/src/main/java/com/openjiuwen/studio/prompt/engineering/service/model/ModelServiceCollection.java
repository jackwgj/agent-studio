/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service.model;

import com.openjiuwen.studio.prompt.engineering.dto.InvokeResp;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBody;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 对接大语言模型调优接口
 *
 */
@Component
public class ModelServiceCollection {
    @Resource
    private PromptModelService<?> promptModelService;

    public InvokeResp callModel(String workspqceId, PromptBody promptBody, String xAuthToken, String projectId) {
        return promptModelService.queryModelCenter(workspqceId, promptBody, xAuthToken, projectId);
    }

}
