/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;


import com.openjiuwen.studio.agent.runtime.dto.ConnectionParamInfo;
import com.openjiuwen.studio.agent.runtime.dto.CreateKnowledgeRouterResponse;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeBaseReq;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteStudioKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.RetrievalKnowledgeBasesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.RetrievalKnowledgeBasesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.SyncKnowledgeReq;
import com.openjiuwen.studio.agent.runtime.dto.UpdateKnowledgeConnectionResponse;

import org.springframework.core.io.Resource;

import java.util.List;

public interface IKnowledgeRagSearchService {

    /**
     * createStudioKnowledgeBase
     *
     * @param body body
     */
    CreateStudioKnowledgeResponseBody createStudioKnowledgeBase(List<CreateStudioKnowledgeBaseReq> body);

    /**
     * createStudioKnowledgeBaseConnection
     *
     * @param body body
     */
    CreateKnowledgeRouterResponse createStudioKnowledgeBaseConnection(
        CreateStudioKnowledgeBaseConnectionRequestBody body);

    /**
     * deleteStudioKnowledgeBase
     *
     * @param baseId baseId
     */
    DeleteStudioKnowledgeBaseResponseBody deleteStudioKnowledgeBase(String baseId);

    /**
     * downloadStudioKnowledgeFaqFile
     *
     * @param baseId baseId
     * @param fileId fileId
     */
    Resource downloadStudioKnowledgeFaqFile(String baseId, String fileId);

    /**
     * downloadStudioKnowledgeFile
     *
     * @param baseId baseId
     * @param fileId fileId
     */
    Resource downloadStudioKnowledgeFile(String baseId, String fileId);

    /**
     * downloadStudioKnowledgeImage
     *
     * @param imageId imageId
     */
    Resource downloadStudioKnowledgeImage(String imageId);


    /**
     * syncteKnowledgeConnection
     *
     * @param body body
     */
    Boolean syncteKnowledgeConnection(List<SyncKnowledgeReq> body);

    /**
     * updateStudioKnowledgeConnection
     *
     * @param connectionId connectionId
     * @param body body
     */
    UpdateKnowledgeConnectionResponse updateStudioKnowledgeConnection(String connectionId, List<ConnectionParamInfo> body);
    /**
     * searchRagKnowledge
     *
     * @param projectId projectId
     * @param body body
     */
    RetrievalKnowledgeBasesResponseBody searchRagKnowledge(String projectId, RetrievalKnowledgeBasesRequestBody body);


}