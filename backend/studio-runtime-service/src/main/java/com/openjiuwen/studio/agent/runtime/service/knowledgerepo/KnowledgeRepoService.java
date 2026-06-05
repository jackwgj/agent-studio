/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo;

import com.openjiuwen.studio.agent.runtime.dto.knowledge.FileInfoResponse;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.KnowledgeRepo;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFaqFileReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFileReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListStudioKnowledgeFaqFilesResponseBody;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.enums.ConnectorTypeEnum;
import com.openjiuwen.studio.agent.runtime.rce.model.SearchTextResp;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 知识库Service
 *
 * @since 2025-01-02
 */
public interface KnowledgeRepoService {

    String KOS_ES_ERROR_CODE = "KOS.00020001";

    ConnectorTypeEnum type();

    /**
     * 知识库检索
     *
     * @param knowledgeRepos KnowledgeRepo
     * @param query 知识检索内容
     * @param searchMode 检索模式
     * @param tags 检索的标签范围
     * @param pageNum 返回结果所在页码
     * @param pageSize 每页返回结果数量
     * @return SearchTextResp
     */
    SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode, List<String> tags,
        Float recallThreshold, Integer pageNum, Integer pageSize);

    List<KnowledgeBaseConnectionParam> EncryptParams(List<KnowledgeBaseConnectionParam> params);

    /**
     * 从知识库下载文件
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId 文件id
     * @return 文件内容
     */
    ResponseEntity<byte[]> downloadFile(KnowledgeRepo knowledgeRepo, String fileId);

    /**
     * 查询分块中返回的图片
     *
     * @param knowledgeRepo
     * @param imageId
     * @return
     */
    Resource queryImage(KnowledgeRepo knowledgeRepo, String imageId);

    /**
     * 查询知识库下文件列表
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param listFileReq 文件列表请求体
     * @return 文件信息列表
     */
    FileInfoResponse listFiles(KnowledgeRepo knowledgeRepo, ListFileReq listFileReq);

    /**
     * 查询知识库下FAQ文件列表
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param listFaqFileReq FAQ文件列表请求体
     * @return FAQ文件信息列表
     */
    ListStudioKnowledgeFaqFilesResponseBody listFaqFiles(KnowledgeRepo knowledgeRepo, ListFaqFileReq listFaqFileReq);

    /**
     * 从知识库下载FAQ文件
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId 文件id
     * @return FAQ文件内容
     */
    ResponseEntity<byte[]> downloadFaqFile(KnowledgeRepo knowledgeRepo, String fileId);

}
