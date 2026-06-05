/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CommonBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeTaskReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksResponseBody;

/**
 * KnowledgeTaskManagement service
 */

public interface IKnowledgeTaskManagementService {

    /**
     * createKnowledgeTask
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param knowledgeRepoId knowledgeRepoId
     * @param body body
     */
    CreateKnowledgeTaskResponseBody createKnowledgeTask(String projectId, String workspaceId, String knowledgeRepoId,
        CreateKnowledgeTaskRequestBody body);

    /**
     * deleteKnowledgeTask
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     * @param body body
     */
    CommonBatchDeleteRsp deleteKnowledgeTask(String projectId, String knowledgeRepoId, String workspaceId,
        DeleteKnowledgeTaskReq body);

    /**
     * listKnowledgeTasks
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param listKnowledgeTasksQo listKnowledgeTasksQo
     */
    ListKnowledgeTasksResponseBody listKnowledgeTasks(String projectId, String knowledgeRepoId,
        ListKnowledgeTasksQo listKnowledgeTasksQo);
}