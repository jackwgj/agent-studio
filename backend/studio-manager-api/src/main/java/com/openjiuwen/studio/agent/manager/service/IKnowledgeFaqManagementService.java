/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqReq;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqResp;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateFaqReq;
import com.openjiuwen.studio.agent.manager.dto.CreateFaqResp;
import com.openjiuwen.studio.agent.manager.dto.ListFaqQo;
import com.openjiuwen.studio.agent.manager.dto.ListFaqResp;
import com.openjiuwen.studio.agent.manager.dto.ModifyFaqReq;
import com.openjiuwen.studio.agent.manager.dto.ModifyFaqResp;

/**
 * KnowledgeFaqManagement service
 */

public interface IKnowledgeFaqManagementService {

    /**
     * batchDeleteFaq
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     * @param body body
     */
    BatchDeleteFaqResp batchDeleteFaq(String projectId, String knowledgeRepoId, String workspaceId,
        BatchDeleteFaqReq body);

    /**
     * createFaq
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param knowledgeRepoId knowledgeRepoId
     * @param body body
     */
    CreateFaqResp createFaq(String projectId, String workspaceId, String knowledgeRepoId, CreateFaqReq body);

    /**
     * deleteFaq
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param faqId faqId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteFaq(String projectId, String knowledgeRepoId, String faqId, String workspaceId);

    /**
     * listFaq
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param listFaqQo listFaqQo
     */
    ListFaqResp listFaq(String projectId, String knowledgeRepoId, ListFaqQo listFaqQo);

    /**
     * modifyFaq
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param knowledgeRepoId knowledgeRepoId
     * @param faqId faqId
     * @param body body
     */
    ModifyFaqResp modifyFaq(String projectId, String workspaceId, String knowledgeRepoId, String faqId,
        ModifyFaqReq body);
}