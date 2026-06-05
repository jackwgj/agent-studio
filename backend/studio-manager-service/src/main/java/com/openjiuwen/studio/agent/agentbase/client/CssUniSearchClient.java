package com.openjiuwen.studio.agent.agentbase.client;/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.AddMemoryRequestBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.AddMemoryResponseBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.BatchDeleteMemoryRequestBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.CommonMessageResponseBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.CreateIndexRequestBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.ListMemoryRequestBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.ListMemoryResponseBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.MemoryInfo;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.SearchMemoryRequestBody;
import com.openjiuwen.studio.agent.agentbase.model.BatchCreateResponse;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeLabelsRsp;
import com.openjiuwen.studio.agent.common.dto.knowledge.CreateKnowledgeRepoReq;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeTaskRequestBody;
import com.openjiuwen.studio.agent.agentbase.model.CreateModelReq;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeLabelsReq;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeLabelsRsp;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeRepoReq;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqDeleteBatchResp;
import com.openjiuwen.studio.agent.agentbase.model.FaqListReq;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqReq;
import com.openjiuwen.studio.agent.agentbase.model.FaqResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeRepoInfo;
import com.openjiuwen.studio.agent.agentbase.model.ListFaqResponse;
import com.openjiuwen.studio.agent.agentbase.model.ListFileDocsRsp;
import com.openjiuwen.studio.agent.agentbase.model.ListFilesRsp;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeLabelsResp;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.ListSegmentRuleResp;
import com.openjiuwen.studio.agent.agentbase.model.ModifyKnowledgeRepoRequestBody;
import com.openjiuwen.studio.agent.agentbase.model.ModifyModelReq;
import com.openjiuwen.studio.agent.common.dto.knowledge.SearchTextReq;
import com.openjiuwen.studio.agent.agentbase.model.SearchTextResp;
import com.openjiuwen.studio.agent.agentbase.model.SegmentRule;
import com.openjiuwen.studio.agent.agentbase.model.UploadFilesResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.ListModelResp;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqResp;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesRequestBody;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeTaskReq;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UploadFaqFileRsp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/**
 * KooSearch知识库相关API client
 *
 * @since 2024-04-19
 */
@FeignClient(name = "CssUniSearch", url = "http://localhost:8080")
public interface CssUniSearchClient {
    /**
     * 创建知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param createKnowledgeRepoReq CreateKnowledgeRepoReq
     * @return String knowledgeRepoId
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/knowledge-repo")
    CreateKnowledgeRepoResp createKnowledgeRepo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @RequestBody CreateKnowledgeRepoReq createKnowledgeRepoReq);

    /**
     * 修改知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param modifyKnowledgeRepoRequestBody ModifyKnowledgeRepoRequestBody
     * @return String knowledgeRepoId
     */
    @PutMapping("/v1/{project_id}/applications/{application_id}/uni-search/knowledge-repo/{repo_id}")
    ModifyKnowledgeRepoResponseBody modifyKnowledgeRepo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestBody ModifyKnowledgeRepoRequestBody modifyKnowledgeRepoRequestBody);

    /**
     * 删除知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param deleteKnowledgeRepoReq DeleteKnowledgeRepoReq
     * @return DeleteKnowledgeResp
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/knowledge-repo")
    DeleteKnowledgeResp deleteKnowledgeRepo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @RequestBody DeleteKnowledgeRepoReq deleteKnowledgeRepoReq);

    /**
     * 查看知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @return KnowledgeRepoInfo
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/knowledge-repo/{repo_id}")
    KnowledgeRepoInfo showKnowledgeRepo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId);

    /**
     * 分页查看知识库列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param pageNum 页数
     * @param pageSize 每页数量
     * @param name 知识库名称
     * @return ListKnowledgeRepoResp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/knowledge-repo")
    ListKnowledgeRepoResp listKnowledgeRepos(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam(value = "page_num") Integer pageNum,
        @RequestParam(value = "page_size") Integer pageSize, @RequestParam(value = "name") String name,
        @RequestParam(value = "order_by") String orderBy, @RequestParam(value = "repo_type") String repoType,
        @RequestParam(value = "embedding_model") String embeddingModel,
        @RequestParam(value = "rerank_model") String rerankModel);

    /**
     * 上传一个文档到指定知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param file 文档
     * @return UploadFilesResp 文档id
     */
    @PostMapping(value = "/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/files",
        consumes = "multipart/form-data")
    UploadFilesResp uploadFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestPart(value = "file") MultipartFile file, @RequestParam(value = "tags", required = false) List<String> tags);

    /**
     * 删除指定文档
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param fileId 文档id
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/files/{file_id}")
    void deleteFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId);

    /**
     * 批量删除文档
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param deleteFileReq 批量删除文档请求
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/files/batch")
    BatchDeleteKnowledgeFilesResponseBody batchDeleteFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestBody BatchDeleteKnowledgeFilesRequestBody deleteFileReq);

    /**
     * 分页查询文档列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param pageNum 页数
     * @param pageSize 每页数量
     * @param fileName 文档名
     * @param fileType 文档类型
     * @param fileStatus 文档状态
     * @return ListFilesRsp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/files/search")
    ListFilesRsp searchFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestParam(value = "page_num") Integer pageNum, @RequestParam(value = "page_size") Integer pageSize,
        @RequestParam(value = "file_name") String fileName, @RequestParam(value = "file_type") String fileType,
        @RequestParam(value = "file_status") String fileStatus);

    /**
     * 下载指定文档
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @return MultipartFile
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/files/{file_id}")
    ResponseEntity<byte[]> downloadFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId);

    /**
     * 新增文件分片
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param fileChunkReq 分片详情
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/files/{file_id}/docs")
    Void createFileDocs(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId, @RequestBody FileChunkReq fileChunkReq);

    /**
     * 修改文件分片
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param docId 分片id
     * @param fileChunkReq 分片详情
     */
    @PutMapping("/v1/{project_id}/applications/{application_id}/uni-search/files/{file_id}/docs/{doc_id}")
    Void updateFileDocs(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId, @PathVariable(value = "doc_id") String docId,
        @RequestBody FileChunkReq fileChunkReq);

    /**
     * 修改元信息
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param tags 标签列表
     */
    @PutMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/files/{file_id}")
    Void updateFileInfo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId,
        @RequestParam(value = "tags", required = false) List<String> tags);

    /**
     * 删除文件分片
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param docIds 分片id列表
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/files/{file_id}/docs")
    Void batchDeleteFileDocs(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId, @RequestBody List<String> docIds);

    /**
     * 分页查看文件解析后的分片列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param pageNum 页数
     * @param pageSize 每页数量
     * @return ListFileDocsRsp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/files/{file_id}/docs")
    ListFileDocsRsp listFileDocs(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId, @RequestParam(value = "page_num") Integer pageNum,
        @RequestParam(value = "page_size") Integer pageSize);

    /**
     * 检索知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param searchTextReq 检索请求
     * @return SearchTextResp 检索结果
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/experience/searchtext")
    SearchTextResp searchText(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestBody SearchTextReq searchTextReq);

    /**
     * 创建FAQ
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param faqReq 创建FAQ请求体
     * @return FaqResp
     */
    @PostMapping(value = "/v1/{project_id}/applications/{application_id}/uni-search/faq")
    FaqResp createFaq(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestBody FaqReq faqReq);

    /**
     * 更新FAQ
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param faqReq 修改FAQ请求体
     */
    @PutMapping(value = "/v1/{project_id}/applications/{application_id}/uni-search/faq")
    void updateFaq(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestBody FaqReq faqReq);

    /**
     * 删除FAQ
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param faqId FAQ id
     */
    @DeleteMapping(value = "/v1/{project_id}/applications/{application_id}/uni-search/faq/{repo_id}/{faq_id}")
    void deleteFaq(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @PathVariable(value = "faq_id") String faqId);

    /**
     * 批量删除faq
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param faqListReq faq列表
     * @return FaqDeleteBatchResp
     */
    @DeleteMapping(value = "/v1/{project_id}/applications/{application_id}/uni-search/faq/batch")
    FaqDeleteBatchResp deleteFaqBatch(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestBody FaqListReq faqListReq);

    /**
     * 查询FAQ详情
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param faqId faq id
     * @return FaqInfo
     */
    @GetMapping(value = "/v1/{project_id}/applications/{application_id}/uni-search/faq/{faq_id}")
    FaqInfo showFaq(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "faq_id") String faqId);

    /**
     * 查询知识库下FAQ列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param pageNum 请求页码
     * @param pageSize 限定响应体每页返回的数据条数
     * @param question 问题关键字
     * @return ListFaqResponse
     */
    @GetMapping(value = "/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/faq")
    ListFaqResponse listFaq(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestParam(value = "page_num") Integer pageNum, @RequestParam(value = "page_size") Integer pageSize,
        @RequestParam(value = "question") String question);

    /**
     * 启用知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     */
    @PutMapping("/v1/{project_id}/applications/{application_id}/uni-search/knowledge-repo/{repo_id}/start")
    void startKnowledgeRepo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId);

    /**
     * 停用知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     */
    @PutMapping("/v1/{project_id}/applications/{application_id}/uni-search/knowledge-repo/{repo_id}/stop")
    void stopKnowledgeRepo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId);

    /**
     * 创建知识文档分层规则
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param segmentRule SegmentRuleReq
     * @return SegmentRule
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/rule-regex")
    SegmentRule createSegmentRule(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestBody SegmentRule segmentRule);

    /**
     * 修改知识文档分层规则
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param id 规则id
     * @param segmentRule SegmentRule
     * @return SegmentRule
     */
    @PutMapping("/v1/{project_id}/applications/{application_id}/uni-search/rule-regex/{id}")
    SegmentRule modifySegmentRule(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "id") String id,
        @RequestBody SegmentRule segmentRule);

    /**
     * 查询知识文档分层规则列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @return ListSegmentRuleResp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/rule-regex")
    ListSegmentRuleResp listSegmentRule(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId);

    /**
     * 删除知识文档分层规则
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param id 规则id
     * @return SegmentRule
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/rule-regex/{id}")
    SegmentRule deleteSegmentRule(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "id") String id);

    /**
     * 注册模型
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param createModelReq 模型注册请求
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/models")
    void addModel(URI baseUrl, @RequestHeader HttpHeaders headers, @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestBody CreateModelReq createModelReq);

    /**
     * 修改模型
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param modifyModelReq 模型修改请求
     */
    @PutMapping("/v1/{project_id}/applications/{application_id}/uni-search/models/{model_id}")
    void modifyModel(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "model_id") String modelId,
        @RequestBody ModifyModelReq modifyModelReq);

    /**
     * 删除模型
     *
     * @param projectId projectId
     * @param applicationId applicationId
     * @param modelName modelName
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/models/{model_name}")
    void deleteModel(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @PathVariable(value = "model_name") String modelName);

    /**
     * 查询模型列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param modelName 模型名称
     * @param modelType 模型类型，枚举值：embedding,rerank,nlp,search-plan,query2query
     * @param modelStatus 模型状态
     * @param pageNum 请求页码
     * @param pageSize 请求限定响应结果的分页大小，例如5条/页，10条/页
     * @return ListModelResp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/models/search")
    ListModelResp listModels(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @RequestParam(value = "model_name") String modelName, @RequestParam(value = "model_type") String modelType,
        @RequestParam(value = "model_status") String modelStatus, @RequestParam(value = "page_num") Integer pageNum,
        @RequestParam(value = "page_size") Integer pageSize);

    /**
     * 批量创建任务
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param createTaskReq 创建任务请求
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/tasks")
    BatchCreateResponse createTask(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestBody CreateKnowledgeTaskRequestBody createTaskReq);

    /**
     * 查询任务列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param pageNum 请求页码
     * @param pageSize 每页数量
     * @param taskType 任务类型
     * @param taskStatus 任务状态
     * @param fileName 文件名称
     * @return listKnowledgeTaskResp 查询任务响应体
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/tasks")
    ListKnowledgeTasksResponseBody listKnowledgeTask(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestParam(value = "page_num") Integer pageNum, @RequestParam(value = "page_size") Integer pageSize,
        @RequestParam(value = "task_type") String taskType, @RequestParam(value = "task_status") String taskStatus,
        @RequestParam(value = "file_name") String fileName);

    /**
     * 批量删除任务
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param deleteKnowledgeTaskReq 批量删除任务请求体
     * @return deleteKnowledgeTaskResp 批量删除响应体
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/tasks/batch")
    BatchDeleteFaqResp deleteKnowledgeTask(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestBody DeleteKnowledgeTaskReq deleteKnowledgeTaskReq);

    /**
     * 上传一个文档到指定知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param file 文档
     * @return UploadFilesResp 文档id
     */
    @PostMapping(value = "/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/faq/batch/upload",
        consumes = "multipart/form-data")
    UploadFaqFileRsp uploadFaqFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestPart(value = "file") MultipartFile file);

    /**
     * 下载指定FAQ文档
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @return MultipartFile
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/faq/batch/{file_id}")
    ResponseEntity<byte[]> downloadFaqFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId);

    /**
     * 删除指定FAQ文档
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param fileId 文档id
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/faq/batch/{file_id}")
    void deleteFaqFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId);

    /**
     * 分页查询FAQ文档列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param pageNum 页数
     * @param pageSize 每页数量
     * @param fileName 文档名
     * @param fileStatus 文档状态
     * @param ids 按id列表精确查询
     * @return ListFilesRsp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/faq/batch/search")
    ListFilesRsp searchFaqFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestParam(value = "page_num") Integer pageNum, @RequestParam(value = "page_size") Integer pageSize,
        @RequestParam(value = "file_name") String fileName, @RequestParam(value = "file_status") String fileStatus,
        @RequestParam(value = "ids") List<String> ids);

    /**
     * 新增FAQ文件分片
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param faqFileChunkReq FAQ分片详情
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/faq/batch/{file_id}/docs")
    Void createFaqFileDocs(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId, @RequestBody FaqFileChunkReq faqFileChunkReq);

    /**
     * 删除FAQ文件分片
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param fileDocIds 分片id列表
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/faq/batch/{file_id}/docs")
    Void batchDeleteFaqFileDocs(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId, @RequestBody List<String> fileDocIds);

    /**
     * 修改FAQ文件分片
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param docId 分片id
     * @param faqFileChunkReq FAQ分片详情
     */
    @PutMapping("/v1/{project_id}/applications/{application_id}/uni-search/faq/batch/{file_id}/docs/{doc_id}")
    Void updateFaqFileDocs(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId, @PathVariable(value = "doc_id") String docId,
        @RequestBody FaqFileChunkReq faqFileChunkReq);

    /**
     * 分页查看FAQ文件解析后的分片列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @param pageNum 页数
     * @param pageSize 每页数量
     * @return ListFileDocsRsp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/faq/batch/{file_id}/docs")
    ListFileDocsRsp listFaqFileDocs(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId, @RequestParam(value = "page_num") Integer pageNum,
        @RequestParam(value = "page_size") Integer pageSize);

    /**
     * 分页查看知识库标签列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param pageNum 页数
     * @param pageSize 每页数量
     * @return ListKnowledgeLabelsResp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/labels")
    ListKnowledgeLabelsResp listLabels(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestParam(value = "page_num") Integer pageNum, @RequestParam(value = "page_size") Integer pageSize);

    /**
     * 创建知识库标签
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param createKnowledgeTagsReq 创建标签请求体
     * @return CreateKnowledgeTagsRsp
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/labels")
    CreateKnowledgeLabelsRsp createLabel(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestBody CreateKnowledgeRepoTagsReq createKnowledgeTagsReq);

    /**
     * 删除知识库标签
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param labelId 标签id
     * @param deleteKnowledgeTagsReq 删除标签请求体
     * @return DeleteKnowledgeTagsRsp
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/labels/{label_id}")
    DeleteKnowledgeLabelsRsp deleteLabel(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @PathVariable(value = "label_id") String labelId, @RequestBody DeleteKnowledgeLabelsReq deleteKnowledgeTagsReq);

    /**
     * 查询分块中的图片
     *
     * @param projectId
     * @param applicationId
     * @param imageId
     * @return InputStream
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/img/{image_id}")
    Resource queryImage(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @RequestParam("file_id") String fileId, @PathVariable(value = "image_id") String imageId);

    /**
     * 创建记忆索引
     *
     * @param projectId
     * @param applicationId
     * @param requestBody
     * @return
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/memories/index")
    CommonMessageResponseBody createMemoryIndex(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestBody CreateIndexRequestBody requestBody);

    /**
     * 添加记忆
     *
     * @param projectId
     * @param applicationId
     * @param indexName
     * @param requestBody
     * @return
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/memories/index/{index_name}")
    AddMemoryResponseBody addMemory(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @PathVariable(value = "index_name") String indexName, @RequestBody AddMemoryRequestBody requestBody);

    /**
     * 查询记忆列表
     *
     * @param projectId
     * @param applicationId
     * @param indexName
     * @param pageNum
     * @param pageSize
     * @param requestBody
     * @return
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/memories/index/{index_name}")
    ListMemoryResponseBody listMemory(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @PathVariable(value = "index_name") String indexName, @RequestParam(value = "page_num") Integer pageNum,
        @RequestParam(value = "page_size") Integer pageSize, @RequestBody ListMemoryRequestBody requestBody);

    /**
     * 检索记忆
     *
     * @param projectId
     * @param applicationId
     * @param indexName
     * @param requestBody
     * @return
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/memories/index/{index_name}/search")
    List<MemoryInfo> searchMemory(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @PathVariable(value = "index_name") String indexName, @RequestBody SearchMemoryRequestBody requestBody);

    /**
     * 获取记忆详情
     *
     * @param projectId
     * @param applicationId
     * @param indexName
     * @param memoryId
     * @return
     */
    @GetMapping(
        "/v1/{project_id}/applications/{application_id}/uni-search/memories/index/{index_name}/memory/{memory_id}")
    MemoryInfo getMemory(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @PathVariable(value = "index_name") String indexName, @PathVariable(value = "memory_id") String memoryId);

    /**
     * 删除单条记忆
     *
     * @param projectId
     * @param applicationId
     * @param indexName
     * @param memoryId
     * @return
     */
    @DeleteMapping(
        "/v1/{project_id}/applications/{application_id}/uni-search/memories/index/{index_name}/memory/{memory_id}")
    CommonMessageResponseBody deleteMemory(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @PathVariable(value = "index_name") String indexName, @PathVariable(value = "memory_id") String memoryId);

    /**
     * 批量删除记忆
     *
     * @param projectId
     * @param applicationId
     * @param indexName
     * @param requestBody
     * @return
     */
    @DeleteMapping("/v1/{project_id}/applications/{application_id}/uni-search/memories/index/{index_name}/batch")
    CommonMessageResponseBody batchDeleteMemory(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId,
        @PathVariable(value = "index_name") String indexName, @RequestBody BatchDeleteMemoryRequestBody requestBody);
}
