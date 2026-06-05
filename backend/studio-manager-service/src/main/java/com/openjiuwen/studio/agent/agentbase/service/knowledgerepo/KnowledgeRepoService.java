/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqSearchCriteria;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeRepo;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeSegRuleInfo;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.ListTaskCriteria;
import com.openjiuwen.studio.agent.agentbase.model.ModelSearchCriteria;
import com.openjiuwen.studio.agent.agentbase.model.SearchTextResp;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesRequestBody;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CommonBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FaqFileInfoListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkReq;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeFaq;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeSegmentRule;
import com.openjiuwen.studio.agent.manager.dto.ListFaqFileChunksReq;
import com.openjiuwen.studio.agent.manager.dto.ListFaqFileReq;
import com.openjiuwen.studio.agent.manager.dto.ListFaqResp;
import com.openjiuwen.studio.agent.manager.dto.ListFileChunksReq;
import com.openjiuwen.studio.agent.manager.dto.ListFileReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeRepoReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTagsResp;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ModelInfo;
import com.openjiuwen.studio.agent.manager.dto.UpdateFileMetaInfoReq;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库Service
 *
 * @since 2025-01-02
 */
public interface KnowledgeRepoService {

    /**
     * 内部分页大小
     */
    int INTERNAL_TAG_PAGE_SIZE = 100;

    /**
     * 内部页码
     */
    int INTERNAL_TAG_PAGE_NUM = 1;

    /**
     * 最大标签数量
     */
    int MAX_TAG_NUM = 100;

    /**
     * 创建知识库
     *
     * @param knowledgeRepo KnowledgeRepo
     * @return knowledgeRepo id
     */
    CreateKnowledgeRepoInfo createKnowledgeRepo(KnowledgeRepo knowledgeRepo);

    /**
     * 修改知识库
     *
     * @param knowledgeRepo KnowledgeRepo
     * @return knowledgeRepo id
     */
    String modifyKnowledgeRepo(KnowledgeRepo knowledgeRepo);

    /**
     * 删除知识库
     *
     * @param knowledgeRepo KnowledgeRepo
     */
    void deleteKnowledgeRepo(KnowledgeRepo knowledgeRepo);

    /**
     * 查询知识库详情
     *
     * @param knowledgeRepo KnowledgeRepo
     * @return KnowledgeRepo
     */
    KnowledgeRepo retrieveKnowledgeRepo(KnowledgeRepo knowledgeRepo);

    /**
     * 查询知识库列表
     *
     * @param listKnowledgeRepoReq 知识库列表查询请求
     * @return 知识库列表
     */
    ListKnowledgeRepoResp listKnowledgeRepos(ListKnowledgeRepoReq listKnowledgeRepoReq);

    /**
     * 上传文件到知识库
     *
     * @param knowledgeRepo knowledgeRepo
     * @return file id
     */
    String uploadFile(KnowledgeRepo knowledgeRepo, MultipartFile file, List<String> tags);

    /**
     * 删除知识库中的文件
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId 文件id
     */
    void deleteFile(KnowledgeRepo knowledgeRepo, String fileId);

    /**
     * 批量删除知识库中的文件
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param deleteFileReq 批量删除文件请求
     */
    BatchDeleteKnowledgeFilesResponseBody batchDeleteFile(KnowledgeRepo knowledgeRepo,
        BatchDeleteKnowledgeFilesRequestBody deleteFileReq);

    /**
     * 查询知识库下文件列表
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param listFileReq 文件列表请求体
     * @return 文件信息列表
     */
    ListKnowledgeFilesResponseBody listFiles(KnowledgeRepo knowledgeRepo, ListFileReq listFileReq);

    /**
     * 从知识库下载文件
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId 文件id
     * @return 文件内容
     */
    ResponseEntity<byte[]> downloadFile(KnowledgeRepo knowledgeRepo, String fileId);

    /**
     * 新增文件分片
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId fileId
     * @param fileChunkReq 新增文件切片请求体
     */
    void createFileChunk(KnowledgeRepo knowledgeRepo, String fileId, FileChunkReq fileChunkReq);

    /**
     * 修改文件分片
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId fileId
     * @param chunkId chunkId
     * @param fileChunkReq 修改文件分片请求体
     */
    void updateFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId, FileChunkReq fileChunkReq);

    /**
     * 修改文件元信息
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId fileId
     * @param updateFileMetaInfoReq 修改元信息请求体
     */
    void updateFileMetaInfo(KnowledgeRepo knowledgeRepo, String fileId, UpdateFileMetaInfoReq updateFileMetaInfoReq);

    /**
     * 删除文件分片
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId fileId
     * @param chunkId chunkId
     */
    void deleteFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId);

    /**
     * 查询文件分片详情
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param listFileChunksReq 文件切片请求体
     * @return 文件切片列表
     */
    FileChunkListRsp listFileChunks(KnowledgeRepo knowledgeRepo, ListFileChunksReq listFileChunksReq);

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
        int pageNum, int pageSize);

    /**
     * 创建FAQ
     *
     * @param knowledgeFaq KnowledgeFaq
     * @return faq id
     */
    String createFaq(KnowledgeFaq knowledgeFaq);

    /**
     * 删除单条FAQ
     *
     * @param repoId 知识库id
     * @param faqId faq id
     */
    void deleteFaq(String repoId, String faqId);

    /**
     * 批量删除FAQ
     *
     * @param repoId 知识库ID
     * @param faqIds faq id 列表
     * @return 成功删除的记录数
     */
    Integer deleteFaqBatch(String repoId, List<String> faqIds);

    /**
     * 修改FAQ
     *
     * @param knowledgeFaq KnowledgeFaq
     * @return faq id
     */
    String modifyFaq(KnowledgeFaq knowledgeFaq);

    /**
     * 查询FAQ详情
     *
     * @param faqId faq id
     * @return KnowledgeFaq
     */
    KnowledgeFaq retrieveFaq(String faqId, String repoId);

    /**
     * 查询FAQ列表
     *
     * @param faqSearchCriteria FaqSearchCriteria
     * @return ListFaqResp
     */
    ListFaqResp listFaq(FaqSearchCriteria faqSearchCriteria);

    /**
     * 启用知识库
     *
     * @param knowledgeRepo 知识库
     */
    void startKnowledgeRepo(KnowledgeRepo knowledgeRepo);

    /**
     * 停用知识库
     *
     * @param knowledgeRepo 知识库
     */
    void stopKnowledgeRepo(KnowledgeRepo knowledgeRepo);

    /**
     * 创建知识分层规则
     *
     * @param segmentRule KnowledgeSegmentRule
     * @param repoId KooSearch/LakeSearch的真实ID
     * @return 规则id
     */
    KnowledgeSegRuleInfo createSegmentRule(KnowledgeSegmentRule segmentRule, String repoId);

    /**
     * 修改知识分层规则
     *
     * @param segmentRule KnowledgeSegmentRule
     * @return 规则id
     */
    String modifySegmentRule(KnowledgeSegmentRule segmentRule);

    /**
     * 删除知识分层规则
     *
     * @param ruleId 规则id
     */
    void deleteSegmentRule(String ruleId);

    /**
     * 查询已注册的模型列表
     *
     * @param searchCriteria 模型列表查询条件
     * @return 模型列表
     */
    List<ModelInfo> listModels(ModelSearchCriteria searchCriteria, String connectionId);

    /**
     * 创建任务
     *
     * @param repoId 知识库id
     * @param taskType 任务类型
     * @param fileIds 文件id列表
     * @return CreateTaskResp
     */
    CreateKnowledgeTaskResponseBody createTask(String repoId, String taskType, List<String> fileIds);

    /**
     * 查询任务列表
     *
     * @param repoId 知识库id
     * @param listTaskCriteria 任务列表查询条件
     * @return listKnowledgeTaskResp 查询任务响应体
     */
    ListKnowledgeTasksResponseBody listKnowledgeTask(String repoId, ListTaskCriteria listTaskCriteria);

    /**
     * 批量删除任务
     *
     * @param repoId 知识库id
     * @param taskIds 任务id列表
     * @return CommonBatchDeleteRsp
     */
    CommonBatchDeleteRsp deleteKnowledgeTask(String repoId, List<String> taskIds);

    /**
     * 上传FAQ文件到知识库
     *
     * @param knowledgeRepo knowledgeRepo
     * @return file id
     */
    String uploadFaqFile(KnowledgeRepo knowledgeRepo, MultipartFile file);

    /**
     * 删除知识库中的FAQ文件
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId 文件id
     */
    void deleteFaqFile(KnowledgeRepo knowledgeRepo, String fileId);

    /**
     * 查询知识库下FAQ文件列表
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param listFaqFileReq FAQ文件列表请求体
     * @return FAQ文件信息列表
     */
    FaqFileInfoListRsp listFaqFiles(KnowledgeRepo knowledgeRepo, ListFaqFileReq listFaqFileReq);

    /**
     * 从知识库下载FAQ文件
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId 文件id
     * @return FAQ文件内容
     */
    ResponseEntity<byte[]> downloadFaqFile(KnowledgeRepo knowledgeRepo, String fileId);

    /**
     * 新增FAQ文件分片
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId fileId
     * @param faqFileChunkReq 新增FAQ文件切片请求体
     */
    void createFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, FaqFileChunkReq faqFileChunkReq);

    /**
     * 修改FAQ文件分片
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId fileId
     * @param chunkId chunkId
     * @param faqFileChunkReq 修改FAQ文件分片请求体
     */
    void updateFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId,
        FaqFileChunkReq faqFileChunkReq);

    /**
     * 删除FAQ文件分片
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param fileId fileId
     * @param chunkId chunkId
     */
    void deleteFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId);

    /**
     * 查询FAQ文件分片详情
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param listFaqFileChunksReq 文件FAQ切片请求体
     * @return FAQ文件切片列表
     */
    FaqFileChunkListRsp listFaqFileChunks(KnowledgeRepo knowledgeRepo, ListFaqFileChunksReq listFaqFileChunksReq);

    /**
     * 查询知识库标签列表
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param pageNum 返回结果所在页码
     * @param pageSize 每页返回结果数量
     * @return 标签列表
     */
    ListKnowledgeTagsResp listKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, int pageNum, int pageSize);

    /**
     * 创建知识库文档标签
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param body CreateKnowledgeTagsReq
     * @return 标签Id
     */
    CreateKnowledgeRepoTagsRsp createKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, CreateKnowledgeRepoTagsReq body);

    /**
     * 删除知识库文档标签
     *
     * @param knowledgeRepo KnowledgeRepo
     * @param body CreateKnowledgeTagsReq
     * @return 标签Id
     */
    DeleteKnowledgeRepoTagsRsp deleteKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, String tagId,
        DeleteKnowledgeRepoTagsReq body);

    /**
     * 查询分块中返回的图片
     *
     * @param knowledgeRepo
     * @param imageId
     * @return
     */
    Resource queryImage(KnowledgeRepo knowledgeRepo, String imageId);

}
