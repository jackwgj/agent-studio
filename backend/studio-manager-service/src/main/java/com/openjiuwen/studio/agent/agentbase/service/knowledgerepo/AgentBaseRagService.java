/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import com.openjiuwen.studio.agent.agentbase.client.AgentBaseRagClient;
import com.openjiuwen.studio.agent.agentbase.common.constant.CommonConstant;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqSearchCriteria;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeRepo;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeSegRuleInfo;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.ListTaskCriteria;
import com.openjiuwen.studio.agent.agentbase.model.ModelSearchCriteria;
import com.openjiuwen.studio.agent.agentbase.model.SearchTextResp;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.Document;
import com.openjiuwen.studio.agent.agentbase.model.ragflow.FileStatusEnum;
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
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.AgentBaseRagConnection;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider.AgentBaseRagConnectionProvider;
import com.openjiuwen.studio.agent.agentbase.utils.DelimiterUtils;
import com.openjiuwen.studio.agent.common.dto.knowledge.RetrieveChunkInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.ragflow.response.RetrieveChunksResponseBody;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesRequestBody;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ChatReferenceInfo;
import com.openjiuwen.studio.agent.manager.dto.CommonBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FaqFileInfoListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkInfo;
import com.openjiuwen.studio.agent.manager.dto.FileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FileInfo;
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
import com.openjiuwen.studio.agent.common.dto.knowledge.ParseConf;
import com.openjiuwen.studio.agent.manager.dto.RagChunkParserConf;
import com.openjiuwen.studio.agent.common.dto.knowledge.SplitConf;
import com.openjiuwen.studio.agent.manager.dto.UpdateFileMetaInfoReq;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 基于ragflow的自研RAGFlow服务
 * 待对接
 *
 * @since 2025-06-14
 */
@Slf4j
@Service("MgAgentBaseRag")
public class AgentBaseRagService implements KnowledgeRepoService {

    private final AgentBaseRagClient agentBaseRagClient;

    private final AgentBaseRagConnectionProvider agentBaseRagConnectionProvider;

    private final KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    @Autowired
    public AgentBaseRagService(AgentBaseRagClient agentBaseRagClient,
        AgentBaseRagConnectionProvider agentBaseRagConnectionProvider,
        KnowledgeConnectionRouterService knowledgeConnectionRouterService) {
        this.agentBaseRagClient = agentBaseRagClient;
        this.agentBaseRagConnectionProvider = agentBaseRagConnectionProvider;
        this.knowledgeConnectionRouterService = knowledgeConnectionRouterService;
    }

    private URI getUri(String connectionId) {
        AgentBaseRagConnection agentBaseRagConnection = getAgentBaseRagConnection(connectionId);
        String ragEndpoint = agentBaseRagConnection.getEndpoint();
        if (StringUtils.isBlank(ragEndpoint)) {
            log.error("agent base rag endpoint is empty");
            throw new AgentBaseException(ErrorCode.INVALID_KNOWLEDGE_REPO_REQUEST);

        }
        return URI.create(ragEndpoint);
    }

    private HttpHeaders getHeaders() {
        String authToken = RequestContextUtils.getRequestAuthToken();
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstant.X_AUTH_TOKEN, authToken);
        return headers;
    }

    private AgentBaseRagConnection getAgentBaseRagConnection(String connectionId) {
        return agentBaseRagConnectionProvider.getKnowledgeSourceConnection(connectionId);
    }

    @Override
    public CreateKnowledgeRepoInfo createKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        try {
            RagChunkParserConf ragChunkParserConf = knowledgeRepo.getRagChunkParserConf();
            RagFlowCreateKnowledgeBaseRequest ragFlowCreateKnowledgeBaseRequest
                = RagFlowCreateKnowledgeBaseRequest.builder()
                .name(knowledgeRepo.getDisplayName())
                .embeddingModel(knowledgeRepo.getEmbeddingModel())
                .reRankModel(knowledgeRepo.getRerankModel())
                .parserId(ragChunkParserConf.getChunkMethod().toString())
                .parserConfig(RagFlowCreateKnowledgeBaseRequest.ParserConfig.builder()
                    .chunkTokenNum(ragChunkParserConf.getChunkTokenNum())
                    .delimiter(DelimiterUtils.delimiterListToString(ragChunkParserConf.getDelimiter()))
                    .build())
                .build();

            String connectionId = knowledgeConnectionRouterService.findConnectionIdByContext();
            //  调用agent-base-rag创建知识库
            RagFlowApiResponse<RagFlowCreateKnowledgeBaseResp> ragFlowCreateKnowledgeBaseResp
                = agentBaseRagClient.createKnowledgeRepo(getUri(connectionId), getHeaders(),
                knowledgeRepo.getProjectId(), knowledgeRepo.getWorkspaceId(), ragFlowCreateKnowledgeBaseRequest);
            log.info("Success to delete KnowledgeRepo: {}", ragFlowCreateKnowledgeBaseResp);

            CreateKnowledgeRepoInfo createKnowledgeRepoInfo = new CreateKnowledgeRepoInfo();
            createKnowledgeRepoInfo.setKnowledgeBaseId(ragFlowCreateKnowledgeBaseResp.getData().getKbId());
            createKnowledgeRepoInfo.setKnowledgeBaseConnectionId(connectionId);
            return createKnowledgeRepoInfo;
        } catch (Exception exception) {
            log.error("Fail to delete knowledge repo, projectId: {}, knowledgeRepoId: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getKnowledgeRepoId());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public String modifyKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        RagChunkParserConf ragChunkParserConf = knowledgeRepo.getRagChunkParserConf();
        RagFlowUpdateKnowledgeBaseRequest ragFlowUpdateKnowledgeBaseRequest = new RagFlowUpdateKnowledgeBaseRequest();
        if (ObjectUtils.isNotEmpty(ragChunkParserConf)) {
            ragFlowUpdateKnowledgeBaseRequest = RagFlowUpdateKnowledgeBaseRequest.builder()
                .parserId(ragChunkParserConf.getChunkMethod().toString())
                .parserConfig(RagFlowUpdateKnowledgeBaseRequest.ParserConfig.builder()
                    .chunkTokenNum(ragChunkParserConf.getChunkTokenNum())
                    .delimiter(DelimiterUtils.delimiterListToString(ragChunkParserConf.getDelimiter()))
                    .build())
                .build();
        }
        Optional.ofNullable(knowledgeRepo.getRerankModel())
            .ifPresent(ragFlowUpdateKnowledgeBaseRequest::setReRankModel);
        Optional.ofNullable(knowledgeRepo.getDisplayName()).ifPresent(ragFlowUpdateKnowledgeBaseRequest::setName);

        String connectionId = agentBaseRagConnectionProvider.getConnectionIdByRepoId(
            knowledgeRepo.getKnowledgeRepoId());
        String projectId = RequestContextUtils.getRequestProjectId();
        String workspaceId = knowledgeRepo.getWorkspaceId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();

        try {
            RagFlowApiResponse<String> ragFlowUpdateKnowledgeBaseResp = agentBaseRagClient.modifyKnowledgeRepo(
                getUri(connectionId), getHeaders(), ragFlowUpdateKnowledgeBaseRequest, projectId, workspaceId,
                knowledgeRepoId);
            return ragFlowUpdateKnowledgeBaseResp.getData();
        } catch (Exception exception) {
            log.error("Fail to modify knowledge repo, projectId: {}, repoName: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getDisplayName());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void deleteKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        String connectionId = agentBaseRagConnectionProvider.getConnectionIdByRepoId(
            knowledgeRepo.getKnowledgeRepoId());
        String projectId = RequestContextUtils.getRequestProjectId();
        String workspaceId = knowledgeRepo.getWorkspaceId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            RagFlowApiResponse<Boolean> deleteKnowledgeResp = agentBaseRagClient.deleteKnowledgeRepo(
                getUri(connectionId), getHeaders(), projectId, workspaceId, knowledgeRepoId);
            log.info("Success to delete KnowledgeRepo: {}", deleteKnowledgeResp);
        } catch (Exception exception) {
            log.error("Fail to delete knowledge repo, projectId: {}, knowledgeRepoId: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getKnowledgeRepoId());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public KnowledgeRepo retrieveKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        String workspaceId = knowledgeRepo.getWorkspaceId();
        String projectId = RequestContextUtils.getRequestProjectId();
        String connectionId = agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
        RagFlowApiResponse<RagFlowRetrieveKnowledgeBaseResp> ragFlowRetrieveKnowledgeBaseResp;
        try {
            ragFlowRetrieveKnowledgeBaseResp = agentBaseRagClient.retrieveKnowledgeRepo(getUri(connectionId),
                getHeaders(), projectId, workspaceId, knowledgeRepoId);
            log.info("Success to retrieve KnowledgeRepo: {}", knowledgeRepoId);
        } catch (Exception exception) {
            log.error("Fail to retrieve knowledge repo, projectId: {}, knowledgeRepoId: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepoId);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        if (Objects.isNull(ragFlowRetrieveKnowledgeBaseResp)) {
            log.error("Knowledge repo is not exist in kooSearch: {}", knowledgeRepoId);
            throw new AgentBaseException(ErrorCode.RESOURCE_NOT_EXIST, knowledgeRepoId);
        }
        RagFlowRetrieveKnowledgeBaseResp knowledgeBaseRespData = ragFlowRetrieveKnowledgeBaseResp.getData();
        KnowledgeRepo realKnowledgeRepo = new KnowledgeRepo();
        realKnowledgeRepo.setEmbeddingModel(knowledgeBaseRespData.getEmbeddingModel());
        realKnowledgeRepo.setRerankModel(knowledgeBaseRespData.getRerankModel());
        RagChunkParserConf ragChunkParserConf = new RagChunkParserConf();
        ragChunkParserConf.setChunkMethod(
            RagChunkParserConf.ChunkMethodEnum.fromValue(knowledgeBaseRespData.getParserId()));
        ragChunkParserConf.setChunkTokenNum(knowledgeBaseRespData.getParserConfig().getChunkTokenNum());
        if (ragChunkParserConf.getChunkMethod().equals(RagChunkParserConf.ChunkMethodEnum.NAIVE)) {
            ragChunkParserConf.setDelimiter(
                DelimiterUtils.delimiterStringToList(knowledgeBaseRespData.getParserConfig().getDelimiter()));
        }
        realKnowledgeRepo.setRagChunkParserConf(ragChunkParserConf);
        realKnowledgeRepo.setSplitConf(new SplitConf());
        realKnowledgeRepo.setParseConf(new ParseConf());
        return realKnowledgeRepo;
    }

    @Override
    public ListKnowledgeRepoResp listKnowledgeRepos(ListKnowledgeRepoReq listKnowledgeRepoReq) {
        return null;
    }

    @Override
    public String uploadFile(KnowledgeRepo knowledgeRepo, MultipartFile resource, List<String> tags) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            RagFlowApiResponse<RagFlowUploadFileResp> response = agentBaseRagClient.uploadFile(
                getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId)), getHeaders(),
                projectId, knowledgeRepoId, resource);
            return response.getData().getId();
        } catch (Exception e) {
            log.error("Fail to upload file: {}", e);
            throw new AgentBaseException(ErrorCode.FAIL_TO_UPLOAD_KNOWLEDGE_REPO_FILE, e);
        }
    }

    @Override
    public void deleteFile(KnowledgeRepo knowledgeRepo, String fileId) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        RagFlowDeleteDataSetRequest request = new RagFlowDeleteDataSetRequest();
        request.setFileIds(Set.of(fileId));
        agentBaseRagClient.deleteFile(getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId)),
            getHeaders(), projectId, knowledgeRepoId, request);
    }

    @Override
    public BatchDeleteKnowledgeFilesResponseBody batchDeleteFile(KnowledgeRepo knowledgeRepo,
        BatchDeleteKnowledgeFilesRequestBody deleteFileReq) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        RagFlowDeleteDataSetRequest request = new RagFlowDeleteDataSetRequest();
        request.setFileIds(new HashSet<>(deleteFileReq.getFileIds()));
        agentBaseRagClient.deleteFile(getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(
                agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepo.getKnowledgeRepoId()))), getHeaders(),
            projectId, knowledgeRepoId, request);
        return new BatchDeleteKnowledgeFilesResponseBody().setTotalCount(deleteFileReq.getFileIds().size())
            .setDeletedCount(deleteFileReq.getFileIds().size());
    }

    @Override
    public ListKnowledgeFilesResponseBody listFiles(KnowledgeRepo knowledgeRepo, ListFileReq listFileReq) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        RagFlowListFilesRequest request = RagFlowListFilesRequest.builder()
            .type(listFileReq.getFileType())
            .keywords(listFileReq.getFileName())
            .limit(listFileReq.getPageSize())
            .offset((listFileReq.getPageNum() - 1) * listFileReq.getPageSize())
            .build();
        if (listFileReq.getFileStatus() != null) {
            request.setStatus(List.of(FileStatusEnum.converse_convert(listFileReq.getFileStatus())));
        }
        RagFlowApiResponse<RagFlowListFilesResp> response = agentBaseRagClient.listFiles(
            getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId)), getHeaders(), projectId,
            knowledgeRepoId, request);
        int total = response.getData().getTotal();
        List<Document> documents = response.getData().getDocs();
        List<FileInfo> fileInfos = ObjectUtils.firstNonNull(documents, Collections.<Document>emptyList())
            .stream()
            .map(document -> new FileInfo().setFileId(document.getId())
                .setFileName(document.getName())
                .setFileSize(document.getSize())
                .setProjectId(projectId)
                .setFileStatus(FileStatusEnum.convert(document.getRun()))
                .setFileType(document.getType())
                .setCreateTime(document.getCreateTime())
                .setUpdateTime(document.getUpdateTime())
                .setMetadata(null)
                .setFailureReason(document.getProgressMsg()))
            .toList();
        return new ListKnowledgeFilesResponseBody().setCount(total).setFileInfoList(fileInfos);
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(KnowledgeRepo knowledgeRepo, String fileId) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        return agentBaseRagClient.downloadFileContent(
            getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId)), getHeaders(), projectId,
            knowledgeRepoId, fileId);
    }

    @Override
    public void createFileChunk(KnowledgeRepo knowledgeRepo, String fileId, FileChunkReq fileChunkReq) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        RagFlowCreateChunkRequest request = new RagFlowCreateChunkRequest();
        request.setContent(fileChunkReq.getContent());
        request.setTitle(fileChunkReq.getTitle());
        agentBaseRagClient.creatChunk(getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId)),
            getHeaders(), projectId, knowledgeRepoId, fileId, request);
    }

    @Override
    public void updateFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId, FileChunkReq fileChunkReq) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        RagFlowCreateChunkRequest request = new RagFlowCreateChunkRequest();
        request.setContent(fileChunkReq.getContent());
        request.setTitle(fileChunkReq.getTitle());
        agentBaseRagClient.updateChunk(getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId)),
            getHeaders(), projectId, knowledgeRepoId, fileId, chunkId, request);
    }

    @Override
    public void updateFileMetaInfo(KnowledgeRepo knowledgeRepo, String fileId,
        UpdateFileMetaInfoReq updateFileMetaInfoReq) {
        log.warn("do not support updateMetaInfo yet");
        throw new AgentBaseException("do not support updateMetaInfo yet");
    }

    @Override
    public void deleteFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        RagFlowDeleteChunkRequest request = new RagFlowDeleteChunkRequest();
        request.setChunkIds(List.of(chunkId));
        agentBaseRagClient.deleteChunk(getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId)),
            getHeaders(), projectId, knowledgeRepoId, fileId, request);
    }

    @Override
    public FileChunkListRsp listFileChunks(KnowledgeRepo knowledgeRepo, ListFileChunksReq listFileChunksReq) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        RagFlowApiResponse<RagFlowListChunkResp> result = agentBaseRagClient.listFileChunks(
            getUri(agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId)), getHeaders(), projectId,
            knowledgeRepoId, listFileChunksReq.getFileId(), listFileChunksReq.getPageSize(),
            listFileChunksReq.getPageNum());
        if (HttpStatus.OK.value() == result.getCode()) {
            int total = Optional.ofNullable(result.getData()).map(RagFlowListChunkResp::getTotal).orElse(0);
            FileChunkListRsp fileChunkListRsp = new FileChunkListRsp();
            List<FileChunkInfo> fileChunkList = new ArrayList<>();
            result.getData().getChunks().forEach(item -> {
                FileChunkInfo fileChunkInfo = new FileChunkInfo();
                fileChunkInfo.setId(item.getChunkId());
                fileChunkInfo.setKnowledgeRepoId(knowledgeRepoId);
                fileChunkInfo.setTitle(item.getDocnmKwd());
                fileChunkInfo.setContent(item.getContentWithWeight());
                fileChunkList.add(fileChunkInfo);

            });
            fileChunkListRsp.setCount((long) total);
            fileChunkListRsp.setFileChunkList(fileChunkList);
            return fileChunkListRsp;
        } else {
            log.error("Call rag flow to retrieve chunk failed. Because of {}", result.getMessage());
            throw new AgentBaseException(ErrorCode.CALL_EXTERNAL_KNOWLEDGE_ERROR);
        }
    }

    @Override
    public SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode,
        List<String> tags, int pageNum, int pageSize) {
        String projectId = RequestContextUtils.getRequestProjectId();
        String knowledgeRepoId = knowledgeRepos.get(0).getKnowledgeRepoId();
        String connectionId = agentBaseRagConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
        SearchTextResp searchTextResp = new SearchTextResp();
        try {
            // 调用KooSearch接口检索知识库文档
            RagFlowApiResponse<RetrieveChunksResponseBody> ragFlowSearchTextResp = agentBaseRagClient.searchText(
                getUri(connectionId), getHeaders(), projectId, RagFlowSearchTextRequest.builder()
                    .question(query)
                    .knowledgeBaseIds(knowledgeRepos.stream().map(KnowledgeRepo::getKnowledgeRepoId).toList())
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .build());
            log.info("Success to search knowledge repo: {}", knowledgeRepoId);

            searchTextResp.setTotal(Optional.ofNullable(ragFlowSearchTextResp.getData())
                .map(RetrieveChunksResponseBody::getTotal)
                .orElse(0));
            searchTextResp.setDocList(Optional.ofNullable(ragFlowSearchTextResp.getData())
                .map(RetrieveChunksResponseBody::getChunks)
                .orElse(
                    Collections.<RetrieveChunkInfo>emptyList())
                .stream()
                .map(item -> {
                    ChatReferenceInfo chatReferenceInfo = new ChatReferenceInfo();
                    chatReferenceInfo.setTitle(item.getDocumentKeyword());
                    chatReferenceInfo.setSubtitle(item.getDocumentKeyword());
                    chatReferenceInfo.setContent(item.getContentWithWeight());
                    chatReferenceInfo.setScore(item.getSimilarity());
                    chatReferenceInfo.setRepoId(item.getDatasetId());
                    chatReferenceInfo.setFileId(item.getDocumentId());
                    chatReferenceInfo.setChunkId(item.getId());
                    chatReferenceInfo.setDocType(item.getDocTypeKwd());
                    return chatReferenceInfo;
                })
                .toList());
        } catch (Exception exception) {
            log.error("Fail to search text from knowledge repo, knowledgeRepoId: {}.", knowledgeRepoId, exception);
            throw new AgentBaseException(
                "Fail to search text from knowledge repo, knowledgeRepoId:" + knowledgeRepoId + ".");
        }
        return searchTextResp;
    }

    @Override
    public String createFaq(KnowledgeFaq knowledgeFaq) {
        log.warn("do not support createFaq yet");
        throw new AgentBaseException("do not support createFaq yet");
    }

    @Override
    public ListFaqResp listFaq(FaqSearchCriteria faqSearchCriteria) {
        log.warn("do not support listFaq yet");
        throw new AgentBaseException("do not support listFaq yet");
    }

    @Override
    public void startKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        // AgentBaseRag 目前不支持启用知识库
    }

    @Override
    public void stopKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        // AgentBaseRag 目前不支持停用知识库
    }

    @Override
    public KnowledgeSegRuleInfo createSegmentRule(KnowledgeSegmentRule segmentRule, String repoId) {
        log.warn("do not support createSegmentRule yet");
        throw new AgentBaseException("do not support createSegmentRule yet");
    }

    @Override
    public String modifySegmentRule(KnowledgeSegmentRule segmentRule) {
        log.warn("do not support modifySegmentRule yet");
        throw new AgentBaseException("do not support modifySegmentRule yet");
    }

    @Override
    public void deleteSegmentRule(String ruleId) {
        log.warn("do not support deleteSegmentRule yet");
        throw new AgentBaseException("do not support deleteSegmentRule yet");
    }

    @Override
    public void deleteFaq(String repoId, String faqId) {
        log.warn("do not support deleteFaq yet");
        throw new AgentBaseException("do not support deleteFaq yet");
    }

    @Override
    public Integer deleteFaqBatch(String repoId, List<String> faqIds) {
        log.warn("do not support deleteFaqBatch yet");
        throw new AgentBaseException("do not support deleteFaqBatch yet");
    }

    @Override
    public String modifyFaq(KnowledgeFaq knowledgeFaq) {
        log.warn("do not support modifyFaq yet");
        throw new AgentBaseException("do not support modifyFaq yet");
    }

    @Override
    public KnowledgeFaq retrieveFaq(String faqId, String repoId) {
        log.warn("do not support retrieveFaq yet");
        throw new AgentBaseException("do not support retrieveFaq yet");
    }

    @Override
    public List<ModelInfo> listModels(ModelSearchCriteria searchCriteria, String connectionId) {
        // 此处不写代码，在agent-manager中对应调用模型中心接口
        log.warn("do not support listModels yet");
        throw new AgentBaseException("do not support listModels yet");
    }

    @Override
    public CreateKnowledgeTaskResponseBody createTask(String repoId, String taskType, List<String> fileIds) {
        String connectionId = agentBaseRagConnectionProvider.getConnectionIdByRepoId(repoId);
        String projectId = RequestContextUtils.getRequestProjectId();
        RagFlowApiResponse<Boolean> ragFlowCreateTaskResp;
        if (taskType.equals("RETRY_FILES")) {
            ragFlowCreateTaskResp = agentBaseRagClient.createTask(getUri(connectionId), getHeaders(), projectId, repoId,
                RagFlowCreateTaskRequest.builder().delete(true).run(1).docIds(fileIds).build());
            return new CreateKnowledgeTaskResponseBody().setTotalCount(1)
                .setCreatedCount(ragFlowCreateTaskResp.getData() ? 1 : 0);
        }
        return new CreateKnowledgeTaskResponseBody();
    }

    @Override
    public ListKnowledgeTasksResponseBody listKnowledgeTask(String repoId, ListTaskCriteria listTaskCriteria) {
        log.warn("do not support listKnowledgeTask yet");
        throw new AgentBaseException("do not support listKnowledgeTask yet");
    }

    @Override
    public CommonBatchDeleteRsp deleteKnowledgeTask(String repoId, List<String> taskIds) {
        log.warn("do not support deleteKnowledgeTask yet");
        throw new AgentBaseException("do not support deleteKnowledgeTask yet");
    }

    @Override
    public String uploadFaqFile(KnowledgeRepo knowledgeRepo, MultipartFile resource) {
        log.warn("do not support uploadFaqFile yet");
        throw new AgentBaseException("do not support uploadFaqFile yet");
    }

    @Override
    public void deleteFaqFile(KnowledgeRepo knowledgeRepo, String fileId) {
        log.warn("do not support deleteFaqFile yet");
        throw new AgentBaseException("do not support deleteFaqFile yet");
    }

    @Override
    public FaqFileInfoListRsp listFaqFiles(KnowledgeRepo knowledgeRepo, ListFaqFileReq listFaqFileReq) {
        log.warn("do not support listFaqFiles yet");
        throw new AgentBaseException("do not support listFaqFiles yet");
    }

    @Override
    public ResponseEntity<byte[]> downloadFaqFile(KnowledgeRepo knowledgeRepo, String fileId) {
        log.warn("do not support downloadFaqFile yet");
        throw new AgentBaseException("do not support downloadFaqFile yet");
    }

    @Override
    public void createFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, FaqFileChunkReq faqFileChunkReq) {
        log.warn("do not support createFaqFileChunk yet");
        throw new AgentBaseException("do not support createFaqFileChunk yet");
    }

    @Override
    public void updateFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId,
        FaqFileChunkReq faqFileChunkReq) {
        log.warn("do not support updateFaqFileChunk yet");
        throw new AgentBaseException("do not support updateFaqFileChunk yet");
    }

    @Override
    public void deleteFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId) {
        log.warn("do not support deleteFaqFileChunk yet");
        throw new AgentBaseException("do not support deleteFaqFileChunk yet");
    }

    @Override
    public FaqFileChunkListRsp listFaqFileChunks(KnowledgeRepo knowledgeRepo,
        ListFaqFileChunksReq listFaqFileChunksReq) {
        log.warn("do not support listFaqFileChunks yet");
        throw new AgentBaseException("do not support listFaqFileChunks yet");
    }

    @Override
    public ListKnowledgeTagsResp listKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, int pageNum, int pageSize) {
        log.warn("do not support listKnowledgeRepoTags yet");
        throw new AgentBaseException("do not support listKnowledgeRepoTags yet");
    }

    @Override
    public CreateKnowledgeRepoTagsRsp createKnowledgeRepoTags(KnowledgeRepo knowledgeRepo,
        CreateKnowledgeRepoTagsReq body) {
        log.warn("do not support createKnowledgeRepoTags yet");
        throw new AgentBaseException("do not support createKnowledgeRepoTags yet");
    }

    @Override
    public DeleteKnowledgeRepoTagsRsp deleteKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, String tagId,
        DeleteKnowledgeRepoTagsReq body) {
        log.warn("do not support deleteKnowledgeRepoTags yet");
        throw new AgentBaseException("do not support deleteKnowledgeRepoTags yet");
    }

    @Override
    public Resource queryImage(KnowledgeRepo knowledgeRepo, String imageId) {
        log.warn("do not support queryImage yet");
        throw new AgentBaseException("do not support queryImage yet");
    }

}
