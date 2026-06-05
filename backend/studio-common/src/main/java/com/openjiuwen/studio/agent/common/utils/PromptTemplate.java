/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 */
@Slf4j
public class PromptTemplate {
    private final String template;

    /**
     * 初始化
     *
     * @param template 提示词模板
     * @throws AgentStudioException 参数为空
     */
    public PromptTemplate(String template) {
        this.template = template;
    }

    @PostConstruct
    public void validateTemplate() {
        if (StringUtils.isEmpty(template)) {
            log.error("prompt template should not be empty");
            throw new AgentStudioException(StudioError.PROMPT_TEMPLATE_SHOULD_NOT_BE_EMPTY);
        }
    }

    /**
     * 格式化提示词
     *
     * @param kvs kv键值对列表
     * @return 提示词
     */
    public String format(Map<String, Object> kvs) {
        String result;
        Jinjava jinjava = new Jinjava(JinjavaConfig.newBuilder().withNestedInterpretationEnabled(false).build());
        try {
            result = jinjava.render(template, kvs);
        } catch (Exception e) {
            log.error("render template failed, template: {}, kvs: {}", template, kvs, e);
            throw new AgentStudioException(StudioError.PROMPT_RENDER_TEMPLATE_FAILED);
        }
        return result;
    }

    /**
     * 格式化提示词
     *
     * @param kvs kv键值对列表
     * @return 提示词
     */
    public final String format(KV... kvs) {
        Map<String, Object> context = new HashMap<>();
        for (KV kv : kvs) {
            context.put(kv.getKey(), kv.getValue());
        }
        return format(context);
    }

    /**
     * 格式化提示词
     *
     * @param kvs kv键值对列表
     * @return 提示词
     */
    public final String format(List<KV> kvs) {
        Map<String, Object> context = new HashMap<>();
        for (KV kv : kvs) {
            context.put(kv.getKey(), kv.getValue());
        }
        return format(context);
    }
}
