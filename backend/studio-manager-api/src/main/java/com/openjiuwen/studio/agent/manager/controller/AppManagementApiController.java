/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.AppInfo;
import com.openjiuwen.studio.agent.manager.dto.AppListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListAppsQo;
import com.openjiuwen.studio.agent.manager.dto.SearchCriteria;
import com.openjiuwen.studio.agent.manager.service.IAppManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * AppManagement controller
 */
@RestController

public class AppManagementApiController implements AppManagementApi {
    private static final Logger log = LoggerFactory.getLogger(AppManagementApiController.class);

    @Autowired
    private IAppManagementService appManagementService;

    @Override
    public ResponseEntity<AppInfo> copyApp(String projectId, String appId, String workspaceId, String targetWorkspaceId,
        SearchCriteria body) {
        return ResponseModel.success(
            appManagementService.copyApp(projectId, appId, workspaceId, targetWorkspaceId, body));
    }

    @Override
    public ResponseEntity<AppListRsp> listApps(String projectId, ListAppsQo listAppsQo) {
        return ResponseModel.success(appManagementService.listApps(projectId, listAppsQo));
    }
}