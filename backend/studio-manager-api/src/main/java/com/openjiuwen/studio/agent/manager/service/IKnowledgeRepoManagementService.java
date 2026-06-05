/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateExternalKnowledgeBaseRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateExternalKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.DeleteOneResponseBody;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeSearchRecordRsp;
import com.openjiuwen.studio.agent.manager.dto.ListExternalKnowledgeBasesQo;
import com.openjiuwen.studio.agent.manager.dto.ListExternalKnowledgeBasesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBasesQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBasesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeModelsQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeModelsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeRepoTagsQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeSearchRecordsQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTagsResp;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.PermissionsRequestBody;
import com.openjiuwen.studio.agent.manager.dto.SearchKnowledgeRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.SearchKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.StartKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.StopKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateKnowledgeVisibilityResponse;

/**
 * KnowledgeRepoManagement service
 */

public interface IKnowledgeRepoManagementService {

    /**
     * createExternalKnowledgeBase
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateExternalKnowledgeBaseResponseBody createExternalKnowledgeBase(String projectId, String workspaceId,
        CreateExternalKnowledgeBaseRequestBody body);

    /**
     * createKnowledgeRepo
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateKnowledgeRepoResponseBody createKnowledgeRepo(String projectId, String workspaceId,
        CreateKnowledgeRepoRequestBody body);

    /**
     * createKnowledgeRepoTags
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param knowledgeRepoId knowledgeRepoId
     * @param body body
     */
    CreateKnowledgeRepoTagsRsp createKnowledgeRepoTags(String projectId, String workspaceId, String knowledgeRepoId,
        CreateKnowledgeRepoTagsReq body);

    /**
     * deleteKnowledgeRepo
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     */
    DeleteOneResponseBody deleteKnowledgeRepo(String projectId, String knowledgeRepoId, String workspaceId);

    /**
     * deleteKnowledgeRepoTags
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param knowledgeRepoId knowledgeRepoId
     * @param tagId tagId
     * @param body body
     */
    DeleteKnowledgeRepoTagsRsp deleteKnowledgeRepoTags(String projectId, String workspaceId, String knowledgeRepoId,
        String tagId, DeleteKnowledgeRepoTagsReq body);

    /**
     * deleteKnowledgeSearchRecord
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param recordId recordId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteKnowledgeSearchRecord(String projectId, String knowledgeRepoId, String recordId,
        String workspaceId);

    /**
     * listExternalKnowledgeBases
     *
     * @param projectId projectId
     * @param listExternalKnowledgeBasesQo listExternalKnowledgeBasesQo
     */
    ListExternalKnowledgeBasesResponseBody listExternalKnowledgeBases(String projectId,
        ListExternalKnowledgeBasesQo listExternalKnowledgeBasesQo);

    /**
     * listKnowledgeBases
     *
     * @param projectId projectId
     * @param listKnowledgeBasesQo listKnowledgeBasesQo
     */
    ListKnowledgeBasesResponseBody listKnowledgeBases(String projectId, ListKnowledgeBasesQo listKnowledgeBasesQo);

    /**
     * listKnowledgeModels
     *
     * @param projectId projectId
     * @param listKnowledgeModelsQo listKnowledgeModelsQo
     */
    ListKnowledgeModelsResponseBody listKnowledgeModels(String projectId, ListKnowledgeModelsQo listKnowledgeModelsQo);

    /**
     * listKnowledgeRepoTags
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param listKnowledgeRepoTagsQo listKnowledgeRepoTagsQo
     */
    ListKnowledgeTagsResp listKnowledgeRepoTags(String projectId, String knowledgeRepoId,
        ListKnowledgeRepoTagsQo listKnowledgeRepoTagsQo);

    /**
     * listKnowledgeSearchRecords
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param listKnowledgeSearchRecordsQo listKnowledgeSearchRecordsQo
     */
    KnowledgeSearchRecordRsp listKnowledgeSearchRecords(String projectId, String knowledgeRepoId,
        ListKnowledgeSearchRecordsQo listKnowledgeSearchRecordsQo);

    /**
     * modifyKnowledgeBaseVisibility
     *
     * @param projectId projectId
     * @param knowledgeBaseId knowledgeBaseId
     * @param workspaceId workspaceId
     * @param body body
     */
    UpdateKnowledgeVisibilityResponse modifyKnowledgeBaseVisibility(String projectId, String knowledgeBaseId,
        String workspaceId, PermissionsRequestBody body);

    /**
     * modifyKnowledgeRepo
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     * @param body body
     */
    ModifyKnowledgeRepoResponseBody modifyKnowledgeRepo(String projectId, String knowledgeRepoId, String workspaceId,
        ModifyKnowledgeRepoRequestBody body);

    /**
     * searchKnowledgeRepo
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param offset offset
     * @param limit limit
     * @param body body
     */
    SearchKnowledgeRepoResponseBody searchKnowledgeRepo(String projectId, String workspaceId, Integer offset,
        Integer limit, SearchKnowledgeRepoRequestBody body);

    /**
     * showKnowledgeRepo
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     */
    ShowKnowledgeRepoResponseBody showKnowledgeRepo(String projectId, String knowledgeRepoId, String workspaceId);

    /**
     * startKnowledgeRepo
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     */
    StartKnowledgeRepoResponseBody startKnowledgeRepo(String projectId, String knowledgeRepoId, String workspaceId);

    /**
     * stopKnowledgeRepo
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     */
    StopKnowledgeRepoResponseBody stopKnowledgeRepo(String projectId, String knowledgeRepoId, String workspaceId);
}