/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.X_APIG_APPCODE;

import com.openjiuwen.studio.agent.common.dto.knowledge.KooSearchAuthMode;
import com.openjiuwen.studio.agent.common.dto.knowledge.SearchTextReq;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.CssFileInfo;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.FileInfo;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.FileInfoResponse;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.KnowledgeRepo;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFaqFileReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFileReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFilesRsp;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListStudioKnowledgeFaqFilesResponseBody;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.enums.ConnectorTypeEnum;
import com.openjiuwen.studio.agent.runtime.rce.client.CssUniSearchClient;
import com.openjiuwen.studio.agent.runtime.rce.model.SearchTextResp;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.KooSearchConnection;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider.KooSearchConnectionProvider;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * KooSearch 知识库功能接口
 *
 * @since 2024-04-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CssUniSearchService implements KnowledgeRepoService {

    private final CssUniSearchClient cssUniSearchClient;

    private final KooSearchConnectionProvider kooSearchConnectionProvider;

    @Override
    public ConnectorTypeEnum type() {
        return ConnectorTypeEnum.KOO_SEARCH_INSIDE;
    }

    @Override
    public SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode,
        List<String> tags, Float recallThreshold, Integer pageNum, Integer pageSize) {
        String knowledgeRepoId = knowledgeRepos.get(0).getKnowledgeRepoId();
        SearchTextResp searchTextResp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByKnowledgeBaseId(knowledgeRepos.get(0).getId());
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
            throw new AgentStudioException(StudioError.ERROR_REFRESH_ENVIRONMENT);
        }
        return searchTextResp;
    }

    @Override
    public List<KnowledgeBaseConnectionParam> EncryptParams(List<KnowledgeBaseConnectionParam> params) {
        List<KnowledgeBaseConnectionParam> paramList = new ArrayList<>();
        params.forEach(item -> {
            if ("app_code".equals(item.getCode())) {
                KnowledgeBaseConnectionParam knowledgeBaseConnectionParam = new KnowledgeBaseConnectionParam();
                knowledgeBaseConnectionParam.setCode(item.getCode());
                knowledgeBaseConnectionParam.setValue(CryptoUtils.encrypt(item.getValue()));
                paramList.add(knowledgeBaseConnectionParam);
            } else {
                paramList.add(item);
            }
        });
        return paramList;
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(KnowledgeRepo knowledgeRepo, String fileId) {
        ResponseEntity<byte[]> responseEntity;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByKnowledgeBaseId(
                knowledgeRepo.getId());
            // 调用KooSearch接口下载知识库文档
            responseEntity = cssUniSearchClient.downloadFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId);
            log.info("Success to download file: {}", fileId);
        } catch (Exception exception) {
            log.error("Fail to download file from knowledge repo, domainId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestUserDomainId(), fileId, exception.getMessage(), exception);
            throw new AgentStudioException(StudioError.DOWNLOAD_FILE_FROM_KOO_SEARCH_FAILED);
        }
        return responseEntity;
    }

    @Override
    public Resource queryImage(KnowledgeRepo knowledgeRepo, String imageId) {
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByKnowledgeBaseId(
                knowledgeRepo.getId());
            return cssUniSearchClient.queryImage(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), null,
                imageId);
        } catch (Exception exception) {
            log.error("Fail to query image, domainId: {}, knowledgeRepoId: {}, imageId: {}, error: {}.",
                RequestContextUtils.getRequestUserDomainId(), knowledgeRepo.getKnowledgeRepoId(), imageId,
                exception.getMessage(), exception);
            throw new AgentStudioException(StudioError.QUERY_IMAGE_FROM_KOO_SEARCH_FAILED);
        }
    }

    @Override
    public FileInfoResponse listFiles(KnowledgeRepo knowledgeRepo, ListFileReq listFileReq) {
        ListFilesRsp listFilesRsp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByKnowledgeBaseId(
                knowledgeRepo.getId());
            // 调用KooSearch接口查询知识库文档列表
            listFilesRsp = cssUniSearchClient.searchFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(),
                listFileReq.getPageNum(), listFileReq.getPageSize(), listFileReq.getFileName(),
                listFileReq.getFileType(), listFileReq.getFileStatus());
            log.info("Success to list Files: {}", listFilesRsp.getTotal());
        } catch (Exception exception) {
            log.error(
                "Fail to list file from knowledge repo, domainId: {}, knowledgeRepoId: {}, fileName: {}, error: {}.",
                RequestContextUtils.getRequestUserDomainId(), knowledgeRepo.getKnowledgeRepoId(),
                listFileReq.getFileName(), exception.getMessage(), exception);
            throw new AgentStudioException(StudioError.LIST_FILES_FROM_KOO_SEARCH_FAILED);
        }
        List<FileInfo> fileInfos = new ArrayList<>();
        for (CssFileInfo cssFileInfo : listFilesRsp.getFiles()) {
            fileInfos.add(getFileInfoFromCssFileInfo(cssFileInfo));
        }
        return new FileInfoResponse().setFileInfoList(fileInfos).setCount(listFilesRsp.getTotal());

    }

    @Override
    public ListStudioKnowledgeFaqFilesResponseBody listFaqFiles(KnowledgeRepo knowledgeRepo,
        ListFaqFileReq listFaqFileReq) {
        ListFilesRsp listFilesRsp;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByKnowledgeBaseId(
                knowledgeRepo.getId());
            // 调用KooSearch接口查询知识库FAQ文档列表
            listFilesRsp = cssUniSearchClient.searchFaqFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(),
                listFaqFileReq.getPageNum(), listFaqFileReq.getPageSize(), listFaqFileReq.getFileName(),
                listFaqFileReq.getFileStatus(), listFaqFileReq.getIds());
            log.info("Success to list FAQ Files: {}", listFilesRsp.getTotal());
        } catch (Exception exception) {
            log.error(
                "Fail to list FAQ file from knowledge repo, domainId: {}, knowledgeRepoId: {}, fileName: {}, error: {}.",
                RequestContextUtils.getRequestUserDomainId(), knowledgeRepo.getKnowledgeRepoId(),
                listFaqFileReq.getFileName(), exception.getMessage(), exception);
            throw new AgentStudioException(StudioError.LIST_FAQ_FILES_FROM_KOO_SEARCH_FAILED);
        }
        List<FileInfo> fileInfos = new ArrayList<>();
        for (CssFileInfo cssFileInfo : listFilesRsp.getFiles()) {
            fileInfos.add(getFileInfoFromCssFileInfo(cssFileInfo));
        }
        return new ListStudioKnowledgeFaqFilesResponseBody().setFileInfoList(fileInfos)
            .setCount(listFilesRsp.getTotal());
    }

    @Override
    public ResponseEntity<byte[]> downloadFaqFile(KnowledgeRepo knowledgeRepo, String fileId) {
        ResponseEntity<byte[]> responseEntity;
        try {
            String connectionId = kooSearchConnectionProvider.getConnectionIdByKnowledgeBaseId(
                knowledgeRepo.getId());
            // 调用KooSearch接口下载知识库文档
            responseEntity = cssUniSearchClient.downloadFaqFiles(getUri(connectionId), getHeaders(connectionId),
                getProjectId(connectionId), getApplicationId(connectionId), knowledgeRepo.getKnowledgeRepoId(), fileId);
            log.info("Success to download FAQ file: {}", fileId);
        } catch (Exception exception) {
            log.error("Fail to download FAQ file from knowledge repo, domainId: {}, fileId: {}, error: {}.",
                RequestContextUtils.getRequestUserDomainId(), fileId, exception.getMessage(), exception);
            throw new AgentStudioException(StudioError.DOWNLOAD_FAQ_FILES_FROM_KOO_SEARCH_FAILED);
        }
        return responseEntity;
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
            throw new AgentStudioException(StudioError.INVALID_KNOWLEDGE_REPO_REQUEST, "endpoint");
        }
        return URI.create(endpoint);
    }

    // 根据知识库type，决定选用的token
    private HttpHeaders getAuthHeader(String connectionId, KooSearchConnection kooSearchConnection) {
        String authMode = getKooSearchAuthMode(connectionId);
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.isEmpty(authMode)) {
            return headers;
        }
        if (KooSearchAuthMode.valueOf(authMode.toUpperCase(Locale.ENGLISH)) == KooSearchAuthMode.APP_CODE) {
            String appCode = kooSearchConnection.getAppCode();
            String decryptAppCode = CryptoUtils.decrypt(appCode);
            headers.add(X_APIG_APPCODE, decryptAppCode);
        } else {
            return headers;
        }
        return headers;
    }

    public String getKooSearchAuthMode(String connectionId) {
        KooSearchConnection kooSearchConnection = kooSearchConnectionProvider.getKnowledgeSourceConnection(
            connectionId);
        return kooSearchConnection.getAuthMode();
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

    /**
     * 根据标签列表生成过滤条件
     */
    private String buildFilterStrForTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }
        String tagStr = String.join(" OR ", tags);
        return "tags:(" + tagStr + ")";
    }

    private FileInfo getFileInfoFromCssFileInfo(CssFileInfo cssFileInfo) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(cssFileInfo.getId());
        fileInfo.setFileName(cssFileInfo.getName());
        fileInfo.setFileType(cssFileInfo.getType());
        fileInfo.setFileSize(cssFileInfo.getSize());
        fileInfo.setFileStatus(cssFileInfo.getStatus());
        fileInfo.setFileTags(cssFileInfo.getTags());
        fileInfo.setFailureReason(cssFileInfo.getUploadDesc());
        fileInfo.setCreateTime(Long.parseLong(cssFileInfo.getCreateTime()));
        fileInfo.setUpdateTime(Long.parseLong(cssFileInfo.getCreateTime()));
        return fileInfo;
    }
}
