/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ListCustomObjectQo;
import com.openjiuwen.studio.agent.manager.dto.ObjectBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.ObjectInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ObjectListRsp;
import com.openjiuwen.studio.agent.manager.dto.ObjectRsp;

/**
 * CustomObjectManagement service
 */

public interface ICustomObjectManagementService {

    /**
     * createCustomObject
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    ObjectBriefRsp createCustomObject(String projectId, String workspaceId, ObjectInfoReq body);

    /**
     * deleteCustomObject
     *
     * @param projectId projectId
     * @param id id
     * @param workspaceId workspaceId
     */
    ObjectBriefRsp deleteCustomObject(String projectId, String id, String workspaceId);

    /**
     * listCustomObject
     *
     * @param projectId projectId
     * @param listCustomObjectQo listCustomObjectQo
     */
    ObjectListRsp listCustomObject(String projectId, ListCustomObjectQo listCustomObjectQo);

    /**
     * modifyCustomObject
     *
     * @param projectId projectId
     * @param id id
     * @param workspaceId workspaceId
     * @param body body
     */
    ObjectBriefRsp modifyCustomObject(String projectId, String id, String workspaceId, ObjectInfoReq body);

    /**
     * retrieveCustomObject
     *
     * @param projectId projectId
     * @param id id
     * @param workspaceId workspaceId
     */
    ObjectRsp retrieveCustomObject(String projectId, String id, String workspaceId);
}