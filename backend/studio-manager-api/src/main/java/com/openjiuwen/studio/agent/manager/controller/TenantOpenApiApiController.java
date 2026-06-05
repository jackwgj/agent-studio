/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.DeveloperAgentListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDeveloperAgentsQo;
import com.openjiuwen.studio.agent.manager.service.ITenantOpenApiService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * TenantOpenApi controller
 */
@RestController

public class TenantOpenApiApiController implements TenantOpenApiApi {
    private static final Logger log = LoggerFactory.getLogger(TenantOpenApiApiController.class);

    @Autowired
    private ITenantOpenApiService tenantOpenApiService;

    @Override
    public ResponseEntity<Boolean> existPublishedAgent(String domainId, Long publishedDate) {
        return ResponseModel.success(tenantOpenApiService.existPublishedAgent(domainId, publishedDate));
    }

    @Override
    public ResponseEntity<DeveloperAgentListRsp> listDeveloperAgents(String xUserInfo,
        ListDeveloperAgentsQo listDeveloperAgentsQo) {
        return ResponseModel.success(tenantOpenApiService.listDeveloperAgents(xUserInfo, listDeveloperAgentsQo));
    }
}