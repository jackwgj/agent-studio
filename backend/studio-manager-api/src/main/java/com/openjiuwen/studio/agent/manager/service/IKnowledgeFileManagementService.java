/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

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

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * KnowledgeFileManagement service
 */

public interface IKnowledgeFileManagementService {

    /**
     * batchDeleteKnowledgeFiles
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     * @param body body
     */
    BatchDeleteKnowledgeFilesResponseBody batchDeleteKnowledgeFiles(String projectId, String knowledgeRepoId,
        String workspaceId, BatchDeleteKnowledgeFilesRequestBody body);

    /**
     * createFileChunk
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateFileChunkRsp createFileChunk(String projectId, String knowledgeRepoId, String fileId, String workspaceId,
        FileChunkReq body);

    /**
     * deleteFile
     *
     * @param projectId projectId
     * @param fileId fileId
     * @param knowledgeRepoId knowledgeRepoId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteFile(String projectId, String fileId, String knowledgeRepoId, String workspaceId);

    /**
     * deleteFileChunk
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param chunkId chunkId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteFileChunk(String projectId, String knowledgeRepoId, String fileId, String chunkId,
        String workspaceId);

    /**
     * downloadFileByAccessKey
     *
     * @param projectId projectId
     * @param fileId fileId
     * @param knowledgeBaseId knowledgeBaseId
     * @param downloadFileByAccessKeyQo downloadFileByAccessKeyQo
     */
    Resource downloadFileByAccessKey(String projectId, String fileId, String knowledgeBaseId,
        DownloadFileByAccessKeyQo downloadFileByAccessKeyQo);

    /**
     * downloadKnowledgeImage
     *
     * @param projectId projectId
     * @param knowledgeBaseId knowledgeBaseId
     * @param imageId imageId
     */
    Resource downloadKnowledgeImage(String projectId, String knowledgeBaseId, String imageId);

    /**
     * downloadKnowledgeImageByAccessKey
     *
     * @param projectId projectId
     * @param imageId imageId
     * @param workspaceId workspaceId
     */
    Resource downloadKnowledgeImageByAccessKey(String projectId, String imageId, String workspaceId);

    /**
     * listFileChunks
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param listFileChunksQo listFileChunksQo
     */
    FileChunkListRsp listFileChunks(String projectId, String knowledgeRepoId, String fileId,
        ListFileChunksQo listFileChunksQo);

    /**
     * listKnowledgeFiles
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param listKnowledgeFilesQo listKnowledgeFilesQo
     */
    ListKnowledgeFilesResponseBody listKnowledgeFiles(String projectId, String knowledgeRepoId,
        ListKnowledgeFilesQo listKnowledgeFilesQo);

    /**
     * retrieveFileContent
     *
     * @param projectId projectId
     * @param fileId fileId
     * @param knowledgeRepoId knowledgeRepoId
     * @param retrieveFileContentQo retrieveFileContentQo
     */
    Resource retrieveFileContent(String projectId, String fileId, String knowledgeRepoId,
        RetrieveFileContentQo retrieveFileContentQo);

    /**
     * showKnowledgeFile
     *
     * @param projectId projectId
     * @param fileId fileId
     * @param knowledgeRepoId knowledgeRepoId
     * @param showKnowledgeFileQo showKnowledgeFileQo
     */
    FileInfo showKnowledgeFile(String projectId, String fileId, String knowledgeRepoId,
        ShowKnowledgeFileQo showKnowledgeFileQo);

    /**
     * updateFileChunk
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param chunkId chunkId
     * @param workspaceId workspaceId
     * @param body body
     */
    UpdateFileChunkRsp updateFileChunk(String projectId, String knowledgeRepoId, String fileId, String chunkId,
        String workspaceId, FileChunkReq body);

    /**
     * updateFileMetaInfo
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param workspaceId workspaceId
     * @param body body
     */
    UpdateFileMetaInfoRsp updateFileMetaInfo(String projectId, String knowledgeRepoId, String fileId,
        String workspaceId, UpdateFileMetaInfoReq body);

    /**
     * uploadKnowledgeFile
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param file file
     * @param tags tags
     */
    UploadKnowledgeFileResponseBody uploadKnowledgeFile(String workspaceId, String projectId, String knowledgeRepoId,
        MultipartFile file, List<String> tags);
}