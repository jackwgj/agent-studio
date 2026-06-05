/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ListRouterStrategyQo;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyBaseInfo;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyListResponse;
import com.openjiuwen.studio.agent.manager.dto.RouterStrategyRequest;

/**
 * RouterStrategyMgmt service
 */

public interface IRouterStrategyMgmtService {

    /**
     * createRouterStrategy
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    RouterStrategyBaseInfo createRouterStrategy(String projectId, String workspaceId, RouterStrategyRequest body);

    /**
     * deleteRouterStrategy
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param strategyId strategyId
     */
    Void deleteRouterStrategy(String projectId, String workspaceId, String strategyId);

    /**
     * listRouterStrategy
     *
     * @param projectId projectId
     * @param listRouterStrategyQo listRouterStrategyQo
     */
    RouterStrategyListResponse listRouterStrategy(String projectId, ListRouterStrategyQo listRouterStrategyQo);

    /**
     * strategyDetail
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param strategyId strategyId
     */
    RouterStrategyBaseInfo strategyDetail(String projectId, String workspaceId, String strategyId);

    /**
     * updateRouterStrategy
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param strategyId strategyId
     * @param body body
     */
    Void updateRouterStrategy(String projectId, String workspaceId, String strategyId, RouterStrategyRequest body);
}