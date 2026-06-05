/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesRequestBody;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateFileChunkRsp;
import com.openjiuwen.studio.agent.manager.dto.DownloadFileByAccessKeyQo;
import com.openjiuwen.studio.agent.manager.dto.FileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FileInfo;
import com.openjiuwen.studio.agent.manager.dto.ListFileChunksQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeFilesQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.RetrieveFileContentQo;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeFileQo;
import com.openjiuwen.studio.agent.manager.dto.UpdateFileChunkRsp;
import com.openjiuwen.studio.agent.manager.dto.UpdateFileMetaInfoReq;
import com.openjiuwen.studio.agent.manager.dto.UpdateFileMetaInfoRsp;
import com.openjiuwen.studio.agent.manager.dto.UploadKnowledgeFileResponseBody;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeFileManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * KnowledgeFileManagement controller
 */
@RestController

public class KnowledgeFileManagementApiController implements KnowledgeFileManagementApi {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeFileManagementApiController.class);

    @Autowired
    private IKnowledgeFileManagementService knowledgeFileManagementService;

    @Override
    public ResponseEntity<BatchDeleteKnowledgeFilesResponseBody> batchDeleteKnowledgeFiles(String projectId,
        String knowledgeRepoId, String workspaceId, BatchDeleteKnowledgeFilesRequestBody body) {
        return ResponseModel.success(
            knowledgeFileManagementService.batchDeleteKnowledgeFiles(projectId, knowledgeRepoId, workspaceId, body));
    }

    @Override
    public ResponseEntity<CreateFileChunkRsp> createFileChunk(String projectId, String knowledgeRepoId, String fileId,
        String workspaceId, FileChunkReq body) {
        return ResponseModel.success(
            knowledgeFileManagementService.createFileChunk(projectId, knowledgeRepoId, fileId, workspaceId, body));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteFile(String projectId, String fileId, String knowledgeRepoId,
        String workspaceId) {
        return ResponseModel.success(
            knowledgeFileManagementService.deleteFile(projectId, fileId, knowledgeRepoId, workspaceId));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteFileChunk(String projectId, String knowledgeRepoId, String fileId,
        String chunkId, String workspaceId) {
        return ResponseModel.success(
            knowledgeFileManagementService.deleteFileChunk(projectId, knowledgeRepoId, fileId, chunkId, workspaceId));
    }

    @Override
    public ResponseEntity<Resource> downloadFileByAccessKey(String projectId, String fileId, String knowledgeBaseId,
        DownloadFileByAccessKeyQo downloadFileByAccessKeyQo) {
        return ResponseModel.successDownload(
            knowledgeFileManagementService.downloadFileByAccessKey(projectId, fileId, knowledgeBaseId,
                downloadFileByAccessKeyQo));
    }

    @Override
    public ResponseEntity<Resource> downloadKnowledgeImage(String projectId, String knowledgeBaseId, String imageId) {
        return ResponseModel.successDownload(
            knowledgeFileManagementService.downloadKnowledgeImage(projectId, knowledgeBaseId, imageId));
    }

    @Override
    public ResponseEntity<Resource> downloadKnowledgeImageByAccessKey(String projectId, String imageId,
        String workspaceId) {
        return ResponseModel.successDownload(
            knowledgeFileManagementService.downloadKnowledgeImageByAccessKey(projectId, imageId, workspaceId));
    }

    @Override
    public ResponseEntity<FileChunkListRsp> listFileChunks(String projectId, String knowledgeRepoId, String fileId,
        ListFileChunksQo listFileChunksQo) {
        return ResponseModel.success(
            knowledgeFileManagementService.listFileChunks(projectId, knowledgeRepoId, fileId, listFileChunksQo));
    }

    @Override
    public ResponseEntity<ListKnowledgeFilesResponseBody> listKnowledgeFiles(String projectId, String knowledgeRepoId,
        ListKnowledgeFilesQo listKnowledgeFilesQo) {
        return ResponseModel.success(
            knowledgeFileManagementService.listKnowledgeFiles(projectId, knowledgeRepoId, listKnowledgeFilesQo));
    }

    @Override
    public ResponseEntity<Resource> retrieveFileContent(String projectId, String fileId, String knowledgeRepoId,
        RetrieveFileContentQo retrieveFileContentQo) {
        return ResponseModel.successDownload(
            knowledgeFileManagementService.retrieveFileContent(projectId, fileId, knowledgeRepoId,
                retrieveFileContentQo));
    }

    @Override
    public ResponseEntity<FileInfo> showKnowledgeFile(String projectId, String fileId, String knowledgeRepoId,
        ShowKnowledgeFileQo showKnowledgeFileQo) {
        return ResponseModel.success(
            knowledgeFileManagementService.showKnowledgeFile(projectId, fileId, knowledgeRepoId, showKnowledgeFileQo));
    }

    @Override
    public ResponseEntity<UpdateFileChunkRsp> updateFileChunk(String projectId, String knowledgeRepoId, String fileId,
        String chunkId, String workspaceId, FileChunkReq body) {
        return ResponseModel.success(
            knowledgeFileManagementService.updateFileChunk(projectId, knowledgeRepoId, fileId, chunkId, workspaceId,
                body));
    }

    @Override
    public ResponseEntity<UpdateFileMetaInfoRsp> updateFileMetaInfo(String projectId, String knowledgeRepoId,
        String fileId, String workspaceId, UpdateFileMetaInfoReq body) {
        return ResponseModel.success(
            knowledgeFileManagementService.updateFileMetaInfo(projectId, knowledgeRepoId, fileId, workspaceId, body));
    }

    @Override
    public ResponseEntity<UploadKnowledgeFileResponseBody> uploadKnowledgeFile(String workspaceId, String projectId,
        String knowledgeRepoId, MultipartFile file, List<String> tags) {
        return ResponseModel.success(
            knowledgeFileManagementService.uploadKnowledgeFile(workspaceId, projectId, knowledgeRepoId, file, tags));
    }
}