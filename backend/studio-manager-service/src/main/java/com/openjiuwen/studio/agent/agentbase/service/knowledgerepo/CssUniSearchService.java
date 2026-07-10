/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import static com.openjiuwen.studio.agent.agentbase.common.constant.KooSearchRepoType.TEMPLATE;

import com.openjiuwen.studio.agent.agentbase.client.CssUniSearchClient;
import com.openjiuwen.studio.agent.agentbase.common.constant.CommonConstant;
import com.openjiuwen.studio.agent.common.dto.knowledge.KooSearchAuthMode;
import com.openjiuwen.studio.agent.agentbase.model.BatchCreateResponse;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeLabelsRsp;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.CreateKnowledgeRepoReq;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.CreateKnowledgeTaskRequestBody;
import com.openjiuwen.studio.agent.agentbase.model.CssFileInfo;
import com.openjiuwen.studio.agent.agentbase.model.DeleteKnowledgeLabelsReq;
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
import com.openjiuwen.studio.agent.common.dto.knowledge.ListModelResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.RepoIndexConfig;
import com.openjiuwen.studio.agent.common.dto.knowledge.SearchTextReq;
import com.openjiuwen.studio.agent.agentbase.model.SearchTextResp;
import com.openjiuwen.studio.agent.agentbase.model.SegmentRule;
import com.openjiuwen.studio.agent.agentbase.model.UploadFilesResp;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.KooSearchConnection;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider.KooSearchConnectionProvider;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqResp;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesRequestBody;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteKnowledgeFilesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CommonBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeTaskReq;
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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * KooSearch 知识库功能接口
 *
 * @since 2024-04-19
 */
@Slf4j
@Service("MgKooSearch")
public class CssUniSearchService implements KnowledgeRepoService {

    private final CssUniSearchClient cssUniSearchClient;

    private final KooSearchConnectionProvider kooSearchConnectionProvider;

    private final KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    private static final String TEMPLATE_REPO_NAME_PREFIX = "AgentBuilderTempRepo";

    @Autowired
    @Qualifier("TemplateRepoMgmtExecutor")
    private Executor templateRepoMgmtExecutor;

    @Value("${knowledge.repo-share.template-capacity}")
    private Integer repoTemplateCapacity;

    /**
     * 构造器
     *
     * @param cssUniSearchClient cssUniSearchClient
     */
    public CssUniSearchService(CssUniSearchClient cssUniSearchClient,
        KooSearchConnectionProvider kooSearchConnectionProvider,
        KnowledgeConnectionRouterService knowledgeConnectionRouterService) {
        this.cssUniSearchClient = cssUniSearchClient;
        this.kooSearchConnectionProvider = kooSearchConnectionProvider;
        this.knowledgeConnectionRouterService = knowledgeConnectionRouterService;
    }

    @Override
    public CreateKnowledgeRepoInfo createKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        CreateKnowledgeRepoResp createKnowledgeRepoResp;
        CreateKnowledgeRepoInfo createKnowledgeRepoInfo = new CreateKnowledgeRepoInfo();
        String connectionId = null;
        CreateKnowledgeRepoReq createKnowledgeRepoReq = null;
        try {
            // 默认是Exclusive知识库
            connectionId = knowledgeConnectionRouterService.findConnectionIdByContext();
            createKnowledgeRepoReq = CreateKnowledgeRepoReq.builder()
                .name(knowledgeRepo.getDisplayName())
                .detail(knowledgeRepo.getDescription())
                .repoType(knowledgeRepo.getRepoType().toString())
                .embeddingModel(knowledgeRepo.getEmbeddingModel())
                .reRankModel(knowledgeRepo.getRerankModel())
                .fileExtract(FileExtract.builder()
                    .parseConf(knowledgeRepo.getParseConf())
                    .splitConf(knowledgeRepo.getSplitConf())
                    .build())
                .languageId("zh")
                .build();
            // 调用kooSearch接口，在OP账号下创建知识库
            createKnowledgeRepoResp = cssUniSearchClient.createKnowledgeRepo(getUri(connectionId),
                getHeaders(connectionId), getProjectId(connectionId), getApplicationId(connectionId),
                createKnowledgeRepoReq);
            createKnowledgeRepoInfo.setKnowledgeBaseId(createKnowledgeRepoResp.getRepoId());
            createKnowledgeRepoInfo.setKnowledgeBaseConnectionId(connectionId);
            log.info("Success to create KnowledgeRepo: {}", createKnowledgeRepoResp);
        } catch (Exception exception) {
            log.error("Fail to create knowledge repo, projectId: {}, repoName: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getDisplayName(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        return createKnowledgeRepoInfo;
    }

    public void createTemplateRepo(String connectionId, String embeddingModel, String rerankModel) {
        CreateKnowledgeRepoReq createKnowledgeRepoReq = new CreateKnowledgeRepoReq();
        createKnowledgeRepoReq.setName(TEMPLATE_REPO_NAME_PREFIX + "-" + System.currentTimeMillis());
        createKnowledgeRepoReq.setDetail(StringUtils.EMPTY);
        createKnowledgeRepoReq.setRepoType(TEMPLATE);
        // template知识库采用默认的parseConf和splitConf
        ParseConf parseConf = new ParseConf();
        parseConf.setImageEnabled(false);
        parseConf.setCatalogEnabled(false);
        parseConf.setOcrEnabled(false);
        parseConf.setHeaderFooterEnabled(false);
        SplitConf splitConf = new SplitConf();
        splitConf.setSplitMode(SplitConf.SplitModeEnum.AUTO);
        createKnowledgeRepoReq.setFileExtract(FileExtract.builder().parseConf(parseConf).splitConf(splitConf).build());
        createKnowledgeRepoReq.setEmbeddingModel(embeddingModel);
        createKnowledgeRepoReq.setReRankModel(rerankModel);
        RepoIndexConfig repoIndexConfig = new RepoIndexConfig();
        repoIndexConfig.setShareRepoLimit(repoTemplateCapacity);
        createKnowledgeRepoReq.setRepoIndexConfig(repoIndexConfig);
        createKnowledgeRepoReq.setLanguageId("zh");
        CreateKnowledgeRepoInfo createKnowledgeRepoInfo = new CreateKnowledgeRepoInfo();
        try {
            CreateKnowledgeRepoResp createKnowledgeRepoResp = cssUniSearchClient.createKnowledgeRepo(
                getUri(connectionId), getHeaders(connectionId), getProjectId(connectionId),
                getApplicationId(connectionId), createKnowledgeRepoReq);
            createKnowledgeRepoInfo.setKnowledgeBaseId(createKnowledgeRepoResp.getRepoId());
            createKnowledgeRepoInfo.setKnowledgeBaseConnectionId(connectionId);
        } catch (Exception exception) {
            log.error("Fail to create template knowledge repo, projectId: {}, repoName: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), createKnowledgeRepoReq.getName(), exception);
        }
        log.info("successfully create template repo, repo id is: {}, connection id is {}",
            createKnowledgeRepoInfo.getKnowledgeBaseId(), createKnowledgeRepoInfo.getKnowledgeBaseConnectionId());
    }

    @Override
    public String modifyKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        ModifyKnowledgeRepoRequestBody modifyKnowledgeRepoRequestBody = ModifyKnowledgeRepoRequestBody.builder()
            .name(knowledgeRepo.getDisplayName())
            .build();

        // 如果rerank为空值，查询rerank模型；如果rerank非空值，直接传到request body里面
        if (StringUtils.isEmpty(knowledgeRepo.getRerankModel())) {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
            KnowledgeRepoInfo knowledgeRepoInfo;
            knowledgeRepoInfo = cssUniSearchClient.showKnowledgeRepo(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId);
            modifyKnowledgeRepoRequestBody.setRerankModel(knowledgeRepoInfo.getRerankModel());
        } else {
            modifyKnowledgeRepoRequestBody.setRerankModel(knowledgeRepo.getRerankModel());
        }

        if (!Objects.isNull(knowledgeRepo.getSplitConf()) || !Objects.isNull(knowledgeRepo.getParseConf())) {
            FileExtract fileExtract = new FileExtract();
            Optional.ofNullable(knowledgeRepo.getSplitConf()).ifPresent(fileExtract::setSplitConf);
            Optional.ofNullable(knowledgeRepo.getParseConf()).ifPresent(fileExtract::setParseConf);
            modifyKnowledgeRepoRequestBody.setFileExtract(fileExtract);
        }
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用kooSearch接口，在OP账号下修改知识库配置
            ModifyKnowledgeRepoResponseBody modifyKnowledgeRepoRsp = cssUniSearchClient.modifyKnowledgeRepo(
                getUri(connectionId), getHeaders(connectionId), getProjectId(connectionId),
                getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), modifyKnowledgeRepoRequestBody);
            return modifyKnowledgeRepoRsp.getKnowledgeRepoId();
        } catch (Exception exception) {
            log.error("Fail to modify knowledge repo, projectId: {}, repoName: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getDisplayName(), exception.getMessage(),
                exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void deleteKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        DeleteKnowledgeRepoReq deleteKnowledgeRepoReq = DeleteKnowledgeRepoReq.builder()
            .repoIds(Collections.singletonList(knowledgeRepo.getKnowledgeRepoId()))
            .build();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用kooSearch接口，删除OP账号下的知识库
            DeleteKnowledgeResp deleteKnowledgeResp = cssUniSearchClient.deleteKnowledgeRepo(getUri(connectionId),
                getHeaders(connectionId), getProjectId(connectionId), getApplicationId(connectionId),
                deleteKnowledgeRepoReq);
            log.info("Success to delete KnowledgeRepo: {}", deleteKnowledgeResp);
        } catch (Exception exception) {
            log.error("Fail to delete knowledge repo, projectId: {}, knowledgeRepoId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getKnowledgeRepoId(), exception.getMessage(),
                exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public KnowledgeRepo retrieveKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        KnowledgeRepoInfo knowledgeRepoInfo;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            knowledgeRepoInfo = cssUniSearchClient.showKnowledgeRepo(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId);
            log.info("Success to retrieve KnowledgeRepo: {}",
                Optional.ofNullable(knowledgeRepoInfo).map(KnowledgeRepoInfo::getName).orElse(knowledgeRepoId));
        } catch (Exception exception) {
            log.error("Fail to retrieve knowledge repo, projectId: {}, knowledgeRepoId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepoId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        if (Objects.isNull(knowledgeRepoInfo)) {
            log.error("Knowledge repo is not exist in kooSearch: {}", knowledgeRepoId);
            throw new AgentBaseException(ErrorCode.RESOURCE_NOT_EXIST, knowledgeRepoId);
        }
        KnowledgeRepo realKnowledgeRepo = new KnowledgeRepo();
        realKnowledgeRepo.setEmbeddingModel(knowledgeRepoInfo.getEmbeddingModel());
        realKnowledgeRepo.setRerankModel(knowledgeRepoInfo.getRerankModel());
        realKnowledgeRepo.setDisplayName(knowledgeRepoInfo.getName());
        realKnowledgeRepo.setStatus(knowledgeRepoInfo.getStatus());
        realKnowledgeRepo.setKnowledgeRepoId(knowledgeRepoInfo.getId());
        realKnowledgeRepo.setParseConf(Optional.ofNullable(knowledgeRepoInfo.getFileExtract())
            .map(FileExtract::getParseConf)
            .orElse(new ParseConf()));
        realKnowledgeRepo.setSplitConf(Optional.ofNullable(knowledgeRepoInfo.getFileExtract())
            .map(FileExtract::getSplitConf)
            .orElse(new SplitConf()));
        return realKnowledgeRepo;
    }

    @Override
    public ListKnowledgeRepoResp listKnowledgeRepos(ListKnowledgeRepoReq req) {
        ListKnowledgeRepoResp knowledgeRepoInfos;
        try {
            String connectionId = knowledgeConnectionRouterService.findConnectionIdByContext();
            knowledgeRepoInfos = cssUniSearchClient.listKnowledgeRepos(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), req.getPageNum(), req.getPageSize(),
                req.getName(), null, null, null, null, null);
            log.info("Success to list KnowledgeRepos: {}", knowledgeRepoInfos.getTotal());
            return knowledgeRepoInfos;
        } catch (Exception exception) {
            log.error("Fail to list knowledge repos, projectId: {}, knowledgeRepoName: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), req.getName(), exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    public ListKnowledgeRepoResp listTemplateKnowledgeRepos(ListKnowledgeRepoReq req, String connectionId,
        String orderBy, String repoType, String embeddingModel, String rerankModel) {
        ListKnowledgeRepoResp knowledgeRepoInfos;
        try {
            knowledgeRepoInfos = cssUniSearchClient.listKnowledgeRepos(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), req.getPageNum(), req.getPageSize(),
                req.getName(), null, orderBy, repoType, embeddingModel, rerankModel);
            log.info("Success to list KnowledgeRepos: {}", knowledgeRepoInfos.getTotal());
            return knowledgeRepoInfos;
        } catch (Exception exception) {
            log.error("Fail to list template knowledge repos, projectId: {}, knowledgeRepoName: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), req.getName(), exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public String uploadFile(KnowledgeRepo knowledgeRepo, MultipartFile file, List<String> tags) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        UploadFilesResp uploadFilesResp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
            // 调用KooSearch接口上传文档
            uploadFilesResp = cssUniSearchClient.uploadFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId, file, tags);
            log.info("Success to upload File: {}", uploadFilesResp);
        } catch (Exception exception) {
            log.error("Fail to upload file to knowledge repo, projectId: {}, knowledgeRepoId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepoId, exception.getMessage(), exception);

            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        return uploadFilesResp.getFileId();
    }

    @Override
    public void deleteFile(KnowledgeRepo knowledgeRepo, String fileId) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
            // 调用KooSearch接口删除文档，只支持删除OP账号知识库下的文档
            cssUniSearchClient.deleteFiles(getUri(connectionId), getHeaders(connectionId), getProjectId(connectionId),
                getApplicationId(connectionId), knowledgeRepoId, fileId);
        } catch (Exception exception) {
            log.error(
                "Fail to delete file from knowledge repo, projectId: {}, knowledgeRepoId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepoId, fileId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public ListKnowledgeFilesResponseBody listFiles(KnowledgeRepo knowledgeRepo, ListFileReq listFileReq) {
        String type = knowledgeRepo.getType().toString();
        ListFilesRsp listFilesRsp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用KooSearch接口查询知识库文档列表
            listFilesRsp = cssUniSearchClient.searchFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(),
                listFileReq.getPageNum(), listFileReq.getPageSize(), listFileReq.getFileName(),
                listFileReq.getFileType(), listFileReq.getFileStatus());
            log.info("Success to list Files: {}", listFilesRsp.getTotal());
        } catch (Exception exception) {
            log.error(
                "Fail to list file from knowledge repo, projectId: {}, knowledgeRepoId: {}, fileName: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getKnowledgeRepoId(),
                listFileReq.getFileName(), exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        List<FileInfo> fileInfos = new ArrayList<>();
        for (CssFileInfo cssFileInfo : listFilesRsp.getFiles()) {
            fileInfos.add(getFileInfoFromCssFileInfo(cssFileInfo));
        }
        return new ListKnowledgeFilesResponseBody().setFileInfoList(fileInfos).setCount(listFilesRsp.getTotal());
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(KnowledgeRepo knowledgeRepo, String fileId) {
        String type = knowledgeRepo.getType().toString();
        ResponseEntity<byte[]> responseEntity;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用KooSearch接口下载知识库文档
            responseEntity = cssUniSearchClient.downloadFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId);
            log.info("Success to download file: {}", fileId);
        } catch (Exception exception) {
            log.error("Fail to download file from knowledge repo, projectId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        return responseEntity;
    }

    @Override
    public void createFileChunk(KnowledgeRepo knowledgeRepo, String fileId, FileChunkReq fileChunkReq) {
        String type = knowledgeRepo.getType().toString();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            cssUniSearchClient.createFileDocs(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId,
                fileChunkReq);
        } catch (Exception exception) {
            log.error("Fail to create file chunk from knowledge repo, projectId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void updateFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId, FileChunkReq fileChunkReq) {
        String type = knowledgeRepo.getType().toString();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            cssUniSearchClient.updateFileDocs(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId,
                chunkId, fileChunkReq);
        } catch (Exception exception) {
            log.error(
                "Fail to update file chunk from knowledge repo, projectId: {}, fileId: {}, chunkId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, chunkId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void updateFileMetaInfo(KnowledgeRepo knowledgeRepo, String fileId,
        UpdateFileMetaInfoReq updateFileMetaInfoReq) {
        String type = knowledgeRepo.getType().toString();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            cssUniSearchClient.updateFileInfo(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId,
                updateFileMetaInfoReq.getTags());
        } catch (Exception exception) {
            log.error("Fail to update file info from knowledge repo, projectId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void deleteFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId) {
        String type = knowledgeRepo.getType().toString();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            cssUniSearchClient.batchDeleteFileDocs(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId,
                Collections.singletonList(chunkId));
        } catch (Exception exception) {
            log.error(
                "Fail to delete file chunk from knowledge repo, projectId: {}, fileId: {}, chunkId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, chunkId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public FileChunkListRsp listFileChunks(KnowledgeRepo knowledgeRepo, ListFileChunksReq listFileChunksReq) {
        String type = knowledgeRepo.getType().toString();
        ListFileDocsRsp listFileDocsRsp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            listFileDocsRsp = cssUniSearchClient.listFileDocs(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(),
                listFileChunksReq.getFileId(), listFileChunksReq.getPageNum(), listFileChunksReq.getPageSize());
        } catch (Exception exception) {
            log.error("Fail to list file chunks from knowledge repo, projectId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), listFileChunksReq.getFileId(), exception.getMessage(),
                exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        FileChunkListRsp fileChunkListRsp = new FileChunkListRsp();
        fileChunkListRsp.setCount(listFileDocsRsp.getTotal());
        fileChunkListRsp.setFileChunkList(getFileChunkInfoFromFileDocInfo(listFileDocsRsp.getDocs()));
        return fileChunkListRsp;
    }

    @Override
    public SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode,
        List<String> tags, int pageNum, int pageSize) {
        String type = knowledgeRepos.get(0).getType().toString();
        String knowledgeRepoId = knowledgeRepos.get(0).getKnowledgeRepoId();
        SearchTextResp searchTextResp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
            // 调用KooSearch接口检索知识库文档
            searchTextResp = cssUniSearchClient.searchText(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), SearchTextReq.builder()
                    .repoId(knowledgeRepoId)
                    .content(query)
                    .scope(searchMode)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .filterString(buildFilterStrForTags(tags))
                    // agent-builder和koosearch的检索逻辑不一致，koosearch入参为：主repoId+extraRepoIds；agent-builder只有repoIds。
                    // 默认将第一个repo作为主repoId，并在extraRepoIds中将主repoId过滤掉。
                    .extraRepoIds(knowledgeRepos.stream()
                        .map(KnowledgeRepo::getKnowledgeRepoId)
                        .filter(item -> !item.equals(knowledgeRepoId))
                        .toList())
                    .build());
            log.info("Success to search knowledge repo: {}", knowledgeRepoId);
        } catch (Exception exception) {
            log.error("Fail to search text from knowledge repo, knowledgeRepoId: {}, error: {}.", knowledgeRepoId,
                exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
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
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeFaq.getKnowledgeRepoId());
            // 调用KooSearch接口创建FAQ
            FaqResp faqResp = cssUniSearchClient.createFaq(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), new FaqReq(knowledgeFaq));
            log.info("Success to create faq: {}", faqResp);
            return faqResp.getFaqId();
        } catch (Exception exception) {
            log.error("Fail to createFaq, repo: {}, error: {}.", knowledgeFaq.getKnowledgeRepoId(),
                exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void deleteFaq(String repoId, String faqId) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(repoId);
            // 调用KooSearch接口删除FAQ
            cssUniSearchClient.deleteFaq(getUri(connectionId), getHeaders(connectionId), getProjectId(connectionId),
                getApplicationId(connectionId), repoId, faqId);
            log.info("Success to delete faq: {}", faqId);
        } catch (Exception exception) {
            log.error("Fail to delete faq, repo: {}, error: {}.", repoId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public Integer deleteFaqBatch(String repoId, List<String> faqIds) {
        FaqListReq req = FaqListReq.builder().repoId(repoId).faqIds(faqIds).build();
        FaqDeleteBatchResp faqResp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(repoId);
            // 调用KooSearch接口批量删除FAQ
            faqResp = cssUniSearchClient.deleteFaqBatch(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), req);
            log.info("Success to batch delete faq: {}", faqResp.getDeletedCount());
            return faqResp.getDeletedCount();
        } catch (Exception exception) {
            log.error("Fail to batch delete faq, repo: {}, error: {}.", repoId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public String modifyFaq(KnowledgeFaq knowledgeFaq) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeFaq.getKnowledgeRepoId());
            // 调用KooSearch接口修改FAQ
            cssUniSearchClient.updateFaq(getUri(connectionId), getHeaders(connectionId), getProjectId(connectionId),
                getApplicationId(connectionId), new FaqReq(knowledgeFaq));
            log.info("Success to update faq: {}", knowledgeFaq.getFaqId());
            return knowledgeFaq.getFaqId();
        } catch (Exception exception) {
            log.error("Fail to update faq: {}, error: {}.", knowledgeFaq.getFaqId(), exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public KnowledgeFaq retrieveFaq(String faqId, String repoId) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(repoId);
            // 调用KooSearch接口查询FAQ详情
            FaqInfo faqInfo = cssUniSearchClient.showFaq(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), repoId, faqId);
            log.info("Success to retrieve faq: {}", faqId);
            return faqInfo.convertToDto();
        } catch (Exception exception) {
            log.error("Fail to retrieve faq: {}, error: {}.", faqId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public ListFaqResp listFaq(FaqSearchCriteria faqSearchCriteria) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(faqSearchCriteria.getRepoId());
            // 调用KooSearch接口查询FAQ列表
            ListFaqResponse listFaqResponse = cssUniSearchClient.listFaq(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), faqSearchCriteria.getRepoId(),
                faqSearchCriteria.getPageNum(), faqSearchCriteria.getPageSize(), faqSearchCriteria.getQuestion());
            log.info("Success to list faq: {}", listFaqResponse.getTotal());
            List<KnowledgeFaq> knowledgeFaqs = Optional.ofNullable(listFaqResponse.getRecords())
                .map(faqInfos -> faqInfos.stream().map(FaqInfo::convertToDto).toList())
                .orElse(Collections.emptyList());
            return new ListFaqResp().setFaqList(knowledgeFaqs).setCount(listFaqResponse.getTotal());
        } catch (Exception exception) {
            log.error("Fail to list faq, repo: {}, error: {}.", faqSearchCriteria.getRepoId(), exception.getMessage(),
                exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void startKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        String type = knowledgeRepo.getType().toString();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
            // 调用KooSearch接口开启知识库
            cssUniSearchClient.startKnowledgeRepo(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId);
            log.info("Success to start knowledge repo: {}", knowledgeRepoId);
        } catch (Exception exception) {
            log.error("Fail to start knowledge repo, knowledgeRepoId: {}, error: {}.", knowledgeRepoId,
                exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void stopKnowledgeRepo(KnowledgeRepo knowledgeRepo) {
        String type = knowledgeRepo.getType().toString();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
            // 调用KooSearch接口停用知识库
            cssUniSearchClient.stopKnowledgeRepo(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId);
            log.info("Success to stop knowledge repo: {}", knowledgeRepoId);
        } catch (Exception exception) {
            log.error("Fail to stop knowledge repo, knowledgeRepoId: {}, error: {}.", knowledgeRepoId,
                exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public KnowledgeSegRuleInfo createSegmentRule(KnowledgeSegmentRule segmentRule, String repoId) {
        SegmentRule rule = SegmentRule.builder().regexs(segmentRule.getRuleRegexs()).build();
        try {
            String connectionId = StringUtils.isBlank(repoId)
                ? knowledgeConnectionRouterService.findConnectionIdByContext()
                : kooSearchConnectionProvider.getConnectionIdByRepoId(repoId);
            // 调用KooSearch接口创建知识分层规则
            String ruleId = cssUniSearchClient.createSegmentRule(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), rule).getId();
            log.info("Success to create segment rule: {}", ruleId);
            KnowledgeSegRuleInfo knowledgeSegRuleInfo = new KnowledgeSegRuleInfo();
            knowledgeSegRuleInfo.setRuleId(ruleId);
            knowledgeSegRuleInfo.setKnowledgeBaseConnectionId(connectionId);
            return knowledgeSegRuleInfo;
        } catch (Exception exception) {
            log.error("Fail to create segment rule, error: {}.", exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public String modifySegmentRule(KnowledgeSegmentRule segmentRule) {
        SegmentRule rule = SegmentRule.builder().regexs(segmentRule.getRuleRegexs()).id(segmentRule.getId()).build();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdBySegmentId(rule.getId());
            // 调用KooSearch接口修改知识分层规则
            String ruleId = cssUniSearchClient.modifySegmentRule(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), segmentRule.getId(), rule).getId();
            log.info("Success to modify segment rule: {}", ruleId);
            return ruleId;
        } catch (Exception exception) {
            log.error("Fail to modify segment rule: {}, error: {}.", segmentRule.getId(), exception.getMessage(),
                exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void deleteSegmentRule(String ruleId) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdBySegmentId(ruleId);
            // 调用KooSearch接口删除知识分层规则
            cssUniSearchClient.deleteSegmentRule(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), ruleId);
            log.info("Success to delete segment rule: {}", ruleId);
        } catch (Exception exception) {
            log.error("Fail to delete segment rule: {}, error: {}.", ruleId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public List<ModelInfo> listModels(ModelSearchCriteria searchCriteria, String connectionId) {
        try {
            String realConnectionId = connectionId;
            String repoId = searchCriteria.getRepoId();
            if (StringUtils.isNotEmpty(searchCriteria.getRepoId())) {
                // 更新场景，需要列举当前知识库所在KooSearch实例的模型列表
                realConnectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(repoId);
            }
            // 调用KooSearch接口查询模型列表
            ListModelResp listModelResp = cssUniSearchClient.listModels(getUri(connectionId), getHeaders(connectionId),
                getProjectId(realConnectionId), getApplicationId(realConnectionId), searchCriteria.getModelName(),
                searchCriteria.getModelType(), searchCriteria.getModelStatus(), searchCriteria.getPageNum(),
                searchCriteria.getPageSize());
            log.info("Success to list models: {}", listModelResp.getTotal());
            List<ListModelResp.CssModelInfo> models = listModelResp.getModels();
            if (CollectionUtils.isEmpty(models)) {
                return Collections.emptyList();
            }
            return models.stream()
                .map(model -> new ModelInfo().setName(model.getName())
                    .setType(model.getType())
                    .setStatus(model.getStatus()))
                .toList();
        } catch (Exception exception) {
            log.error("Fail to list models, error: {}.", exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public CreateKnowledgeTaskResponseBody createTask(String repoId, String taskType, List<String> fileIds) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(repoId);
            // 调用KooSearch接口创建任务
            BatchCreateResponse task = cssUniSearchClient.createTask(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), repoId,
                CreateKnowledgeTaskRequestBody.builder().taskType(taskType).fileIds(fileIds).build());
            log.info("Success to create task, repo: {}", repoId);
            return new CreateKnowledgeTaskResponseBody().setCreatedCount(task.getCreatedCount())
                .setTotalCount(task.getTotalCount());
        } catch (Exception exception) {
            log.error("Fail to create task, repo: {}, error: {}", repoId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public ListKnowledgeTasksResponseBody listKnowledgeTask(String repoId, ListTaskCriteria listTaskCriteria) {
        ListKnowledgeTasksResponseBody listKnowledgeTaskResp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(repoId);
            // 调用KooSearch接口查询任务列表
            listKnowledgeTaskResp = cssUniSearchClient.listKnowledgeTask(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), repoId, listTaskCriteria.getPageNum(),
                listTaskCriteria.getPageSize(), listTaskCriteria.getTaskType(), listTaskCriteria.getTaskStatus(),
                listTaskCriteria.getFileName());
            log.info("Success to list tasks: {}", listKnowledgeTaskResp.getTotal());

        } catch (Exception exception) {
            log.error("Fail to list tasks!, error: {}", exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        return listKnowledgeTaskResp;
    }

    @Override
    public CommonBatchDeleteRsp deleteKnowledgeTask(String repoId, List<String> taskIds) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(repoId);
            // 调用KooSearch接口删除任务
            DeleteKnowledgeTaskReq deleteKnowledgeTaskReq = new DeleteKnowledgeTaskReq().setTaskIds(taskIds);
            BatchDeleteFaqResp resp = cssUniSearchClient.deleteKnowledgeTask(getUri(connectionId),
                getHeaders(connectionId), getProjectId(connectionId), getApplicationId(connectionId), repoId,
                deleteKnowledgeTaskReq);
            log.info("Success to delete task: {}", taskIds);
            return new CommonBatchDeleteRsp().setTotalCount(resp.getTotalCount())
                .setDeletedCount(resp.getDeletedCount());
        } catch (Exception exception) {
            log.error("Fail to delete tasks: {}, error: {}.", taskIds, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    // 根据知识库type，决定选用的token
    private HttpHeaders getAuthHeader(String connectionId, KooSearchConnection kooSearchConnection) {
        String authMode = kooSearchConnectionProvider.getKooSearchAuthMode(connectionId);
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.isEmpty(authMode)) {
            return headers;
        }
        if (KooSearchAuthMode.valueOf(authMode.toUpperCase(Locale.ENGLISH)) == KooSearchAuthMode.APP_CODE) {
            String appCode = kooSearchConnection.getAppCode();
            String decryptAppCode = CryptoUtils.decrypt(appCode);
            headers.add(CommonConstant.X_APIG_APPCODE, decryptAppCode);
        }
        return headers;
    }

    // 根据知识库type，决定选用的projectId
    private String getProjectId(String connectionId) {
        KooSearchConnection kooSearchConnection = kooSearchConnectionProvider.getKnowledgeSourceConnection(
            connectionId);
        return kooSearchConnection.getProjectId();
    }

    private String getApplicationId(String connectionId) {
        KooSearchConnection kooSearchConnection = kooSearchConnectionProvider.getKnowledgeSourceConnection(
            connectionId);
        return kooSearchConnection.getApplicationId();
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

    private HttpHeaders getHeaders(String connectionId) {
        KooSearchConnection kooSearchConnection = kooSearchConnectionProvider.getKnowledgeSourceConnection(
            connectionId);
        return getAuthHeader(connectionId, kooSearchConnection);
    }

    private URI getUri(String connectionId) {
        KooSearchConnection kooSearchConnection = kooSearchConnectionProvider.getKnowledgeSourceConnection(
            connectionId);
        String endpoint = kooSearchConnection.getEndpoint();
        if (StringUtils.isBlank(kooSearchConnection.getEndpoint())) {
            throw new AgentBaseException(ErrorCode.INVALID_KNOWLEDGE_REPO_REQUEST, "endpoint");
        }
        return URI.create(endpoint);
    }

    @Override
    public String uploadFaqFile(KnowledgeRepo knowledgeRepo, MultipartFile resource) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        UploadFaqFileRsp uploadFilesResp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
            // 调用KooSearch接口上传文档
            uploadFilesResp = cssUniSearchClient.uploadFaqFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId, resource);
            log.info("Success to upload FAQ File: {}", uploadFilesResp);
        } catch (Exception exception) {
            log.error("Fail to upload FAQ file to knowledge repo, projectId: {}, knowledgeRepoId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepoId);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        return uploadFilesResp.getFileId();
    }

    @Override
    public void deleteFaqFile(KnowledgeRepo knowledgeRepo, String fileId) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(knowledgeRepoId);
            // 调用KooSearch接口删除文档，只支持删除OP账号知识库下的文档
            cssUniSearchClient.deleteFaqFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId, fileId);
        } catch (Exception exception) {
            log.error(
                "Fail to delete FAQ file from knowledge repo, projectId: {}, knowledgeRepoId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepoId, fileId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public FaqFileInfoListRsp listFaqFiles(KnowledgeRepo knowledgeRepo, ListFaqFileReq listFaqFileReq) {
        String type = knowledgeRepo.getType().toString();
        ListFilesRsp listFilesRsp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用KooSearch接口查询知识库FAQ文档列表
            listFilesRsp = cssUniSearchClient.searchFaqFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(),
                listFaqFileReq.getPageNum(), listFaqFileReq.getPageSize(), listFaqFileReq.getFileName(),
                listFaqFileReq.getFileStatus(), listFaqFileReq.getIds());
            log.info("Success to list FAQ Files: {}", listFilesRsp.getTotal());
        } catch (Exception exception) {
            log.error(
                "Fail to list FAQ file from knowledge repo, projectId: {}, knowledgeRepoId: {}, fileName: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getKnowledgeRepoId(),
                listFaqFileReq.getFileName(), exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        List<FileInfo> fileInfos = new ArrayList<>();
        for (CssFileInfo cssFileInfo : listFilesRsp.getFiles()) {
            fileInfos.add(getFileInfoFromCssFileInfo(cssFileInfo));
        }
        return new FaqFileInfoListRsp().setFileInfoList(fileInfos).setCount(listFilesRsp.getTotal());
    }

    @Override
    public ResponseEntity<byte[]> downloadFaqFile(KnowledgeRepo knowledgeRepo, String fileId) {
        String type = knowledgeRepo.getType().toString();
        ResponseEntity<byte[]> responseEntity;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用KooSearch接口下载知识库文档
            responseEntity = cssUniSearchClient.downloadFaqFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId);
            log.info("Success to download FAQ file: {}", fileId);
        } catch (Exception exception) {
            log.error("Fail to download FAQ file from knowledge repo, projectId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        return responseEntity;
    }

    @Override
    public void createFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, FaqFileChunkReq faqFileChunkReq) {
        String type = knowledgeRepo.getType().toString();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            cssUniSearchClient.createFaqFileDocs(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId,
                faqFileChunkReq);
        } catch (Exception exception) {
            log.error("Fail to create FAQ file chunk from knowledge repo, projectId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void deleteFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId) {
        String type = knowledgeRepo.getType().toString();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            cssUniSearchClient.batchDeleteFaqFileDocs(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId,
                Collections.singletonList(chunkId));
        } catch (Exception exception) {
            log.error(
                "Fail to delete FAQ file chunk from knowledge repo, projectId: {}, fileId: {}, chunkId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, chunkId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public void updateFaqFileChunk(KnowledgeRepo knowledgeRepo, String fileId, String chunkId,
        FaqFileChunkReq faqFileChunkReq) {
        String type = knowledgeRepo.getType().toString();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            cssUniSearchClient.updateFaqFileDocs(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId,
                chunkId, faqFileChunkReq);
        } catch (Exception exception) {
            log.error(
                "Fail to update FAQ file chunk from knowledge repo, projectId: {}, fileId: {}, chunkId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, chunkId, exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public FaqFileChunkListRsp listFaqFileChunks(KnowledgeRepo knowledgeRepo,
        ListFaqFileChunksReq listFaqFileChunksReq) {
        String type = knowledgeRepo.getType().toString();
        ListFileDocsRsp listFileDocsRsp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            listFileDocsRsp = cssUniSearchClient.listFaqFileDocs(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(),
                listFaqFileChunksReq.getFileId(), listFaqFileChunksReq.getPageNum(),
                listFaqFileChunksReq.getPageSize());
        } catch (Exception exception) {
            log.error("Fail to list FAQ file chunks from knowledge repo, projectId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), listFaqFileChunksReq.getFileId(), exception.getMessage(),
                exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
        FaqFileChunkListRsp faqFileChunkListRsp = new FaqFileChunkListRsp();
        faqFileChunkListRsp.setCount(listFileDocsRsp.getTotal());
        faqFileChunkListRsp.setFileChunkList(getFileChunkInfoFromFileDocInfo(listFileDocsRsp.getDocs()));
        return faqFileChunkListRsp;
    }

    @Override
    public ListKnowledgeTagsResp listKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, int pageNum, int pageSize) {
        String type = knowledgeRepo.getType().toString();
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用 KooSearch 接口时传入大尺寸，获取全量标签
            ListKnowledgeLabelsResp listKnowledgeLabelsResp = cssUniSearchClient.listLabels(getUri(connectionId),
                getHeaders(connectionId), getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId,
                INTERNAL_TAG_PAGE_NUM, INTERNAL_TAG_PAGE_SIZE);

            log.info("Success to list knowledge repo labels: {}", listKnowledgeLabelsResp.getTotal());
            // 在流中处理：转换 -> 排序 -> 分页
            List<KnowledgeTagInfo> pagedTagList = listKnowledgeLabelsResp.getLabelList().stream().map(labelInfo -> {
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

            return new ListKnowledgeTagsResp().setCount(listKnowledgeLabelsResp.getTotal()).setTagList(pagedTagList);
        } catch (Exception exception) {
            log.error("Fail to list knowledge repo labels, knowledgeRepoId: {}, error: {}.", knowledgeRepoId,
                exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public CreateKnowledgeRepoTagsRsp createKnowledgeRepoTags(KnowledgeRepo knowledgeRepo,
        CreateKnowledgeRepoTagsReq body) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(knowledgeRepo.getKnowledgeRepoId());
        ListKnowledgeLabelsResp listKnowledgeLabelsResp = cssUniSearchClient.listLabels(getUri(connectionId),
            getHeaders(connectionId), getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId,
            INTERNAL_TAG_PAGE_NUM, INTERNAL_TAG_PAGE_SIZE);
        // 检查标签数量是否达到上限
        if (listKnowledgeLabelsResp.getTotal() >= MAX_TAG_NUM) {
            log.error("Fail to create knowledge repo labels, knowledgeRepoId: {}, error: {}.", knowledgeRepoId,
                "the number of labels exceeds the limit.");
            throw new AgentBaseException(ErrorCode.RESOURCE_CAPACITY_LIMIT_ERROR);
        }
        // 检查标签是否已存在
        boolean isExist = listKnowledgeLabelsResp.getLabelList()
            .stream()
            .anyMatch(item -> Objects.equals(item.getName(), body.getName()));
        if (isExist) {
            throw new AgentBaseException(ErrorCode.TAG_NAME_ALREADY_EXIST);
        }
        try {
            // 调用KooSearch接口创建知识库标签
            CreateKnowledgeLabelsRsp createKnowledgeLabelsRsp = cssUniSearchClient.createLabel(getUri(connectionId),
                getHeaders(connectionId), getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId,
                body);
            log.info("Success to create knowledge repo label, id: {}", createKnowledgeLabelsRsp.getLabelId());
            return new CreateKnowledgeRepoTagsRsp().setTagId(createKnowledgeLabelsRsp.getLabelId());
        } catch (Exception exception) {
            log.error("Fail to create knowledge repo labels, knowledgeRepoId: {}, error: {}.", knowledgeRepoId,
                exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public DeleteKnowledgeRepoTagsRsp deleteKnowledgeRepoTags(KnowledgeRepo knowledgeRepo, String tagId,
        DeleteKnowledgeRepoTagsReq body) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用KooSearch接口删除知识库标签
            DeleteKnowledgeLabelsReq deleteKnowledgeLabelsReq = DeleteKnowledgeLabelsReq.builder()
                .labelName(body.getTagName())
                .build();
            DeleteKnowledgeLabelsRsp deleteKnowledgeLabelsRsp = cssUniSearchClient.deleteLabel(getUri(connectionId),
                getHeaders(connectionId), getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId,
                tagId, deleteKnowledgeLabelsReq);
            log.info("Success to delete knowledge repo label, id: {}", deleteKnowledgeLabelsRsp.getLabelId());
            return new DeleteKnowledgeRepoTagsRsp().setTagId(deleteKnowledgeLabelsRsp.getLabelId());
        } catch (Exception exception) {
            log.error("Fail to delete repo labels, knowledgeRepoId: {}, error: {}.", knowledgeRepoId,
                exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    @Override
    public BatchDeleteKnowledgeFilesResponseBody batchDeleteFile(KnowledgeRepo knowledgeRepo,
        BatchDeleteKnowledgeFilesRequestBody deleteFileReq) {
        String knowledgeRepoId = knowledgeRepo.getKnowledgeRepoId();
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            // 调用KooSearch接口批量删除文档，只支持删除OP账号知识库下的文档
            return cssUniSearchClient.batchDeleteFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepoId, deleteFileReq);
        } catch (Exception exception) {
            log.error(
                "Fail to batch delete file from knowledge repo, projectId: {}, knowledgeRepoId: {}, fileIds: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepoId, deleteFileReq, exception.getMessage(),
                exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

    public Resource queryImage(KnowledgeRepo knowledgeRepo, String imageId) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByRepoId(
                knowledgeRepo.getKnowledgeRepoId());
            return cssUniSearchClient.queryImage(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), null,
                imageId);
        } catch (Exception exception) {
            log.error("Fail to query image, projectId: {}, knowledgeRepoId: {}, imageId: {}, error: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getKnowledgeRepoId(), imageId,
                exception.getMessage(), exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        }
    }

}
