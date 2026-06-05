/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */


package com.openjiuwen.studio.agent.agentbase.client;

import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowApiResponse;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowCreateChunkRequest;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowCreateKnowledgeBaseRequest;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowCreateKnowledgeBaseResp;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowCreateTaskRequest;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowDeleteChunkRequest;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowDeleteDataSetRequest;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowListChunkResp;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowListFilesRequest;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowListFilesResp;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowRetrieveKnowledgeBaseResp;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowSearchTextRequest;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowUpdateKnowledgeBaseRequest;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.RagFlowUploadFileResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.ragflow.response.RetrieveChunksResponseBody;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

/**
 * AgnetBaseRag客户端，
 *
 * @since 2025-06-13
 */
@FeignClient(name = "agentBaseRag", url = "http://localhost:8080")
public interface AgentBaseRagClient {

    @PostMapping("/v2/{project_id}/agent-base-rag/knowledge-bases")
    RagFlowApiResponse<RagFlowCreateKnowledgeBaseResp> createKnowledgeRepo(URI baseUrl,
        @RequestHeader HttpHeaders headers, @PathVariable(value = "project_id") String projectId,
        @RequestParam("workspace_id") String workspaceId, @RequestBody RagFlowCreateKnowledgeBaseRequest request);

    @PutMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}")
    RagFlowApiResponse<String> modifyKnowledgeRepo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @RequestBody RagFlowUpdateKnowledgeBaseRequest request, @PathVariable(value = "project_id") String projectId,
        @RequestParam("workspace_id") String workspaceId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId);

    @DeleteMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}")
    RagFlowApiResponse<Boolean> deleteKnowledgeRepo(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId, @RequestParam("workspace_id") String workspaceId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId);

    @GetMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}")
    RagFlowApiResponse<RagFlowRetrieveKnowledgeBaseResp> retrieveKnowledgeRepo(URI baseUrl,
        @RequestHeader HttpHeaders headers, @PathVariable(value = "project_id") String projectId,
        @RequestParam("workspace_id") String workspaceId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId);

    @PostMapping(path = "/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets",
        consumes = "multipart/form-data")
    RagFlowApiResponse<RagFlowUploadFileResp> uploadFile(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId, @RequestPart(value = "files")
        MultipartFile file);

    @DeleteMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets")
    void deleteFile(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId,
        @RequestBody RagFlowDeleteDataSetRequest request);

    @GetMapping(value = "/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets/{dataset_id}",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<byte[]> downloadFileContent(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId,
        @PathVariable(value = "dataset_id") String datasetId);

    @GetMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets")
    RagFlowApiResponse<RagFlowListFilesResp> listFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId,
        @RequestBody RagFlowListFilesRequest request);

    @PostMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets/{dataset_id}/chunks")
    void creatChunk(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId,
        @PathVariable(value = "dataset_id") String fileId, @RequestBody RagFlowCreateChunkRequest request);

    @PutMapping(
        "/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets/{dataset_id}/chunks/{chunk_id}")
    void updateChunk(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId,
        @PathVariable(value = "dataset_id") String fileId, @PathVariable(value = "chunk_id") String chunkId,
        @RequestBody RagFlowCreateChunkRequest request);

    @DeleteMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets/{dataset_id}/chunks")
    void deleteChunk(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId,
        @PathVariable(value = "dataset_id") String fileId, @RequestBody RagFlowDeleteChunkRequest request);

    @GetMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets/{dataset_id}/chunks")
    RagFlowApiResponse<RagFlowListChunkResp> listFileChunks(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId,
        @PathVariable(value = "dataset_id") String fileId, @RequestParam("size") int size,
        @RequestParam("page") int page);

    @PostMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/retrieval")
    RagFlowApiResponse<RetrieveChunksResponseBody> searchText(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId, @RequestBody RagFlowSearchTextRequest request);

    @PostMapping("/v2/{project_id}/agent-base-rag/knowledge-bases/{knowledge_base_id}/datasets/parse")
    RagFlowApiResponse<Boolean> createTask(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "knowledge_base_id") String knowledgeBaseId,
        @RequestBody RagFlowCreateTaskRequest request);
}
