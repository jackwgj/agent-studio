/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

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

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * KnowledgeFaqFileManagement service
 */

public interface IKnowledgeFaqFileManagementService {

    /**
     * createFaqFileChunk
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateFileChunkRsp createFaqFileChunk(String projectId, String knowledgeRepoId, String fileId, String workspaceId,
        FaqFileChunkReq body);

    /**
     * deleteFaqFile
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteFaqFile(String projectId, String knowledgeRepoId, String fileId, String workspaceId);

    /**
     * deleteFaqFileChunk
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param chunkId chunkId
     * @param workspaceId workspaceId
     */
    CommonDeleteRsp deleteFaqFileChunk(String projectId, String knowledgeRepoId, String fileId, String chunkId,
        String workspaceId);

    /**
     * downloadFaqFile
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param downloadFaqFileQo downloadFaqFileQo
     */
    Resource downloadFaqFile(String projectId, String knowledgeRepoId, String fileId,
        DownloadFaqFileQo downloadFaqFileQo);

    /**
     * downloadFaqFileByAccessKey
     *
     * @param projectId projectId
     * @param fileId fileId
     * @param knowledgeBaseId knowledgeBaseId
     * @param downloadFaqFileByAccessKeyQo downloadFaqFileByAccessKeyQo
     */
    Resource downloadFaqFileByAccessKey(String projectId, String fileId, String knowledgeBaseId,
        DownloadFaqFileByAccessKeyQo downloadFaqFileByAccessKeyQo);

    /**
     * listFaqFileChunks
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param listFaqFileChunksQo listFaqFileChunksQo
     */
    FaqFileChunkListRsp listFaqFileChunks(String projectId, String knowledgeRepoId, String fileId,
        ListFaqFileChunksQo listFaqFileChunksQo);

    /**
     * listFaqFiles
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param listFaqFilesQo listFaqFilesQo
     */
    FaqFileInfoListRsp listFaqFiles(String projectId, String knowledgeRepoId, ListFaqFilesQo listFaqFilesQo);

    /**
     * updateFaqFileChunk
     *
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param fileId fileId
     * @param chunkId chunkId
     * @param workspaceId workspaceId
     * @param body body
     */
    UpdateFileChunkRsp updateFaqFileChunk(String projectId, String knowledgeRepoId, String fileId, String chunkId,
        String workspaceId, FaqFileChunkReq body);

    /**
     * uploadFaqFile
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param knowledgeRepoId knowledgeRepoId
     * @param file file
     */
    UploadFaqFileRsp uploadFaqFile(String workspaceId, String projectId, String knowledgeRepoId, MultipartFile file);
}