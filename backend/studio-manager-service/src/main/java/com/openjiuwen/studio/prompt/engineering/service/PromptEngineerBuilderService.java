/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBuilderReq;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 功能描述 提示词生成service
 *
 */
@Slf4j
@Service
public class PromptEngineerBuilderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelManagerService.class);

    private static final long PROMPT_BUILDER_STREAM_TIMEOUT = 120000L;

    private static final int ASYNC_MAX_WAIT_SECONDS = 300;

    private final I18nUtil i18nUtil;

    @Value("${jiuwen.base-url}")
    private String jiuwenBaseUrl;

    @Value("${general.template.url}")
    private String generalTemplateUrl;

    public PromptEngineerBuilderService(I18nUtil i18nUtil) {
        this.i18nUtil = i18nUtil;
    }

    public SseEmitter generatePromptTemplate(String projectId, PromptBuilderReq body) {
        // 该接口已废弃
        return null;
    }
}
