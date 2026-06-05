/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ListRouterStrategyQo;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyBaseInfo;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyListResponse;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyRequest;
import com.openjiuwen.studio.agent.manager.service.IRouterStrategyMgmtService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * RouterStrategyMgmt controller
 */
@RestController

public class RouterStrategyMgmtApiController implements RouterStrategyMgmtApi {
    private static final Logger log = LoggerFactory.getLogger(RouterStrategyMgmtApiController.class);

    @Autowired
    private IRouterStrategyMgmtService routerStrategyMgmtService;

    @Override
    public ResponseEntity<RouterStrategyBaseInfo> createRouterStrategy(String projectId, String workspaceId,
        RouterStrategyRequest body) {
        return ResponseModel.success(routerStrategyMgmtService.createRouterStrategy(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Void> deleteRouterStrategy(String projectId, String workspaceId, String strategyId) {
        return ResponseModel.success(
            routerStrategyMgmtService.deleteRouterStrategy(projectId, workspaceId, strategyId));
    }

    @Override
    public ResponseEntity<RouterStrategyListResponse> listRouterStrategy(String projectId,
        ListRouterStrategyQo listRouterStrategyQo) {
        return ResponseModel.success(routerStrategyMgmtService.listRouterStrategy(projectId, listRouterStrategyQo));
    }

    @Override
    public ResponseEntity<RouterStrategyBaseInfo> strategyDetail(String projectId, String workspaceId,
        String strategyId) {
        return ResponseModel.success(routerStrategyMgmtService.strategyDetail(projectId, workspaceId, strategyId));
    }

    @Override
    public ResponseEntity<Void> updateRouterStrategy(String projectId, String workspaceId, String strategyId,
        RouterStrategyRequest body) {
        return ResponseModel.success(
            routerStrategyMgmtService.updateRouterStrategy(projectId, workspaceId, strategyId, body));
    }
}