/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static com.openjiuwen.studio.agent.common.enums.StudioError.RESOURCE_NOT_EXISTS;

import com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseServiceImpl;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBasesQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBasesResponseBody;
import com.openjiuwen.studio.agent.manager.entity.CommonMeta;
import com.openjiuwen.studio.agent.manager.enums.ResourceTypeEnum;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServiceMapper;
import com.openjiuwen.studio.agent.manager.mapper.MemoryRepoMapper;
import com.openjiuwen.studio.agent.manager.mapper.SkillMapper;
import com.openjiuwen.studio.agent.manager.mapper.ToolMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ModelServiceMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 资源服务
 *
 */
@Service
@Slf4j
public class ResourceService {

    @Autowired
    private ModelServiceMapper modelServiceMapper;

    @Autowired
    private McpServiceMapper mcpServiceMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private WorkflowMapper workflowMapper;

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private MemoryRepoMapper memoryRepoMapper;

    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private KnowledgeBaseServiceImpl knowledgeBaseService;

    /**
     * 校验资源是否存在
     *
     * @param ids id列表
     * @param type 资源类型
     * @param projectId 项目id
     * @param workspaceId 空间id
     */
    public void validResourceExist(List<String> ids, String type, String projectId, String workspaceId) {
        Map<String, CommonMeta> map = getResourceMeta(ids, type, projectId, workspaceId);
        for (String id : ids) {
            if (map == null || !map.containsKey(id)) {
                log.error("Resource:{}, type:{}, projectId:{}, workspaceId:{} not exists.", id, type, projectId,
                    workspaceId);
                throw new AgentStudioException(RESOURCE_NOT_EXISTS, id);
            }
        }
    }

    /**
     * 获取资源元数据
     *
     * @param ids 资源ids
     * @param type 资源类型
     * @param projectId 项目id
     * @param workspaceId 空间id
     * @return 返回资源元数据
     */
    public Map<String, CommonMeta> getResourceMeta(List<String> ids, String type, String projectId,
        String workspaceId) {
        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.fromValue(type);
        if (resourceTypeEnum == null) {
            log.error("resourceType:{} not valid.", type);
            throw new IllegalArgumentException();
        }
        switch (resourceTypeEnum) {
            case CONTROLLER, AGENT -> {
                return CommonMeta
                    .fromAgents(agentMapper.selectByIdsAndProjectIdAndWorkspaceId(projectId, workspaceId, ids));
            }
            case WORKFLOW -> {
                return CommonMeta.fromWorkflows(workflowMapper.getWorkflowEntityByIds(projectId, workspaceId, ids));
            }
            case TOOL -> {
                return CommonMeta.fromTools(toolMapper.selectByToolIdsAndWorkspaceId(ids, projectId, workspaceId), workspaceId);
            }
            case MCP -> {
                return CommonMeta.fromMcps(mcpServiceMapper.selectByIdsAndWorkspace(ids, projectId, workspaceId));
            }
            case REPO -> {
                ListKnowledgeBasesQo listKnowledgeBasesQo = new ListKnowledgeBasesQo();
                listKnowledgeBasesQo.setWorkspaceId(workspaceId);
                listKnowledgeBasesQo.setKnowledgeBaseIds(ids);
                ListKnowledgeBasesResponseBody resp = knowledgeBaseService.listKnowledgeBases(
                    RequestContextUtils.getRequestProjectId(), listKnowledgeBasesQo);
                return CommonMeta.fromRepos(resp != null ? resp.getItems() : Collections.emptyList(), projectId);
            }
            case MODEL -> {
                return CommonMeta.fromModels(modelServiceMapper.queryByIds(ids, projectId, workspaceId));
            }
            case MEMORY -> {
                return CommonMeta.fromMemoryRepos((memoryRepoMapper.selectByCondition(projectId, workspaceId, null, ids, null)));
            }
            case SKILL -> {
                return CommonMeta.fromSkills(skillMapper.searchBySkillIds(ids));
            }
        }
        return null;
    }
}
