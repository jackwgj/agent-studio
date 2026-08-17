/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.agent.manager.bo.SkillDetails;
import com.openjiuwen.studio.agent.manager.dto.SkillStatus;
import com.openjiuwen.studio.agent.manager.entity.SkillEntity;
import com.openjiuwen.studio.agent.manager.mapper.SkillMapper;
import com.openjiuwen.studio.conversation.application.dto.ConversationSkillContext;
import com.openjiuwen.studio.conversation.application.dto.ConversationSkillDescriptor;
import com.openjiuwen.studio.conversation.application.dto.ConversationSkillVo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConversationSkillResolver {
    private static final int PAGE_SIZE = 1000;

    private final SkillMapper skillMapper;

    public ConversationSkillResolver(SkillMapper skillMapper) {
        this.skillMapper = skillMapper;
    }

    public List<ConversationSkillVo> listAvailable(String projectId, String workspaceId, String domainId) {
        return loadCatalog(projectId, workspaceId, domainId).stream()
            .map(item -> ConversationSkillVo.builder()
                .skillId(item.getSkillId())
                .name(item.getName())
                .description(item.getDescription())
                .build())
            .toList();
    }

    public ConversationSkillContext resolveForRun(String projectId, String workspaceId, String domainId,
                                                  List<String> requestedIds) {
        List<ConversationSkillDescriptor> catalog = loadCatalog(projectId, workspaceId, domainId);
        Set<String> availableIds = catalog.stream()
            .map(ConversationSkillDescriptor::getSkillId)
            .collect(Collectors.toSet());
        List<String> recommendedSkillIds = (requestedIds == null ? List.<String>of() : requestedIds).stream()
            .filter(availableIds::contains)
            .toList();
        return new ConversationSkillContext(catalog, recommendedSkillIds);
    }

    private List<ConversationSkillDescriptor> loadCatalog(String projectId, String workspaceId, String domainId) {
        SkillEntity condition = new SkillEntity()
            .setProjectId(projectId)
            .setWorkspaceId(workspaceId)
            .setDomainId(domainId)
            .setStatus(SkillStatus.DEVELOPED.getValue());
        List<ConversationSkillDescriptor> result = new ArrayList<>();
        for (int offset = 0; ; offset += PAGE_SIZE) {
            List<SkillDetails> page = skillMapper.search(condition, offset, PAGE_SIZE, null, null, 0);
            page.stream()
                .filter(item -> Objects.equals(domainId, item.getDomainId()))
                .filter(item -> Objects.equals(projectId, item.getProjectId()))
                .filter(item -> Objects.equals(workspaceId, item.getWorkspaceId()))
                .filter(item -> Objects.equals(SkillStatus.DEVELOPED.getValue(), item.getStatus()))
                .filter(item -> StringUtils.isNotBlank(item.getSkillId()))
                .filter(item -> StringUtils.isNotBlank(item.getLatestVersion()))
                .filter(item -> StringUtils.isNotBlank(item.getObsPath()))
                .map(this::toDescriptor)
                .forEach(result::add);
            if (page.size() < PAGE_SIZE) {
                return result;
            }
        }
    }

    private ConversationSkillDescriptor toDescriptor(SkillDetails skill) {
        return ConversationSkillDescriptor.builder()
            .skillId(skill.getSkillId())
            .versionId(skill.getLatestVersion())
            .name(skill.getName())
            .description(skill.getDescription())
            .objectKey(skill.getObsPath())
            .build();
    }
}
