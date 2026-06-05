/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.DeveloperAgentListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDeveloperAgentsQo;

/**
 * TenantOpenApi service
 */

public interface ITenantOpenApiService {

    /**
     * existPublishedAgent
     *
     * @param domainId domainId
     * @param publishedDate publishedDate
     */
    Boolean existPublishedAgent(String domainId, Long publishedDate);

    /**
     * listDeveloperAgents
     *
     * @param xUserInfo xUserInfo
     * @param listDeveloperAgentsQo listDeveloperAgentsQo
     */
    DeveloperAgentListRsp listDeveloperAgents(String xUserInfo, ListDeveloperAgentsQo listDeveloperAgentsQo);
}