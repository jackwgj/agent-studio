/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.AppInfo;
import com.openjiuwen.studio.agent.manager.dto.AppListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListAppsQo;
import com.openjiuwen.studio.agent.manager.dto.SearchCriteria;

/**
 * AppManagement service
 */

public interface IAppManagementService {

    /**
     * copyApp
     *
     * @param projectId projectId
     * @param appId appId
     * @param workspaceId workspaceId
     * @param targetWorkspaceId targetWorkspaceId
     * @param body body
     */
    AppInfo copyApp(String projectId, String appId, String workspaceId, String targetWorkspaceId, SearchCriteria body);

    /**
     * listApps
     *
     * @param projectId projectId
     * @param listAppsQo listAppsQo
     */
    AppListRsp listApps(String projectId, ListAppsQo listAppsQo);
}