/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import java.util.List;

@Service("PromptTemplate_RelateResourceCleaner")
public class PromptTemplateRelationCleaner implements IRelateSourceCleaner {


    @Resource
    private PePromptTemplateMapper pePromptTemplateMapper;

    @Override
    public void clean(List<String> ids, String domainId) {
        pePromptTemplateMapper.unbindTemplateIdAndTagIdByTemplateIds(ids);
    }
}
