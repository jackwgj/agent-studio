/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.ExportMessagesParams;
import com.openjiuwen.studio.agent.manager.dto.ListStructuredMessagesQo;
import com.openjiuwen.studio.agent.manager.dto.StructMessage;
import com.openjiuwen.studio.agent.manager.dto.StructMessageListRsp;
import com.openjiuwen.studio.agent.manager.dto.StructuredInfoRequest;
import com.openjiuwen.studio.agent.manager.dto.StructuredInfoRequestDelete;
import com.openjiuwen.studio.agent.manager.dto.StructuredMessagesUploadResult;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * MessageManagement service
 */

public interface IMessageManagementService {

    /**
     * createStructuredMessages
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    StructMessage createStructuredMessages(String projectId, String workspaceId, StructuredInfoRequest body);

    /**
     * deleteStructuredMessages
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    DatasourceBatchDeleteRsp deleteStructuredMessages(String projectId, String workspaceId,
        List<StructuredInfoRequestDelete> body);

    /**
     * exportStructuredMessages
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    Resource exportStructuredMessages(String projectId, String workspaceId, ExportMessagesParams body);

    /**
     * exportStructuredTemplate
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    Resource exportStructuredTemplate(String projectId, String workspaceId);

    /**
     * listStructuredMessages
     *
     * @param projectId projectId
     * @param listStructuredMessagesQo listStructuredMessagesQo
     */
    StructMessageListRsp listStructuredMessages(String projectId, ListStructuredMessagesQo listStructuredMessagesQo);

    /**
     * updateStructuredMessages
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    StructMessage updateStructuredMessages(String projectId, String workspaceId, StructuredInfoRequest body);

    /**
     * uploadStructuredMessages
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param file file
     */
    StructuredMessagesUploadResult uploadStructuredMessages(String workspaceId, String projectId, MultipartFile file);
}