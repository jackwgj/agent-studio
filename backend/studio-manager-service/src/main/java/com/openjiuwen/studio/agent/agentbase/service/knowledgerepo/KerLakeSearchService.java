/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import com.openjiuwen.studio.agent.agentbase.client.AgentBaseLakeSearchClient;
import com.openjiuwen.studio.agent.agentbase.common.constant.Constants;
import com.openjiuwen.studio.agent.agentbase.common.enums.LakeSearchApi;
import com.openjiuwen.studio.agent.agentbase.model.BatchCreateResponse;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeLabelsRsp;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.CreateKnowledgeRepoReq;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeTaskRequestBody;
import com.openjiuwen.studio.agent.agentbase.model.CssFileInfo;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeLabelsRsp;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeRepoReq;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqDeleteBatchResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqInfo;
import com.openjiuwen.studio.agent.agentbase.model.FaqListReq;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqReq;
import com.openjiuwen.studio.agent.agentbase.model.FaqResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqSearchCriteria;
import com.openjiuwen.studio.agent.common.dto.knowledge.FileDocInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.FileExtract;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeRepo;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeRepoInfo;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeSegRuleInfo;
import com.openjiuwen.studio.agent.agentbase.model.ListFaqResponse;
import com.openjiuwen.studio.agent.agentbase.model.ListFileDocsRsp;
import com.openjiuwen.studio.agent.agentbase.model.ListFilesRsp;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeLabelsResp;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.ListTaskCriteria;
import com.openjiuwen.studio.agent.agentbase.model.ModelSearchCriteria;
import com.openjiuwen.studio.agent.agentbase.model.ModifyKnowledgeRepoRequestBody;
import com.openjiuwen.studio.agent.agentbase.model.ResultModel;
import com.openjiuwen.studio.agent.common.dto.knowledge.SearchTextReq;
import com.openjiuwen.studio.agent.agentbase.model.SearchTextResp;
import com.openjiuwen.studio.agent.agentbase.model.SegmentRule;
import com.openjiuwen.studio.agent.agentbase.model.UploadFilesResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.ListModelResp;
import com.openjiuwen.studio.agent.foundation.base.utils.JacksonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.base.mapping.ErrorCodeMappingService;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesRequestBody;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CommonBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeTaskReq;
import com.openjiuwen.studio.agent.common.dto.ErrorDetail;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FaqFileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FaqFileInfoListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkInfo;
import com.openjiuwen.studio.agent.manager.dto.FileChunkListRsp;
import com.openjiuwen.studio.agent.manager.dto.FileChunkReq;
import com.openjiuwen.studio.agent.manager.dto.FileInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeFaq;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeSegmentRule;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeTagInfo;
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
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.ParseConf;
import com.openjiuwen.studio.agent.common.dto.knowledge.SplitConf;
import com.openjiuwen.studio.agent.manager.dto.UpdateFileMetaInfoReq;
import com.openjiuwen.studio.agent.manager.dto.UploadFaqFileRsp;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LakeSearch知识库Service，使用kerberos认证
 *
 * @since 2025-06-14
 */
@Slf4j
@Service("MgKerLakeSearch")
public class KerLakeSearchService implements KnowledgeRepoService {

    @Value("${knowledge.lakeSearch.project-id}")
    private String lakeSearchProjectId;

    private final AgentBaseLakeSearchClient agentBaseLakeSearchClient;

    private final ErrorCodeMappingService errorCodeMappingService;

    private final KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    public KerLakeSearchService(AgentBaseLakeSearchClient agentBaseLakeSearchClient,
        ErrorCodeMappingService errorCodeMappingService,
        KnowledgeConnectionRouterService knowledgeConnectionRouterService) {
        this.agentBaseLakeSearchClient = agentBaseLakeSearchClient;
        this.errorCodeMappingService = errorCodeMappingService;
        this.knowledgeConnectionRouterService = knowledgeConnectionRouterService;
    }

    @Override
    public CreateKnowledgeRepoInfo createKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        CreateKnowledgeRepoReq createKnowledgeRepoReq = CreateKnowledgeRepoReq.builder()
            .name(knowledgeRepo.getDisplayName())
            .detail(knowledgeRepo.getDescription())
            .embeddingModel(knowledgeRepo.getEmbeddingModel())
            .reRankModel(knowledgeRepo.getRerankModel())
            .repoType(knowledgeRepo.getRepoType().toString())
            .fileExtract(FileExtract.builder()
                .parseConf(knowledgeRepo.getParseConf())
                .splitConf(knowledgeRepo.getSplitConf())
                .build())
            .languageId(Constants.ZH_LANGUAGE_ID)
            .build();
        String requestContent = JacksonUtils.toJson(createKnowledgeRepoReq);
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            LakeSearchApi.CREATE_KNOWLEDGE_REPO.getEndpoint(), requestContent, LakeSearchApi.CREATE_KNOWLEDGE_REPO,
            null);
        CreateKnowledgeRepoResp createKnowledgeRepoResp = convertResultModel(resultModel,
            CreateKnowledgeRepoResp.class);
        if (Objects.isNull(createKnowledgeRepoResp)) {
            log.error("Fail to create LakeSearch knowledge repo [{}] for [{}]", createKnowledgeRepoReq.getName(),
                resultModel.getErrorDetail());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        CreateKnowledgeRepoInfo createKnowledgeRepoInfo = new CreateKnowledgeRepoInfo();
        createKnowledgeRepoInfo.setKnowledgeBaseId(createKnowledgeRepoResp.getRepoId());
        // 待添加connection_id
        return createKnowledgeRepoInfo;
    }

    @Override
    public String modifyKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        ModifyKnowledgeRepoRequestBody modifyKnowledgeRepoRequestBody = ModifyKnowledgeRepoRequestBody.builder()
            .name(knowledgeRepo.getDisplayName())
            .build();
        Optional.ofNullable(knowledgeRepo.getRerankModel()).ifPresent(modifyKnowledgeRepoRequestBody::setRerankModel);
        if (!Objects.isNull(knowledgeRepo.getSplitConf()) || !Objects.isNull(knowledgeRepo.getParseConf())) {
            FileExtract fileExtract = new FileExtract();
            Optional.ofNullable(knowledgeRepo.getSplitConf()).ifPresent(fileExtract::setSplitConf);
            Optional.ofNullable(knowledgeRepo.getParseConf()).ifPresent(fileExtract::setParseConf);
            modifyKnowledgeRepoRequestBody.setFileExtract(fileExtract);
        }
        String requestContent = JacksonUtils.toJson(modifyKnowledgeRepoRequestBody);
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.MODIFY_KNOWLEDGE_REPO.getEndpoint(), knowledgeRepo.getKnowledgeRepoId()),
            requestContent, LakeSearchApi.MODIFY_KNOWLEDGE_REPO, null);
        ModifyKnowledgeRepoResponseBody modifyKnowledgeRepoRsp = convertResultModel(resultModel,
            ModifyKnowledgeRepoResponseBody.class);
        if (Objects.isNull(modifyKnowledgeRepoRsp)) {
            log.error("Fail to modify LakeSearch knowledge repo [{}] for [{}]", knowledgeRepo.getKnowledgeRepoId(),
                resultModel.getErrorDetail());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return modifyKnowledgeRepoRsp.getKnowledgeRepoId();
    }

    @Override
    public void deleteKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        DeleteKnowledgeRepoReq deleteKnowledgeRepoReq = DeleteKnowledgeRepoReq.builder()
            .repoIds(Collections.singletonList(knowledgeRepo.getKnowledgeRepoId()))
            .build();
        String requestContent = JacksonUtils.toJson(deleteKnowledgeRepoReq);
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            LakeSearchApi.DELETE_KNOWLEDGE_REPO.getEndpoint(), requestContent, LakeSearchApi.DELETE_KNOWLEDGE_REPO,
            null);
        DeleteKnowledgeResp deleteKnowledgeResp = convertResultModel(resultModel, DeleteKnowledgeResp.class);
        if (Objects.isNull(deleteKnowledgeResp)) {
            log.error("Fail to delete LakeSearch knowledge repo [{}] for [{}]", deleteKnowledgeRepoReq.getRepoIds(),
                resultModel.getErrorDetail());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public KnowledgeRepo retrieveKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.RETRIEVE_KNOWLEDGE_REPO.getEndpoint(), repoId), null,
            LakeSearchApi.RETRIEVE_KNOWLEDGE_REPO, null);
        KnowledgeRepoInfo knowledgeRepoInfo = convertResultModel(resultModel, KnowledgeRepoInfo.class);
        int statusCode = resultModel.getStatusCode();

        // 查询过程中的异常状态，状态码非2xx，非404
        if (statusCode != HttpStatus.SC_NOT_FOUND && statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to retrieve LakeSearch knowledge repo [{}] for [{}]", repoId, resultModel.getErrorDetail());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        // 知识库不存在
        if (Objects.isNull(knowledgeRepoInfo)) {
            log.error("Knowledge repo is not exist in LakeSearch, repoId: [{}]", repoId);
            throw new AgentBaseException(ErrorCode.RESOURCE_NOT_EXIST);
        }
        return new KnowledgeRepo().setDisplayName(knowledgeRepoInfo.getName())
            .setKnowledgeRepoId(knowledgeRepoInfo.getId())
            .setStatus(knowledgeRepoInfo.getStatus())
            .setEmbeddingModel(knowledgeRepoInfo.getEmbeddingModel())
            .setRerankModel(knowledgeRepoInfo.getRerankModel())
            .setParseConf(Optional.ofNullable(knowledgeRepoInfo.getFileExtract())
                .map(FileExtract::getParseConf)
                .orElse(new ParseConf()))
            .setSplitConf(Optional.ofNullable(knowledgeRepoInfo.getFileExtract())
                .map(FileExtract::getSplitConf)
                .orElse(new SplitConf()));
    }

    @Override
    public ListKnowledgeRepoResp listKnowledgeRepos(ListKnowledgeRepoReq req) {
        // 拼接调用地址
        StringBuilder endpoint = new StringBuilder(LakeSearchApi.QUERY_KNOWLEDGE_REPOS.getEndpoint());
        if (!Objects.isNull(req) && !Objects.isNull(req.getPageNum()) && !Objects.isNull(req.getPageSize())) {
            endpoint.append("?page_num=").append(req.getPageNum()).append("&page_size=").append(req.getPageSize());
            if (StringUtils.isNoneBlank(req.getName())) {
                endpoint.append("&name=").append(urlEncode(req.getName()));
            }
            if (StringUtils.isNoneBlank(req.getType())) {
                endpoint.append("&type=").append(urlEncode(req.getType()));
            }
        }
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint.toString(), null,
            LakeSearchApi.QUERY_KNOWLEDGE_REPOS, null);
        ListKnowledgeRepoResp knowledgeRepoInfos = convertResultModel(resultModel, ListKnowledgeRepoResp.class);
        if (Objects.isNull(knowledgeRepoInfos)) {
            log.error("Fail to list LakeSearch knowledge repos for [{}]", resultModel.getErrorDetail());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return knowledgeRepoInfos;
    }

    @Override
    public String uploadFile(KnowledgeRepo knowledgeRepo, MultipartFile file, List<String> tags) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.UPLOAD_FILE.getEndpoint(), repoId), null, LakeSearchApi.UPLOAD_FILE, file);
        UploadFilesResp uploadFilesResp = convertResultModel(resultModel, UploadFilesResp.class);
        if (Objects.isNull(uploadFilesResp)) {
            log.error("Fail to upload file [{}] to LakeSearch knowledge repo [{}]", file.getOriginalFilename(), repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return uploadFilesResp.getFileId();
    }

    @Override
    public void deleteFile(KnowledgeRepo knowledgeRepo, String fileId) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.DELETE_FILE.getEndpoint(), repoId, fileId), null, LakeSearchApi.DELETE_FILE,
            null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to delete file [{}] in LakeSearch knowledge repo [{}]", fileId, repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public BatchDeleteKnowledgeFilesResponseBody batchDeleteFile(KnowledgeRepo knowledgeRepo,
        BatchDeleteKnowledgeFilesRequestBody deleteFileReq) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        String requestContent = JacksonUtils.toJson(deleteFileReq);
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.BATCH_DELETE_FILE.getEndpoint(), repoId), requestContent,
            LakeSearchApi.BATCH_DELETE_FILE, null);
        BatchDeleteKnowledgeFilesResponseBody batchDeleteFileResp = convertResultModel(resultModel,
            BatchDeleteKnowledgeFilesResponseBody.class);
        if (Objects.isNull(batchDeleteFileResp)) {
            log.error("Fail to batch delete files in LakeSearch knowledge repo [{}] for [{}]",
                knowledgeRepo.getKnowledgeRepoId(), resultModel.getErrorDetail());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return batchDeleteFileResp;
    }

    @Override
    public ListKnowledgeFilesResponseBody listFiles(KnowledgeRepo knowledgeRepo, ListFileReq listFileReq) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        StringBuilder endpoint = new StringBuilder(String.format(LakeSearchApi.QUERY_FILES.getEndpoint(), repoId));
        if (!Objects.isNull(listFileReq) && !Objects.isNull(listFileReq.getPageNum()) && !Objects.isNull(
            listFileReq.getPageSize())) {
            endpoint.append("?page_num=")
                .append(listFileReq.getPageNum())
                .append("&page_size=")
                .append(listFileReq.getPageSize());
            if (StringUtils.isNoneBlank(listFileReq.getFileName())) {
                endpoint.append("&file_name=").append(urlEncode(listFileReq.getFileName()));
            }
            if (StringUtils.isNoneBlank(listFileReq.getFileType())) {
                endpoint.append("&file_type=").append(urlEncode(listFileReq.getFileType()));
            }
        }
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint.toString(), null,
            LakeSearchApi.QUERY_FILES, null);
        ListFilesRsp listFilesRsp = convertResultModel(resultModel, ListFilesRsp.class);
        if (Objects.isNull(listFilesRsp)) {
            log.error("Fail to list files in LakeSearch knowledge repo [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        List<FileInfo> fileInfos = new ArrayList<>();
        for (CssFileInfo cssFileInfo : listFilesRsp.getFiles()) {
            fileInfos.add(getFileInfoFromCssFileInfo(cssFileInfo));
        }
        return new ListKnowledgeFilesResponseBody().setFileInfoList(fileInfos).setCount(listFilesRsp.getTotal());
    }

    private FileInfo getFileInfoFromCssFileInfo(CssFileInfo cssFileInfo) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(cssFileInfo.getId());
        fileInfo.setFileName(cssFileInfo.getName());
        fileInfo.setFileType(cssFileInfo.getType());
        fileInfo.setFileSize(cssFileInfo.getSize());
        fileInfo.setProjectId(RequestContextUtils.getRequestProjectId());
        fileInfo.setFileStatus(cssFileInfo.getStatus());
        fileInfo.setFileTags(cssFileInfo.getTags());
        fileInfo.setFailureReason(cssFileInfo.getUploadDesc());
        fileInfo.setCreateTime(Long.parseLong(cssFileInfo.getCreateTime()));
        fileInfo.setUpdateTime(Long.parseLong(cssFileInfo.getCreateTime()));
        return fileInfo;
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(KnowledgeRepo knowledgeRepo, String fileId) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.DOWNLOAD_FILE.getEndpoint(), fileId, knowledgeRepo.getKnowledgeRepoId()), null,
            LakeSearchApi.DOWNLOAD_FILE, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to download file [{}] from LakeSearch", fileId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return resultModel.getResponseEntity();
    }

    @Override
    public void createFileChunk(KnowledgeRepo knowledgeRepo, String fileId, FileChunkReq fileChunkReq) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.CREATE_FILE_CHUNK.getEndpoint(), fileId, knowledgeRepo.getKnowledgeRepoId()),
            JacksonUtils.toJson(fileChunkReq), LakeSearchApi.CREATE_FILE_CHUNK, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to create file chunk in LakeSearch file [{}]", fileId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public void updateFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId, FileChunkReq fileChunkReq) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.MODIFY_FILE_CHUNK.getEndpoint(), fileId, chunkId,
                knowledgeRepo.getKnowledgeRepoId()), JacksonUtils.toJson(fileChunkReq), LakeSearchApi.MODIFY_FILE_CHUNK,
            null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to update file chunk in LakeSearch file [{}]", fileId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public void updateFileMetaInfo(KnowledgeRepo knowledgeRepo, String fileId,
        UpdateFileMetaInfoReq updateFileMetaInfoReq) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        StringBuilder endpoint = new StringBuilder(
            String.format(LakeSearchApi.UPDATE_FILE_INFO.getEndpoint(), repoId, fileId));
        if (!Objects.isNull(updateFileMetaInfoReq) && !Objects.isNull(updateFileMetaInfoReq.getTags())) {
            List<String> tags = updateFileMetaInfoReq.getTags();
            String queryString = tags.stream()
                .map(tag -> "tags=" + URLEncoder.encode(tag, StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
            endpoint.append("?").append(queryString);
        }

        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint.toString(), null,
            LakeSearchApi.UPDATE_FILE_INFO, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to update file meta info in LakeSearch file [{}]", fileId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public void deleteFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.DELETE_FILE_CHUNK.getEndpoint(), fileId, knowledgeRepo.getKnowledgeRepoId()),
            JacksonUtils.toJson(Collections.singletonList(chunkId)), LakeSearchApi.DELETE_FILE_CHUNK, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to delete file chunk in LakeSearch file [{}]", fileId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public FileChunkListRsp listFileChunks(KnowledgeRepo knowledgeRepo, ListFileChunksReq listFileChunksReq) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        StringBuilder endpoint = new StringBuilder(
            String.format(LakeSearchApi.QUERY_FILE_CHUNKS.getEndpoint(), listFileChunksReq.getFileId()));
        if (!Objects.isNull(listFileChunksReq.getPageNum()) && !Objects.isNull(listFileChunksReq.getPageSize())) {
            endpoint.append("?repo_id=")
                .append(knowledgeRepo.getKnowledgeRepoId())
                .append("&page_num=")
                .append(listFileChunksReq.getPageNum())
                .append("&page_size=")
                .append(listFileChunksReq.getPageSize());
        }
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint.toString(), null,
            LakeSearchApi.QUERY_FILE_CHUNKS, null);
        ListFileDocsRsp listFileDocsRsp = convertResultModel(resultModel, ListFileDocsRsp.class);
        if (Objects.isNull(listFileDocsRsp)) {
            log.error("Fail to list file chunks in LakeSearch knowledge repo [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        FileChunkListRsp fileChunkListRsp = new FileChunkListRsp();
        fileChunkListRsp.setCount(listFileDocsRsp.getTotal());
        fileChunkListRsp.setFileChunkList(getFileChunkInfoFromFileDocInfo(listFileDocsRsp.getDocs()));
        return fileChunkListRsp;
    }

    private List<FileChunkInfo> getFileChunkInfoFromFileDocInfo(List<FileDocInfo> fileDocInfos) {
        List<FileChunkInfo> fileChunkInfos = new ArrayList<>();
        if (CollectionUtils.isEmpty(fileDocInfos)) {
            return fileChunkInfos;
        }
        for (FileDocInfo fileDocInfo : fileDocInfos) {
            FileChunkInfo fileChunkInfo = new FileChunkInfo();
            fileChunkInfo.setId(fileDocInfo.getId());
            fileChunkInfo.setTimestamp(fileDocInfo.getTimestamp());
            fileChunkInfo.setContent(fileDocInfo.getContent());
            fileChunkInfo.setTitle(fileDocInfo.getTitle());
            fileChunkInfo.setPageNum(fileDocInfo.getPageNum());
            fileChunkInfo.setComponentNum(fileDocInfo.getComponentNum());
            fileChunkInfos.add(fileChunkInfo);
        }
        return fileChunkInfos;
    }

    @Override
    public SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode,
        List<String> tags, int pageNum, int pageSize) {
        String repoId = knowledgeRepos.get(0).getKnowledgeRepoId();
        SearchTextReq searchTextReq = SearchTextReq.builder()
            .repoId(repoId)
            .content(query)
            .scope(searchMode)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .filterString(buildFilterStrForTags(tags))
            .extraRepoIds(knowledgeRepos.stream()
                .map(KnowledgeRepo::getKnowledgeRepoId)
                .filter(item -> !item.equals(repoId))
                .toList())
            .build();
        String requestContent = JacksonUtils.toJson(searchTextReq);
        log.info("Start to search text from lakeSearch with request body [{}]", requestContent);
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(LakeSearchApi.SEARCH_TEXT.getEndpoint(),
            requestContent, LakeSearchApi.SEARCH_TEXT, null);
        SearchTextResp searchTextResp = convertResultModel(resultModel, SearchTextResp.class);
        if (Objects.isNull(searchTextResp)) {
            log.error("Fail to search text from LakeSearch knowledge repo [{}]", searchTextReq.getRepoId());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        log.info("Success to search text from knowledge repo, result size: [{}]", searchTextResp.getTotal());
        return searchTextResp;
    }

    /**
     * 根据标签列表生成过滤条件
     *
     * @param tags
     * @return
     */
    private String buildFilterStrForTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }
        String tagStr = String.join(" OR ", tags);
        return "tags:(" + tagStr + ")";
    }

    @Override
    public String createFaq(KnowledgeFaq knowledgeFaq) {
        String requestContent = JacksonUtils.toJson(new FaqReq(knowledgeFaq));
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(LakeSearchApi.CREATE_FAQ.getEndpoint(),
            requestContent, LakeSearchApi.CREATE_FAQ, null);
        FaqResp faqResp = convertResultModel(resultModel, FaqResp.class);
        if (Objects.isNull(faqResp)) {
            log.error("Fail to create faq in LakeSearch knowledge repo [{}]", knowledgeFaq.getKnowledgeRepoId());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return faqResp.getFaqId();
    }

    @Override
    public void deleteFaq(String repoId, String faqId) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.DELETE_FAQ.getEndpoint(), repoId, faqId), null, LakeSearchApi.DELETE_FAQ, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to delete faq in LakeSearch faqId [{}]", faqId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public Integer deleteFaqBatch(String repoId, List<String> faqIds) {
        FaqListReq req = FaqListReq.builder().repoId(repoId).faqIds(faqIds).build();
        String requestContent = JacksonUtils.toJson(req);
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(LakeSearchApi.BATCH_DELETE_FAQ.getEndpoint(),
            requestContent, LakeSearchApi.BATCH_DELETE_FAQ, null);
        FaqDeleteBatchResp faqDeleteBatchResp = convertResultModel(resultModel, FaqDeleteBatchResp.class);
        if (Objects.isNull(faqDeleteBatchResp)) {
            log.error("Fail to batch delete faqs in LakeSearch knowledge repo [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return faqDeleteBatchResp.getDeletedCount();
    }

    @Override
    public String modifyFaq(KnowledgeFaq knowledgeFaq) {
        String requestContent = JacksonUtils.toJson(new FaqReq(knowledgeFaq));
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(LakeSearchApi.MODIFY_FAQ.getEndpoint(),
            requestContent, LakeSearchApi.MODIFY_FAQ, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to modify faq in LakeSearch faqId [{}]", knowledgeFaq.getFaqId());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return knowledgeFaq.getFaqId();
    }

    @Override
    public KnowledgeFaq retrieveFaq(String faqId, String repoId) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.RETRIEVE_FAQ.getEndpoint(), faqId, repoId), null, LakeSearchApi.RETRIEVE_FAQ,
            null);
        FaqInfo faqInfo = convertResultModel(resultModel, FaqInfo.class);
        if (Objects.isNull(faqInfo)) {
            log.error("Fail to retrieve faq from LakeSearch faqId [{}]", faqId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return faqInfo.convertToDto();
    }

    @Override
    public ListFaqResp listFaq(FaqSearchCriteria faqSearchCriteria) {
        StringBuilder endpoint = new StringBuilder(
            String.format(LakeSearchApi.LIST_FAQS.getEndpoint(), faqSearchCriteria.getRepoId()));
        if (!Objects.isNull(faqSearchCriteria.getPageNum()) && !Objects.isNull(faqSearchCriteria.getPageSize())) {
            endpoint.append("?page_num=")
                .append(faqSearchCriteria.getPageNum())
                .append("&page_size=")
                .append(faqSearchCriteria.getPageSize())
                .append("&repo_id=")
                .append(faqSearchCriteria.getRepoId());
            if (!Objects.isNull(faqSearchCriteria.getQuestion())) {
                endpoint.append("&question=").append(urlEncode(faqSearchCriteria.getQuestion()));
            }
        }
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint.toString(), null,
            LakeSearchApi.LIST_FAQS, null);
        ListFaqResponse listFaqResponse = convertResultModel(resultModel, ListFaqResponse.class);
        if (Objects.isNull(listFaqResponse)) {
            log.error("Fail to list faqs from LakeSearch repoId [{}]", faqSearchCriteria.getRepoId());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        List<KnowledgeFaq> knowledgeFaqs = Optional.ofNullable(listFaqResponse.getRecords())
            .map(faqInfos -> faqInfos.stream().map(FaqInfo::convertToDto).toList())
            .orElse(Collections.emptyList());
        return new ListFaqResp().setFaqList(knowledgeFaqs).setCount(listFaqResponse.getTotal());
    }

    @Override
    public void startKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.START_KNOWLEDGE_REPO.getEndpoint(), repoId), null,
            LakeSearchApi.START_KNOWLEDGE_REPO, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to start knowledge repo in LakeSearch repoId [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public void stopKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.STOP_KNOWLEDGE_REPO.getEndpoint(), repoId), null,
            LakeSearchApi.STOP_KNOWLEDGE_REPO, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to stop knowledge repo in LakeSearch repoId [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public KnowledgeSegRuleInfo createSegmentRule(KnowledgeSegmentRule segmentRule, String repoId) {
        String connectionId = knowledgeConnectionRouterService.findConnectionIdByContext();
        String requestContent = JacksonUtils.toJson(SegmentRule.builder().regexs(segmentRule.getRuleRegexs()).build());
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(LakeSearchApi.CREATE_SEGMENT_RULE.getEndpoint(),
            requestContent, LakeSearchApi.CREATE_SEGMENT_RULE, null);
        SegmentRule rule = convertResultModel(resultModel, SegmentRule.class);
        if (Objects.isNull(rule)) {
            log.error("Fail to create segment rule from LakeSearch");
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        KnowledgeSegRuleInfo knowledgeSegRuleInfo = new KnowledgeSegRuleInfo();
        knowledgeSegRuleInfo.setRuleId(rule.getId());
        // 找到知识库connectionId
        knowledgeSegRuleInfo.setKnowledgeBaseConnectionId(connectionId);
        return knowledgeSegRuleInfo;
    }

    @Override
    public String modifySegmentRule(KnowledgeSegmentRule segmentRule) {
        String requestContent = JacksonUtils.toJson(SegmentRule.builder().regexs(segmentRule.getRuleRegexs()).build());
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.MODIFY_SEGMENT_RULE.getEndpoint(), segmentRule.getId()), requestContent,
            LakeSearchApi.MODIFY_SEGMENT_RULE, null);
        SegmentRule rule = convertResultModel(resultModel, SegmentRule.class);
        if (Objects.isNull(rule)) {
            log.error("Fail to modify segment rule from LakeSearch ruleId [{}]", segmentRule.getId());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return rule.getId();
    }

    @Override
    public void deleteSegmentRule(String ruleId) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.DELETE_SEGMENT_RULE.getEndpoint(), ruleId), null,
            LakeSearchApi.DELETE_SEGMENT_RULE, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to delete segment rule in LakeSearch ruleId [{}]", ruleId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public List<ModelInfo> listModels(ModelSearchCriteria searchCriteria, String connectionId) {
        StringBuilder endpoint = new StringBuilder(LakeSearchApi.LIST_MODELS.getEndpoint());
        endpoint.append("?page_num=")
            .append(searchCriteria.getPageNum())
            .append("&page_size=")
            .append(searchCriteria.getPageSize());
        if (!Objects.isNull(searchCriteria.getModelType())) {
            endpoint.append("&model_type=").append(urlEncode(searchCriteria.getModelType()));
        }
        if (!Objects.isNull(searchCriteria.getModelName())) {
            endpoint.append("&model_name=").append(urlEncode(searchCriteria.getModelName()));
        }
        if (!Objects.isNull(searchCriteria.getModelStatus())) {
            endpoint.append("&model_status=").append(urlEncode(searchCriteria.getModelStatus()));
        }
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint.toString(), null,
            LakeSearchApi.LIST_MODELS, null);
        ListModelResp resp = convertResultModel(resultModel, ListModelResp.class);
        if (Objects.isNull(resp)) {
            log.error("Fail to list models from LakeSearch");
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        if (CollectionUtils.isEmpty(resp.getModels())) {
            return Collections.emptyList();
        }
        return resp.getModels().stream().map(model -> new ModelInfo().setName(model.getName())
            .setType(model.getType())
            .setStatus(model.getStatus())).toList();
    }

    @Override
    public CreateKnowledgeTaskResponseBody createTask(String repoId, String taskType, List<String> fileIds) {
        String requestContent = JacksonUtils.toJson(
            CreateKnowledgeTaskRequestBody.builder().taskType(taskType).fileIds(fileIds).build());
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.CREATE_TASKS.getEndpoint(), repoId), requestContent, LakeSearchApi.CREATE_TASKS,
            null);
        BatchCreateResponse task = convertResultModel(resultModel, BatchCreateResponse.class);
        if (Objects.isNull(task)) {
            log.error("Fail to create task in LakeSearch, repoId [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return new CreateKnowledgeTaskResponseBody().setCreatedCount(task.getCreatedCount())
            .setTotalCount(task.getTotalCount());
    }

    @Override
    public ListKnowledgeTasksResponseBody listKnowledgeTask(String repoId, ListTaskCriteria listTaskCriteria) {
        StringBuilder endpoint = new StringBuilder(String.format(LakeSearchApi.LIST_TASKS.getEndpoint(), repoId));
        endpoint.append("?page_num=")
            .append(listTaskCriteria.getPageNum())
            .append("&page_size=")
            .append(listTaskCriteria.getPageSize());
        if (!Objects.isNull(listTaskCriteria.getTaskType())) {
            endpoint.append("&task_type=").append(urlEncode(listTaskCriteria.getTaskType()));
        }
        if (!Objects.isNull(listTaskCriteria.getTaskStatus())) {
            endpoint.append("&task_status=").append(urlEncode(listTaskCriteria.getTaskStatus()));
        }
        if (!Objects.isNull(listTaskCriteria.getFileName())) {
            endpoint.append("&file_name=").append(urlEncode(listTaskCriteria.getFileName()));
        }
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint.toString(), null,
            LakeSearchApi.LIST_TASKS, null);
        ListKnowledgeTasksResponseBody listKnowledgeTaskResp = convertResultModel(resultModel,
            ListKnowledgeTasksResponseBody.class);
        if (Objects.isNull(listKnowledgeTaskResp)) {
            log.error("Fail to list tasks from LakeSearch knowledge repo [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return listKnowledgeTaskResp;
    }

    @Override
    public CommonBatchDeleteRsp deleteKnowledgeTask(String repoId, List<String> taskIds) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.BATCH_DELETE_TASKS.getEndpoint(), repoId),
            JacksonUtils.toJson(new DeleteKnowledgeTaskReq().setTaskIds(taskIds)), LakeSearchApi.BATCH_DELETE_TASKS, null);
        CommonBatchDeleteRsp commonBatchDeleteRsp = convertResultModel(resultModel, CommonBatchDeleteRsp.class);
        if (Objects.isNull(commonBatchDeleteRsp)) {
            log.error("Fail to delete knowledge tasks in LakeSearch repo [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return commonBatchDeleteRsp;
    }

    @Override
    public String uploadFaqFile(KnowledgeRepo knowledgeRepo, MultipartFile resource) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.UPLOAD_FAQ_FILE.getEndpoint(), repoId), null, LakeSearchApi.UPLOAD_FAQ_FILE,
            resource);
        UploadFaqFileRsp uploadFaqFileRsp = convertResultModel(resultModel, UploadFaqFileRsp.class);
        if (Objects.isNull(uploadFaqFileRsp)) {
            log.error("Fail to upload faq file [{}] to LakeSearch knowledge repo [{}]", resource.getOriginalFilename(), repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return uploadFaqFileRsp.getFileId();
    }

    @Override
    public void deleteFaqFile(KnowledgeRepo knowledgeRepo, String fileId) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.DELETE_FAQ_FILE.getEndpoint(), repoId, fileId), null,
            LakeSearchApi.DELETE_FAQ_FILE, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to delete faq file [{}] in LakeSearch repo [{}]", fileId, repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public FaqFileInfoListRsp listFaqFiles(KnowledgeRepo knowledgeRepo, ListFaqFileReq listFaqFileReq) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        StringBuilder endpoint = new StringBuilder(String.format(LakeSearchApi.LIST_FAQ_FILES.getEndpoint(), repoId));
        endpoint.append("?page_num=")
            .append(listFaqFileReq.getPageNum())
            .append("&page_size=")
            .append(listFaqFileReq.getPageSize());
        if (!Objects.isNull(listFaqFileReq.getFileName())) {
            endpoint.append("&file_name=").append(urlEncode(listFaqFileReq.getFileName()));
        }
        if (!Objects.isNull(listFaqFileReq.getFileStatus())) {
            endpoint.append("&file_status=").append(urlEncode(listFaqFileReq.getFileStatus()));
        }
        if (!CollectionUtils.isEmpty(listFaqFileReq.getIds())) {
            endpoint.append("&ids=").append(urlEncode(String.join(",", listFaqFileReq.getIds())));
        }
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint.toString(), null,
            LakeSearchApi.LIST_FAQ_FILES, null);
        ListFilesRsp listFilesRsp = convertResultModel(resultModel, ListFilesRsp.class);
        if (Objects.isNull(listFilesRsp)) {
            log.error("Fail to list faq files from LakeSearch knowledge repo [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        List<FileInfo> fileInfos = new ArrayList<>();
        for (CssFileInfo cssFileInfo : listFilesRsp.getFiles()) {
            fileInfos.add(getFileInfoFromCssFileInfo(cssFileInfo));
        }
        return new FaqFileInfoListRsp().setFileInfoList(fileInfos).setCount(listFilesRsp.getTotal());
    }

    @Override
    public ResponseEntity<byte[]> downloadFaqFile(KnowledgeRepo knowledgeRepo, String fileId) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.DOWNLOAD_FAQ_FILE.getEndpoint(), fileId, knowledgeRepo.getKnowledgeRepoId()),
            null, LakeSearchApi.DOWNLOAD_FAQ_FILE, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to download faq file [{}] from LakeSearch", fileId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        return resultModel.getResponseEntity();
    }

    @Override
    public void createFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, FaqFileChunkReq faqFileChunkReq) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.CREATE_FAQ_FILE_CHUNK.getEndpoint(), fileId,
                knowledgeRepo.getKnowledgeRepoId()), JacksonUtils.toJson(faqFileChunkReq),
            LakeSearchApi.CREATE_FAQ_FILE_CHUNK, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to create faq file chunk in LakeSearch fileId [{}]", fileId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public void updateFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId,
        FaqFileChunkReq faqFileChunkReq) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.MODIFY_FAQ_FILE_CHUNK.getEndpoint(), fileId, chunkId,
                knowledgeRepo.getKnowledgeRepoId()), JacksonUtils.toJson(faqFileChunkReq),
            LakeSearchApi.MODIFY_FAQ_FILE_CHUNK, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to modify faq file chunk in LakeSearch fileId [{}] chunkId [{}]", fileId, chunkId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public void deleteFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId) {
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.BATCH_DELETE_FAQ_FILE_CHUNKS.getEndpoint(), fileId,
                knowledgeRepo.getKnowledgeRepoId()), JacksonUtils.toJson(Collections.singletonList(chunkId)),
            LakeSearchApi.BATCH_DELETE_FAQ_FILE_CHUNKS, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to delete faq file chunk in LakeSearch fileId [{}] chunkId [{}]", fileId, chunkId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
    }

    @Override
    public FaqFileChunkListRsp listFaqFileChunks(KnowledgeRepo knowledgeRepo,
        ListFaqFileChunksReq listFaqFileChunksReq) {
        String endpoint =
            String.format(LakeSearchApi.LIST_FAQ_FILE_CHUNKS.getEndpoint(), listFaqFileChunksReq.getFileId(),
                knowledgeRepo.getKnowledgeRepoId()) + "?page_num=" + listFaqFileChunksReq.getPageNum() + "&page_size="
                + listFaqFileChunksReq.getPageSize();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint, null,
            LakeSearchApi.LIST_FAQ_FILE_CHUNKS, null);
        ListFileDocsRsp listFileDocsRsp = convertResultModel(resultModel, ListFileDocsRsp.class);
        if (Objects.isNull(listFileDocsRsp)) {
            log.error("Fail to list faq file chunks from LakeSearch knowledge repo [{}]",
                knowledgeRepo.getKnowledgeRepoId());
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        FaqFileChunkListRsp faqFileChunkListRsp = new FaqFileChunkListRsp();
        faqFileChunkListRsp.setCount(listFileDocsRsp.getTotal());
        faqFileChunkListRsp.setFileChunkList(getFileChunkInfoFromFileDocInfo(listFileDocsRsp.getDocs()));
        return faqFileChunkListRsp;
    }

    @Override
    public Resource queryImage(KnowledgeRepo knowledgeRepo, String imageId) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(
            String.format(LakeSearchApi.QUERY_IMAGE_CONTENT.getEndpoint(), imageId, knowledgeRepo.getKnowledgeRepoId()),
            null, LakeSearchApi.QUERY_IMAGE_CONTENT, null);
        if (Objects.isNull(resultModel) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to query image of chunks in LakeSearch repoId [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        byte[] bytes = resultModel.getContent().getBytes();
        return new ResponseModel.TransferResource(new ByteArrayInputStream(bytes));
    }

    @Override
    public ListKnowledgeTagsResp listKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, int pageNum, int pageSize) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();
        String endpoint = String.format(LakeSearchApi.LIST_KNOWLEDGE_REPO_TAGS.getEndpoint(), repoId) + "?page_num="
            + INTERNAL_TAG_PAGE_NUM + "&page_size=" + INTERNAL_TAG_PAGE_SIZE;
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint, null,
            LakeSearchApi.LIST_KNOWLEDGE_REPO_TAGS, null);
        ListKnowledgeLabelsResp labelsResp = convertResultModel(resultModel, ListKnowledgeLabelsResp.class);
        if (Objects.isNull(labelsResp) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to list labels from LakeSearch knowledge repo [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }
        log.info("Success to list labels from knowledge repo, labels size: [{}]", labelsResp.getTotal());
        List<KnowledgeTagInfo> tagList = labelsResp.getLabelList().stream().map(labelInfo -> {
                KnowledgeTagInfo tagInfo = new KnowledgeTagInfo();
                tagInfo.setId(labelInfo.getId());
                tagInfo.setName(labelInfo.getName());
                tagInfo.setColor(labelInfo.getColor());
                tagInfo.setCreateTime(Long.parseLong(labelInfo.getCreateTime()));

                return tagInfo;
            })
            // 全局排序：按创建时间降序
            .sorted(Comparator.comparingLong(KnowledgeTagInfo::getCreateTime).reversed())
            // 内存分页：计算跳过的行数并截取
            // pageNum 通常从 1 开始，skip 需要 (pageNum - 1) * pageSize
            .skip((long) (pageNum - 1) * pageSize).limit(pageSize).collect(Collectors.toList());

        return new ListKnowledgeTagsResp().setCount(labelsResp.getTotal()).setTagList(tagList);
    }

    @Override
    public CreateKnowledgeRepoTagsRsp createKnowledgeRepoTags(KnowledgeRepo knowledgeRepo,
        CreateKnowledgeRepoTagsReq body) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();

        // 检查标签数量是否已达上限
        String listLabelsEndpoint = String.format(LakeSearchApi.LIST_KNOWLEDGE_REPO_TAGS.getEndpoint(), repoId)
            + "?page_num=" + INTERNAL_TAG_PAGE_NUM + "&page_size=" + INTERNAL_TAG_PAGE_SIZE;

        ResultModel listLabelsResult = agentBaseLakeSearchClient.sendAction(listLabelsEndpoint, null,
            LakeSearchApi.LIST_KNOWLEDGE_REPO_TAGS, null);

        ListKnowledgeLabelsResp listLabelsResp = convertResultModel(listLabelsResult, ListKnowledgeLabelsResp.class);

        if (Objects.isNull(listLabelsResp) || listLabelsResult.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to list labels from LakeSearch knowledge repo [{}]", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(listLabelsResult).getErrorCode()));
        }

        // 检查标签数量是否达到上限
        if (listLabelsResp.getTotal() >= MAX_TAG_NUM) {
            log.error("Fail to create knowledge repo labels, knowledgeRepoId: {}, error: {}.", repoId,
                "the number of labels exceeds the limit.");
            throw new AgentBaseException(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR);
        }
        // 检查标签是否已存在
        boolean isExist = listLabelsResp.getLabelList()
            .stream()
            .anyMatch(item -> Objects.equals(item.getName(), body.getName()));
        if (isExist) {
            throw new AgentBaseException(ErrorCode.TAG_NAME_ALREADY_EXIST);
        }

        String createLabelEndpoint = String.format(LakeSearchApi.CREATE_KNOWLEDGE_REPO_TAGS.getEndpoint(), repoId);

        ResultModel createLabelResult = agentBaseLakeSearchClient.sendAction(createLabelEndpoint,
            JacksonUtils.toJson(body), LakeSearchApi.CREATE_KNOWLEDGE_REPO_TAGS, null);

        CreateKnowledgeLabelsRsp createLabelsRsp = convertResultModel(createLabelResult,
            CreateKnowledgeLabelsRsp.class);

        if (Objects.isNull(createLabelsRsp) || createLabelResult.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to create knowledge repo label, knowledgeRepoId: {}", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(createLabelResult).getErrorCode()));
        }

        log.info("Success to create knowledge repo label, id: {}", createLabelsRsp.getLabelId());

        return new CreateKnowledgeRepoTagsRsp().setTagId(createLabelsRsp.getLabelId());
    }

    @Override
    public DeleteKnowledgeRepoTagsRsp deleteKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, String tagId,
        DeleteKnowledgeRepoTagsReq body) {
        String repoId = knowledgeRepo.getKnowledgeRepoId();

        String endpoint = String.format(LakeSearchApi.DELETE_KNOWLEDGE_REPO_TAGS.getEndpoint(), repoId, tagId);

        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint, JacksonUtils.toJson(body),
            LakeSearchApi.DELETE_KNOWLEDGE_REPO_TAGS, null);

        DeleteKnowledgeLabelsRsp deleteLabelsRsp = convertResultModel(resultModel, DeleteKnowledgeLabelsRsp.class);

        if (Objects.isNull(deleteLabelsRsp) || resultModel.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            log.error("Fail to delete knowledge repo label, knowledgeRepoId: {}", repoId);
            throw new AgentBaseException(
                errorCodeMappingService.getAgentBaseErrorCode(getErrorDetail(resultModel).getErrorCode()));
        }

        log.info("Success to delete knowledge repo label, id: {}", deleteLabelsRsp.getLabelId());

        return new DeleteKnowledgeRepoTagsRsp().setTagId(deleteLabelsRsp.getLabelId());
    }

    private ErrorDetail getErrorDetail(ResultModel resultModel) {
        if (Objects.isNull(resultModel) || Objects.isNull(resultModel.getErrorDetail())) {
            log.warn("Get error info from lakeSearch with kerberos is null.");
            return new ErrorDetail();
        }
        return new ErrorDetail().setErrorCode(resultModel.getErrorDetail().getErrorCode())
            .setErrorMsg(resultModel.getErrorDetail().getErrorMsg());
    }

    private String urlEncode(String param) {
        return StringUtils.isBlank(param) ? param : URLEncoder.encode(param, StandardCharsets.UTF_8);
    }

    private <T> T convertResultModel(ResultModel resultModel, Class<T> clazz) {
        if (Objects.isNull(resultModel)) {
            throw new AgentBaseException(ErrorCode.LAKE_SEARCH_SERVICE_EXCEPTION);
        }
        return JacksonUtils.readValue(resultModel.getContent(), clazz);
    }
}
