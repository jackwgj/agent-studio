/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderListRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderReq;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderRsp;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthInfoReq;
import com.openjiuwen.studio.agent.manager.dto.QueryPlatformProvidersQo;
import com.openjiuwen.studio.agent.manager.dto.QueryUserProvidersQo;
import com.openjiuwen.studio.agent.manager.service.IProviderMgmtService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProviderMgmt controller
 */
@RestController

public class ProviderMgmtApiController implements ProviderMgmtApi {
    private static final Logger log = LoggerFactory.getLogger(ProviderMgmtApiController.class);

    @Autowired
    private IProviderMgmtService providerMgmtService;

    @Override
    public ResponseEntity<String> createModelServiceProviders(String projectId, String workspaceId,
        ModelServiceProviderReq body) {
        return ResponseModel.success(providerMgmtService.createModelServiceProviders(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Void> deleteModelServiceProvider(String projectId, String workspaceId, String id) {
        return ResponseModel.success(providerMgmtService.deleteModelServiceProvider(projectId, workspaceId, id));
    }

    @Override
    public ResponseEntity<ModelServiceProviderRsp> integrationModelServiceProviderDetail(String projectId,
        String workspaceId, String id) {
        return ResponseModel.success(
            providerMgmtService.integrationModelServiceProviderDetail(projectId, workspaceId, id));
    }

    @Override
    public ResponseEntity<ModelServiceProviderRsp> platformModelServiceProviderDetail(String projectId, String id,
        String workspaceId) {
        return ResponseModel.success(
            providerMgmtService.platformModelServiceProviderDetail(projectId, id, workspaceId));
    }

    @Override
    public ResponseEntity<ModelServiceProviderListRsp> queryPlatformProviders(String projectId,
        QueryPlatformProvidersQo queryPlatformProvidersQo) {
        return ResponseModel.success(providerMgmtService.queryPlatformProviders(projectId, queryPlatformProvidersQo));
    }

    @Override
    public ResponseEntity<ModelServiceProviderListRsp> queryUserProviders(String projectId,
        QueryUserProvidersQo queryUserProvidersQo) {
        return ResponseModel.success(providerMgmtService.queryUserProviders(projectId, queryUserProvidersQo));
    }

    @Override
    public ResponseEntity<Void> updateModelServiceProvider(String projectId, String workspaceId, String id,
        Boolean availableCheck, ModelServiceProviderReq body) {
        return ResponseModel.success(
            providerMgmtService.updateModelServiceProvider(projectId, workspaceId, id, availableCheck, body));
    }

    @Override
    public ResponseEntity<Void> updateModelServiceProviderAuthInfo(String projectId, String workspaceId, String id,
        Boolean availableCheck, ProviderAuthInfoReq body) {
        return ResponseModel.success(
            providerMgmtService.updateModelServiceProviderAuthInfo(projectId, workspaceId, id, availableCheck, body));
    }
}