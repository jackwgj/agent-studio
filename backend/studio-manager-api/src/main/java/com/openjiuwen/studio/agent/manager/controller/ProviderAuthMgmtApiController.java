/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.AuthConfigListQo;
import com.openjiuwen.studio.agent.manager.dto.CreateAuthConfigReq;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthCfgList;
import com.openjiuwen.studio.agent.manager.service.IProviderAuthMgmtService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProviderAuthMgmt controller
 */
@RestController

public class ProviderAuthMgmtApiController implements ProviderAuthMgmtApi {
    private static final Logger log = LoggerFactory.getLogger(ProviderAuthMgmtApiController.class);

    @Autowired
    private IProviderAuthMgmtService providerAuthMgmtService;

    @Override
    public ResponseEntity<ProviderAuthCfgList> authConfigList(String projectId, AuthConfigListQo authConfigListQo) {
        return ResponseModel.success(providerAuthMgmtService.authConfigList(projectId, authConfigListQo));
    }

    @Override
    public ResponseEntity<Void> createAuthConfig(String projectId, String workspaceId, Boolean availableCheck,
        CreateAuthConfigReq body) {
        return ResponseModel.success(
            providerAuthMgmtService.createAuthConfig(projectId, workspaceId, availableCheck, body));
    }

    @Override
    public ResponseEntity<Void> removeAuthConfig(String projectId, String workspaceId, String id) {
        return ResponseModel.success(providerAuthMgmtService.removeAuthConfig(projectId, workspaceId, id));
    }

    @Override
    public ResponseEntity<Void> removeProviderAuthCfg(String projectId, String workspaceId, String providerId,
        String authId) {
        return ResponseModel.success(
            providerAuthMgmtService.removeProviderAuthCfg(projectId, workspaceId, providerId, authId));
    }
}