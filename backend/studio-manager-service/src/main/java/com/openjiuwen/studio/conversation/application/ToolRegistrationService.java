/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.application;

import com.openjiuwen.studio.agent.manager.entity.ToolEntity;
import com.openjiuwen.studio.agent.manager.mapper.ToolMapper;
import com.openjiuwen.studio.conversation.application.dto.ToolSpecDto;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对话工作台内置工具目录注册服务（方案 A2）。
 *
 * <p>Java 在发起对话前向 Python describe 索取本请求所需工具描述，再由本服务幂等写入
 * t_tool：按 {@code tool_id} 主键查询，存在即跳过；不存在则 insert，并发冲突
 * （DuplicateKeyException）视为已存在跳过。内置工具统一 type=inner、
 * project_id=opSvcProjectId、workspace_id="default"、visibility=global。</p>
 */
@Slf4j
@Service
public class ToolRegistrationService {

    private static final String INNER_WORKSPACE_ID = "default";
    private static final String INNER_TYPE = "inner";
    private static final String INNER_VISIBILITY = "global";
    private static final String DEFAULT_INTF_TYPE = "blocking";
    private static final String DEFAULT_CREATOR = "官方预置";
    private static final String DEFAULT_CREATOR_ID = "openjiuwen";

    @Value("${op.svc.project-id}")
    private String opSvcProjectId;

    private final ToolMapper toolMapper;

    public ToolRegistrationService(ToolMapper toolMapper) {
        this.toolMapper = toolMapper;
    }

    /**
     * 幂等注册一批内置工具描述。
     *
     * @param specs Python describe 返回的工具描述列表
     */
    @Transactional
    public void ensureTools(List<ToolSpecDto> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }
        for (ToolSpecDto spec : specs) {
            ensureTool(spec);
        }
    }

    private void ensureTool(ToolSpecDto spec) {
        String toolId = spec == null ? null : spec.getToolId();
        if (StringUtils.isBlank(toolId)) {
            return;
        }
        if (toolMapper.selectByPrimaryKeyAndWorkspace(toolId, opSvcProjectId, INNER_WORKSPACE_ID) != null) {
            return;
        }
        try {
            toolMapper.insert(buildInnerToolEntity(spec));
            log.info("Registered built-in conversation tool: {}", toolId);
        } catch (DuplicateKeyException e) {
            log.warn("Built-in conversation tool already registered concurrently, skip: {}", toolId);
        }
    }

    private ToolEntity buildInnerToolEntity(ToolSpecDto spec) {
        ToolEntity entity = new ToolEntity();
        entity.setToolId(spec.getToolId());
        entity.setProjectId(opSvcProjectId);
        entity.setWorkspaceId(INNER_WORKSPACE_ID);
        entity.setType(INNER_TYPE);
        entity.setToolDisplayName(StringUtils.defaultIfBlank(spec.getToolDisplayName(), spec.getToolId()));
        entity.setToolChineseName(spec.getToolChineseName());
        entity.setToolDesc(StringUtils.defaultIfBlank(spec.getToolDesc(), spec.getToolId()));
        entity.setIntfType(StringUtils.defaultIfBlank(spec.getIntfType(), DEFAULT_INTF_TYPE));
        entity.setInputSchema(spec.getInputSchema());
        entity.setOutputSchema(spec.getOutputSchema());
        entity.setMetadata(spec.getMetadata());
        entity.setVisibility(StringUtils.defaultIfBlank(spec.getVisibility(), INNER_VISIBILITY));
        entity.setPublished(spec.getPublished() == null ? 1 : spec.getPublished());
        entity.setAuthRequired(spec.getAuthRequired() != null && spec.getAuthRequired());
        entity.setIsInputList(spec.getIsInputList());
        entity.setIsOutputList(spec.getIsOutputList());
        entity.setCreator(StringUtils.defaultIfBlank(spec.getCreator(), DEFAULT_CREATOR));
        entity.setCreatorId(StringUtils.defaultIfBlank(spec.getCreatorId(), DEFAULT_CREATOR_ID));
        entity.setCategory(spec.getCategory());
        entity.setTraceId(spec.getToolId());
        return entity;
    }
}
