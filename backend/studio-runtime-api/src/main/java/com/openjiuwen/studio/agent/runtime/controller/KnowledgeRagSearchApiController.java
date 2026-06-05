/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
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
import com.openjiuwen.studio.agent.runtime.service.IKnowledgeRagSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class KnowledgeRagSearchApiController implements KnowledgeRagSearchApi {

    @Autowired
    private IKnowledgeRagSearchService knowledgeRagSearchService;

    @Override
    public ResponseEntity<RetrievalKnowledgeBasesResponseBody> searchRagKnowledge(String projectId,
        RetrievalKnowledgeBasesRequestBody body) {
        return ResponseModel.success(knowledgeRagSearchService.searchRagKnowledge(projectId, body));
    }

    @Override
    public ResponseEntity<CreateStudioKnowledgeResponseBody> createStudioKnowledgeBase(
        List<CreateStudioKnowledgeBaseReq> body) {
        return ResponseModel.success(knowledgeRagSearchService.createStudioKnowledgeBase(body));
    }

    @Override
    public ResponseEntity<CreateKnowledgeRouterResponse> createStudioKnowledgeBaseConnection(
        CreateStudioKnowledgeBaseConnectionRequestBody body) {
        return ResponseModel.success(knowledgeRagSearchService.createStudioKnowledgeBaseConnection(body));
    }

    @Override
    public ResponseEntity<DeleteStudioKnowledgeBaseResponseBody> deleteStudioKnowledgeBase(String baseId) {
        return ResponseModel.success(knowledgeRagSearchService.deleteStudioKnowledgeBase(baseId));
    }

    @Override
    public ResponseEntity<Resource> downloadStudioKnowledgeFaqFile(String baseId, String fileId) {
        return ResponseModel.successDownload(knowledgeRagSearchService.downloadStudioKnowledgeFaqFile(baseId, fileId));
    }

    @Override
    public ResponseEntity<Resource> downloadStudioKnowledgeFile(String baseId, String fileId) {
        return ResponseModel.successDownload(knowledgeRagSearchService.downloadStudioKnowledgeFile(baseId, fileId));
    }

    @Override
    public ResponseEntity<Resource> downloadStudioKnowledgeImage(String imageId) {
        return ResponseModel.successDownload(knowledgeRagSearchService.downloadStudioKnowledgeImage(imageId));
    }

    @Override
    public ResponseEntity<Boolean> syncteKnowledgeConnection(List<SyncKnowledgeReq> body) {
        return ResponseModel.success(knowledgeRagSearchService.syncteKnowledgeConnection(body));
    }

    @Override
    public ResponseEntity<UpdateKnowledgeConnectionResponse> updateStudioKnowledgeConnection(String connectionId,
        List<ConnectionParamInfo> body) {
        return ResponseModel.success(knowledgeRagSearchService.updateStudioKnowledgeConnection(connectionId, body));
    }
}