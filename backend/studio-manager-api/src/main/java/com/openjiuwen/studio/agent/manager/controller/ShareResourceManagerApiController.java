/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.QueryShareResourceInfoByResourceIdQo;
import com.openjiuwen.studio.agent.manager.dto.QueryShareResourceListQo;
import com.openjiuwen.studio.agent.manager.dto.QuerySharedResourceListQo;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceListRsp;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceRequestInfo;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceResp;
import com.openjiuwen.studio.agent.manager.service.IShareResourceManagerService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ShareResourceManager controller
 */
@RestController

public class ShareResourceManagerApiController implements ShareResourceManagerApi {
    private static final Logger log = LoggerFactory.getLogger(ShareResourceManagerApiController.class);

    @Autowired
    private IShareResourceManagerService shareResourceManagerService;

    @Override
    public ResponseEntity<Boolean> cancelShareResource(String projectId, String resourceId, String workspaceId,
        String resourceType) {
        return ResponseModel.success(
            shareResourceManagerService.cancelShareResource(projectId, resourceId, workspaceId, resourceType));
    }

    @Override
    public ResponseEntity<Boolean> createOrUpdateShareResource(String projectId, String workspaceId,
        ShareResourceRequestInfo body) {
        return ResponseModel.success(
            shareResourceManagerService.createOrUpdateShareResource(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ShareResourceResp> queryShareResourceInfoByResourceId(String projectId, String resourceId,
        QueryShareResourceInfoByResourceIdQo queryShareResourceInfoByResourceIdQo) {
        return ResponseModel.success(
            shareResourceManagerService.queryShareResourceInfoByResourceId(projectId, resourceId,
                queryShareResourceInfoByResourceIdQo));
    }

    @Override
    public ResponseEntity<ShareResourceListRsp> queryShareResourceList(String projectId,
        QueryShareResourceListQo queryShareResourceListQo) {
        return ResponseModel.success(
            shareResourceManagerService.queryShareResourceList(projectId, queryShareResourceListQo));
    }

    @Override
    public ResponseEntity<ShareResourceListRsp> querySharedResourceList(String projectId,
        QuerySharedResourceListQo querySharedResourceListQo) {
        return ResponseModel.success(
            shareResourceManagerService.querySharedResourceList(projectId, querySharedResourceListQo));
    }
}