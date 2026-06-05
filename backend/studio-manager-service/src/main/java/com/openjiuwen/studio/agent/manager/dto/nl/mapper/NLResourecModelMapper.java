/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.nl.mapper;

import com.openjiuwen.studio.agent.manager.dto.KnowledgeBaseListItem;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVersionListItem;
import com.openjiuwen.studio.agent.manager.dto.nl.KnowledgeResource;
import com.openjiuwen.studio.agent.manager.dto.nl.WorkflowResource;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * NLResourecModelMapper
 *
 */
@Mapper
public interface NLResourecModelMapper {
    /**
     * INSTANCE
     */
    NLResourecModelMapper INSTANCE = Mappers.getMapper(NLResourecModelMapper.class);

    /**
     * toKnowledgeResources
     *
     * @param knowledgeBaseListItems knowledgeBaseListItems
     * @return List<KnowledgeResource>
     */
    List<KnowledgeResource> toKnowledgeResources(List<KnowledgeBaseListItem> knowledgeBaseListItems);

    /**
     * toKnowledgeResource
     *
     * @param knowledgeBaseListItem knowledgeBaseListItem
     * @return KnowledgeResource
     */
    @Mapping(target = "knowledgeBaseId", source = "knowledgeBaseId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    KnowledgeResource toKnowledgeResource(KnowledgeBaseListItem knowledgeBaseListItem);

    /**
     * toWorkflowResources
     *
     * @param workflowVersionListItems workflowVersionListItems
     * @return List<WorkflowResource>
     */
    List<WorkflowResource> toWorkflowResources(List<WorkflowVersionListItem> workflowVersionListItems);

    /**
     * toWorkflowResource
     *
     * @param workflowVersionListItem workflowVersionListItem
     * @return WorkflowResource
     */
    @Mapping(target = "workflowId", source = "workflowId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "lastVersionId", ignore = true)
    @Mapping(target = "resourceId", ignore = true)
    WorkflowResource toWorkflowResource(WorkflowVersionListItem workflowVersionListItem);
}
