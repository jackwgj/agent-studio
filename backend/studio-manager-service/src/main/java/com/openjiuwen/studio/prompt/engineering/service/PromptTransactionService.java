/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.entity.PePromptTemplate;
import com.openjiuwen.studio.prompt.engineering.entity.PeTag;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 事务封装类
 *
 */
@Component
public class PromptTransactionService {

    @Resource
    private PePromptTemplateMapper pePromptTemplateMapper;

    /**
     * 新增模板
     *
     * @param pePromptTemplate pePromptTemplate
     * @param tagIdsByTaskId tagIdsByTaskId
     */
    @Transactional
    public void insertOneTemplate(PePromptTemplate pePromptTemplate, List<String> tagIdsByTaskId, String projectId) {
        pePromptTemplateMapper.insertOneTemplate(pePromptTemplate, projectId, pePromptTemplate.getIndustry().getId());
        if (!CollectionUtils.isEmpty(tagIdsByTaskId)) {
            pePromptTemplateMapper.bindingTemplateIdAndTagId(pePromptTemplate.getId(), tagIdsByTaskId, pePromptTemplate.getWorkspaceId());
        }
    }

    @Transactional
    public void batchInsertTemplate(List<PePromptTemplate> pePromptTemplates, String projectId) {
        for (PePromptTemplate pePromptTemplate : pePromptTemplates) {
            pePromptTemplateMapper.insertOneTemplate(pePromptTemplate, projectId, pePromptTemplate.getIndustry().getId());
            if (!CollectionUtils.isEmpty(pePromptTemplate.getTags())) {
                List<String> tagIds = pePromptTemplate.getTags().stream().map(PeTag::getId).collect(Collectors.toList());
                pePromptTemplateMapper.bindingTemplateIdAndTagId(pePromptTemplate.getId(), tagIds, pePromptTemplate.getWorkspaceId());
            }
        }
    }
}
