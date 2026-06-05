/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.stratup;

import com.openjiuwen.studio.prompt.engineering.entity.PePromptTemplate;
import com.openjiuwen.studio.prompt.engineering.entity.PeTag;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.update.UpdatePromptTemplateMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
public class UpdatePromptService {
    @Autowired
    private PePromptTemplateMapper templateMapper;
    @Autowired
    private UpdatePromptTemplateMapper updateMapper;

    public void updatePrompt() {
        // 查询所有模板数据
        List<PePromptTemplate> promptOld = updateMapper.selectAllTemplates();
        List<String> ids = templateMapper.selectAllIds();

        // 遍历列表，将每个对象的 workspaceId 设置为 projectId
        for (PePromptTemplate template : promptOld) {
            // 将 projectId 赋值给 workspaceId
            template.setWorkspaceId(template.getProjectId());
            if (ids.contains(template.getId())) {
                log.info("template {}, id {} is exist", template.getName(), template.getId());
                continue;
            }
            log.info("template {}, id {} insert", template.getName(), template.getId());
            templateMapper.insertOneTemplate(template, template.getProjectId(), template.getIndustry().getId());
            List<String> tagIds = template.getTags() == null ? null : template.getTags().stream().map(PeTag::getId).toList();
            if (!CollectionUtils.isEmpty(tagIds)) {
                templateMapper.bindingTemplateIdAndTagId(template.getId(), tagIds,
                    template.getWorkspaceId());
            }
        }
    }

}
