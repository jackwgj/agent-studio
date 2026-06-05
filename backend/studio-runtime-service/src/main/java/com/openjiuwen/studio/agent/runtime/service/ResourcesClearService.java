/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.utils.PublishUtils;
import com.openjiuwen.studio.agent.runtime.dto.ResourceClearResp;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * 资源清理
 *
 */
@Service
@Slf4j
public class ResourcesClearService implements IResourcesCleanerService {
    private final PublishUtils publishUtils;

    private final AgentIrCacheService agentIrCacheService;

    private final WorkflowIrCacheService workflowIrCacheService;

    private final ObsService obsService;

    public ResourcesClearService(AgentIrCacheService agentIrCacheService,
        WorkflowIrCacheService workflowIrCacheService, PublishUtils publishUtils, ObsService obsService) {
        this.agentIrCacheService = agentIrCacheService;
        this.workflowIrCacheService = workflowIrCacheService;
        this.publishUtils = publishUtils;
        this.obsService = obsService;
    }

    /**
     * 清理指定资源相关的内容
     *
     * @param projectId projectId 项目 id
     * @param resourceId resourceId 资源 id
     * @param type type 资源类型：agent, workflow, controller
     * @return 清理结果
     */
    @Override
    public ResourceClearResp clearResource(String projectId, String resourceId, String type) {
        if (Constants.AppType.AGENT.equals(type) || Constants.AppType.CONTROLLER.equals(type)) {
            agentIrCacheService.clearAgentCache(resourceId);
        } else if (Constants.AppType.WORKFLOW.equals(type)) {
            workflowIrCacheService.clearWorkflowCache(resourceId);
        }
        publishUtils.deletePublish(resourceId, Constants.LATEST_PUBLISH_VERSION, obsService);
        return new ResourceClearResp().setId(resourceId);
    }
}
