/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.share;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.ResourceVersionInfo;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareScopeEntity;
import com.openjiuwen.studio.agent.manager.mapper.ShareResourceMapper;
import com.openjiuwen.studio.agent.manager.mapper.ShareScopeMapper;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;
import com.openjiuwen.studio.agent.manager.utils.StringComparisonUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class ShareInnerService {
    @Autowired
    private ShareScopeMapper shareScopeMapper;

    @Autowired
    private ShareResourceMapper shareResourceMapper;

    @Resource
    private McpServiceDao serviceDao;

    public List<ShareScopeEntity> getShareScopeByWorkspaceAndId(List<String> resourceId, String workspaceId) {
        return shareScopeMapper.selectShareScopesByResourceIdsAndWorkspaceId(resourceId, workspaceId);
    }

    public List<ShareResourceEntity> getOriginSharedInfoByResourceId(List<String> resourceIds) {
        return shareResourceMapper.selectShareResourceByResourceIds(resourceIds);
    }

    public boolean cancelPluginShared(String projectId, String workspaceId, String pluginId) {
        ShareResourceEntity shareResource = shareResourceMapper.selectShareResourceEntityById(projectId, workspaceId,
            pluginId);
        if (ObjectUtils.isNotEmpty(shareResource)) {
            log.error("The plugin {} has a shared record in the workspace {} and cannot be deleted directly.", pluginId,
                workspaceId);
            throw new AgentStudioException(StudioError.SHARE_RESOURCE_CANNOT_BE_DELETE_DIRECTLY);
        }
        return true;
    }

    public boolean cancelPluginVersionShared(String projectId, String workspaceId, String pluginId, String versionId) {
        ShareResourceEntity shareResource = shareResourceMapper.selectShareResourceEntityById(projectId, workspaceId,
            pluginId);
        if (ObjectUtils.isEmpty(shareResource)) {
            return true;
        }
        List<ResourceVersionInfo> versionInfos = JSONObject.parseObject(shareResource.getVersionList(),
            new TypeReference<>() { });
        versionInfos.forEach(versionInfo -> {
            if (StringComparisonUtils.equalsIgnoreCase(versionInfo.getVersionId(), versionId)){
                log.error("The version {} of plugin {} has been shared  and cannot be deleted directly.", versionId,
                    pluginId);
                throw new AgentStudioException(StudioError.SHARE_RESOURCE_CANNOT_BE_DELETE_DIRECTLY);
            }
        });
        return true;
    }
    public boolean cancelMCPShared(String id, String projectId, String workspaceId) {
        ShareResourceEntity shareResource = shareResourceMapper.selectShareResourceEntityById(projectId, workspaceId,
            id);
        if (ObjectUtils.isNotEmpty(shareResource)) {
            log.error("The MCP {} has a shared record and cannot be deleted directly.", id);
            throw new AgentStudioException(StudioError.SHARE_RESOURCE_CANNOT_BE_DELETE_DIRECTLY);
        }
        return true;
    }

    public List<McpServiceEntity> getShareMcp(List<String> serviceIds, String workspaceId) {
        List<ShareScopeEntity> shareScopeEntities = getShareScopeByWorkspaceAndId(serviceIds, workspaceId);
        if (CollectionUtils.isEmpty(shareScopeEntities)) {
            return new ArrayList<>();
        }
        List<String> shareServiceIds = shareScopeEntities.stream().map(ShareScopeEntity::getResourceId).toList();
        List<ShareResourceEntity> shareResourceEntities = getOriginSharedInfoByResourceId(shareServiceIds);

        return serviceDao.listMcpServiceByShareResourceEntity(shareResourceEntities);
    }

    public boolean isShared(String resourceId, String workspaceId) {
        List<ShareScopeEntity> shareScopeEntities = getShareScopeByWorkspaceAndId(List.of(resourceId), workspaceId);
        if (!CollectionUtils.isEmpty(shareScopeEntities)) {
            return true;
        }
        return false;
    }
}
