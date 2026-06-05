/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.QueryShareResourceInfoByResourceIdQo;
import com.openjiuwen.studio.agent.manager.dto.QueryShareResourceListQo;
import com.openjiuwen.studio.agent.manager.dto.QuerySharedResourceListQo;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceListRsp;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceRequestInfo;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceResp;

/**
 * ShareResourceManager service
 */

public interface IShareResourceManagerService {

    /**
     * cancelShareResource
     *
     * @param projectId projectId
     * @param resourceId resourceId
     * @param workspaceId workspaceId
     * @param resourceType resourceType
     */
    Boolean cancelShareResource(String projectId, String resourceId, String workspaceId, String resourceType);

    /**
     * createOrUpdateShareResource
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    Boolean createOrUpdateShareResource(String projectId, String workspaceId, ShareResourceRequestInfo body);

    /**
     * queryShareResourceInfoByResourceId
     *
     * @param projectId projectId
     * @param resourceId resourceId
     * @param queryShareResourceInfoByResourceIdQo queryShareResourceInfoByResourceIdQo
     */
    ShareResourceResp queryShareResourceInfoByResourceId(String projectId, String resourceId,
        QueryShareResourceInfoByResourceIdQo queryShareResourceInfoByResourceIdQo);

    /**
     * queryShareResourceList
     *
     * @param projectId projectId
     * @param queryShareResourceListQo queryShareResourceListQo
     */
    ShareResourceListRsp queryShareResourceList(String projectId, QueryShareResourceListQo queryShareResourceListQo);

    /**
     * querySharedResourceList
     *
     * @param projectId projectId
     * @param querySharedResourceListQo querySharedResourceListQo
     */
    ShareResourceListRsp querySharedResourceList(String projectId, QuerySharedResourceListQo querySharedResourceListQo);
}