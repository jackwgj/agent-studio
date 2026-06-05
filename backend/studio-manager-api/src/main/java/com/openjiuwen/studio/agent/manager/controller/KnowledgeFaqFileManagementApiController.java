/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateFileChunkRsp;
import com.openjiuwen.studio.agent.manager.dto.DownloadFaqFileByAccessKeyQo;
import com.openjiuwen.studio.agent.manager.dto.DownloadFaqFileQo;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FaqFileInfoListRsp;
import com.openjiuwen.studio.agent.manager.dto.ListFaqFileChunksQo;
import com.openjiuwen.studio.agent.manager.dto.ListFaqFilesQo;
import com.openjiuwen.studio.agent.manager.dto.UpdateFileChunkRsp;
import com.openjiuwen.studio.agent.manager.dto.UploadFaqFileRsp;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeFaqFileManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * KnowledgeFaqFileManagement controller
 */
@RestController

public class KnowledgeFaqFileManagementApiController implements KnowledgeFaqFileManagementApi {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeFaqFileManagementApiController.class);

    @Autowired
    private IKnowledgeFaqFileManagementService knowledgeFaqFileManagementService;

    @Override
    public ResponseEntity<CreateFileChunkRsp> createFaqFileChunk(String projectId, String knowledgeRepoId,
        String fileId, String workspaceId, FaqFileChunkReq body) {
        return ResponseModel.success(
            knowledgeFaqFileManagementService.createFaqFileChunk(projectId, knowledgeRepoId, fileId, workspaceId,
                body));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteFaqFile(String projectId, String knowledgeRepoId, String fileId,
        String workspaceId) {
        return ResponseModel.success(
            knowledgeFaqFileManagementService.deleteFaqFile(projectId, knowledgeRepoId, fileId, workspaceId));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteFaqFileChunk(String projectId, String knowledgeRepoId, String fileId,
        String chunkId, String workspaceId) {
        return ResponseModel.success(
            knowledgeFaqFileManagementService.deleteFaqFileChunk(projectId, knowledgeRepoId, fileId, chunkId,
                workspaceId));
    }

    @Override
    public ResponseEntity<Resource> downloadFaqFile(String projectId, String knowledgeRepoId, String fileId,
        DownloadFaqFileQo downloadFaqFileQo) {
        return ResponseModel.successDownload(
            knowledgeFaqFileManagementService.downloadFaqFile(projectId, knowledgeRepoId, fileId, downloadFaqFileQo));
    }

    @Override
    public ResponseEntity<Resource> downloadFaqFileByAccessKey(String projectId, String fileId, String knowledgeBaseId,
        DownloadFaqFileByAccessKeyQo downloadFaqFileByAccessKeyQo) {
        return ResponseModel.successDownload(
            knowledgeFaqFileManagementService.downloadFaqFileByAccessKey(projectId, fileId, knowledgeBaseId,
                downloadFaqFileByAccessKeyQo));
    }

    @Override
    public ResponseEntity<FaqFileChunkListRsp> listFaqFileChunks(String projectId, String knowledgeRepoId,
        String fileId, ListFaqFileChunksQo listFaqFileChunksQo) {
        return ResponseModel.success(
            knowledgeFaqFileManagementService.listFaqFileChunks(projectId, knowledgeRepoId, fileId,
                listFaqFileChunksQo));
    }

    @Override
    public ResponseEntity<FaqFileInfoListRsp> listFaqFiles(String projectId, String knowledgeRepoId,
        ListFaqFilesQo listFaqFilesQo) {
        return ResponseModel.success(
            knowledgeFaqFileManagementService.listFaqFiles(projectId, knowledgeRepoId, listFaqFilesQo));
    }

    @Override
    public ResponseEntity<UpdateFileChunkRsp> updateFaqFileChunk(String projectId, String knowledgeRepoId,
        String fileId, String chunkId, String workspaceId, FaqFileChunkReq body) {
        return ResponseModel.success(
            knowledgeFaqFileManagementService.updateFaqFileChunk(projectId, knowledgeRepoId, fileId, chunkId,
                workspaceId, body));
    }

    @Override
    public ResponseEntity<UploadFaqFileRsp> uploadFaqFile(String workspaceId, String projectId, String knowledgeRepoId,
        MultipartFile file) {
        return ResponseModel.success(
            knowledgeFaqFileManagementService.uploadFaqFile(workspaceId, projectId, knowledgeRepoId, file));
    }
}