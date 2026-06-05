/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderListRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderReq;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderRsp;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthInfoReq;
import com.openjiuwen.studio.agent.manager.dto.QueryPlatformProvidersQo;
import com.openjiuwen.studio.agent.manager.dto.QueryUserProvidersQo;

/**
 * ProviderMgmt service
 */

public interface IProviderMgmtService {

    /**
     * createModelServiceProviders
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    String createModelServiceProviders(String projectId, String workspaceId, ModelServiceProviderReq body);

    /**
     * deleteModelServiceProvider
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param id id
     */
    Void deleteModelServiceProvider(String projectId, String workspaceId, String id);

    /**
     * integrationModelServiceProviderDetail
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param id id
     */
    ModelServiceProviderRsp integrationModelServiceProviderDetail(String projectId, String workspaceId, String id);

    /**
     * platformModelServiceProviderDetail
     *
     * @param projectId projectId
     * @param id id
     * @param workspaceId workspaceId
     */
    ModelServiceProviderRsp platformModelServiceProviderDetail(String projectId, String id, String workspaceId);

    /**
     * queryPlatformProviders
     *
     * @param projectId projectId
     * @param queryPlatformProvidersQo queryPlatformProvidersQo
     */
    ModelServiceProviderListRsp queryPlatformProviders(String projectId,
        QueryPlatformProvidersQo queryPlatformProvidersQo);

    /**
     * queryUserProviders
     *
     * @param projectId projectId
     * @param queryUserProvidersQo queryUserProvidersQo
     */
    ModelServiceProviderListRsp queryUserProviders(String projectId, QueryUserProvidersQo queryUserProvidersQo);

    /**
     * updateModelServiceProvider
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param id id
     * @param availableCheck availableCheck
     * @param body body
     */
    Void updateModelServiceProvider(String projectId, String workspaceId, String id, Boolean availableCheck,
        ModelServiceProviderReq body);

    /**
     * updateModelServiceProviderAuthInfo
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param id id
     * @param availableCheck availableCheck
     * @param body body
     */
    Void updateModelServiceProviderAuthInfo(String projectId, String workspaceId, String id, Boolean availableCheck,
        ProviderAuthInfoReq body);
}