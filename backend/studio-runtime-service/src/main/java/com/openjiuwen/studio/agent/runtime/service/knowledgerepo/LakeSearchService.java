/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo;

import static com.openjiuwen.studio.agent.common.constant.Constants.CustomModel.CUSTOM_TOKEN;
import static com.openjiuwen.studio.agent.common.constant.Constants.CustomModel.CUSTOM_USER_ID;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.Common.AUTHORIZATION;

import com.openjiuwen.studio.agent.common.dto.knowledge.SearchTextReq;
import com.openjiuwen.studio.agent.common.enums.KnowledgeSourceEnum;
import com.openjiuwen.studio.agent.common.enums.RagAuthMode;
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
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.LakeSearchConnection;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider.KooSearchConnectionProvider;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider.LakeSearchConnectionProvider;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * LakeSearch知识库Service
 *
 * @since 2025-01-02
 */
@Slf4j
@Service("MgLakeSearch")
@RequiredArgsConstructor
public class LakeSearchService implements KnowledgeRepoService {

    private final CssUniSearchClient cssUniSearchClient;

    private final LakeSearchConnectionProvider lakeSearchConnectionProvider;

    private final KooSearchConnectionProvider kooSearchConnectionProvider;

    @Value("${knowledge.source}")
    private String knowledgeSource;

    @Value("${knowledge.lakeSearch.auth-mode}")
    private String authMode;

    @Value("${knowledge.lakeSearch.endpoint}")
    private String lakeSearchEndpoint;

    @Value("${knowledge.lakeSearch.project-id}")
    private String lakeSearchProjectId;

    @Value("${knowledge.lakeSearch.application-id}")
    private String lakeSearchApplicationId;

    private static final String TOKEN = "token";

    private static final String USER_ID = "userid";

    private static final String X_USER_ID = "X-USER-ID";

    @Override
    public ConnectorTypeEnum type() {
        return ConnectorTypeEnum.LAKE_SEARCH_INSIDE;
    }

    @Override
    public SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode,
        List<String> tags, Float recallThreshold, Integer pageNum, Integer pageSize) {
        String knowledgeRepoId = knowledgeRepos.get(0).getKnowledgeRepoId();
        String knowledgeBaseId = knowledgeRepos.get(0).getId();
        SearchTextResp searchTextResp = new SearchTextResp();
        try {
            // 调用LakeSearch接口检索知识库文档
            searchTextResp = cssUniSearchClient.searchText(getUri(knowledgeBaseId), getHeaders(knowledgeBaseId),
                lakeSearchProjectId, lakeSearchApplicationId, SearchTextReq.builder()
                    .repoId(knowledgeRepoId)
                    .content(query)
                    .scope(searchMode)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .filterString(buildFilterStrForTags(tags))
                    .extraRepoIds(knowledgeRepos.stream()
                        .map(KnowledgeRepo::getKnowledgeRepoId)
                        .filter(item -> !item.equals(knowledgeRepoId))
                        .toList())
                    .build());
            log.info("Success to search knowledge repo: {}", knowledgeRepoId);
        } catch (Exception exception) {
            log.error("Fail to search text from knowledge repo, knowledgeRepoId: {}.", knowledgeRepoId, exception);
            throw new AgentStudioException(StudioError.KNOWLEDGE_SEARCH_EXCEPTION);
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
            // 调用LakeSearch接口下载知识库文档
            responseEntity = cssUniSearchClient.downloadFiles(getUri(knowledgeRepo.getId()),
                getHeaders(knowledgeRepo.getId()), lakeSearchProjectId, lakeSearchApplicationId,
                knowledgeRepo.getKnowledgeRepoId(), fileId);
            log.info("Success to download file: {}", fileId);
        } catch (Exception exception) {
            log.error("Fail to download file from knowledge repo, projectId: {}, fileId: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, exception);
            throw new AgentStudioException(StudioError.DOWNLOAD_FILE_FROM_LAKE_SEARCH_FAILED);
        }
        return responseEntity;
    }

    @Override
    public Resource queryImage(KnowledgeRepo knowledgeRepo, String imageId) {
        try {
            return cssUniSearchClient.queryImage(getUri(knowledgeRepo.getId()),
                getHeaders(knowledgeRepo.getId()), lakeSearchProjectId, lakeSearchApplicationId,
                knowledgeRepo.getKnowledgeRepoId(), null, imageId);
        } catch (Exception exception) {
            log.error("Fail to query image, projectId: {}, knowledgeRepoId: {}, imageId: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getKnowledgeRepoId(), imageId, exception);
            throw new AgentStudioException(StudioError.QUERY_IMAGE_FROM_LAKE_SEARCH_FAILED);
        }
    }

    @Override
    public FileInfoResponse listFiles(KnowledgeRepo knowledgeRepo, ListFileReq listFileReq) {
        ListFilesRsp listFilesRsp;
        try {
            // 调用LakeSearch接口查询知识库文档列表
            listFilesRsp = cssUniSearchClient.searchFiles(getUri(knowledgeRepo.getId()),
                getHeaders(knowledgeRepo.getId()), lakeSearchProjectId, lakeSearchApplicationId,
                knowledgeRepo.getKnowledgeRepoId(), listFileReq.getPageNum(), listFileReq.getPageSize(),
                listFileReq.getFileName(), listFileReq.getFileType(), listFileReq.getFileStatus());
            log.info("Success to list Files: {}", listFilesRsp.getTotal());
        } catch (Exception exception) {
            log.error("Fail to list file from knowledge repo", exception);
            throw new AgentStudioException(StudioError.LIST_FILES_FROM_LAKE_SEARCH_FAILED);
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
            // 调用LakeSearch接口查询知识库文档列表
            listFilesRsp = cssUniSearchClient.searchFaqFiles(getUri(knowledgeRepo.getId()),
                getHeaders(knowledgeRepo.getId()), lakeSearchProjectId, lakeSearchApplicationId,
                knowledgeRepo.getKnowledgeRepoId(), listFaqFileReq.getPageNum(), listFaqFileReq.getPageSize(),
                listFaqFileReq.getFileName(), listFaqFileReq.getFileStatus(), listFaqFileReq.getIds());
            log.info("Success to list FAQ Files: {}", listFilesRsp.getTotal());
        } catch (Exception exception) {
            log.error("Fail to list FAQ file from knowledge repo, projectId: {}, knowledgeRepoId: {}, fileName: {}.",
                RequestContextUtils.getRequestProjectId(), knowledgeRepo.getKnowledgeRepoId(),
                listFaqFileReq.getFileName(), exception);
            throw new AgentStudioException(StudioError.LIST_FAQ_FILES_FROM_LAKE_SEARCH_FAILED);
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
            // 调用LakeSearch接口下载知识库文档
            responseEntity = cssUniSearchClient.downloadFaqFiles(getUri(knowledgeRepo.getId()),
                getHeaders(knowledgeRepo.getId()), lakeSearchProjectId, lakeSearchApplicationId,
                knowledgeRepo.getKnowledgeRepoId(), fileId);
            log.info("Success to download FAQ file: {}", fileId);
        } catch (Exception exception) {
            log.error("Fail to download FAQ file from knowledge repo, projectId: {}, fileId: {}.",
                RequestContextUtils.getRequestProjectId(), fileId, exception);
            throw new AgentStudioException(StudioError.DOWNLOAD_FAQ_FILES_FROM_LAKE_SEARCH_FAILED);
        }
        return responseEntity;
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

    private URI getUri(String knowledgeBaseId) {
        String connectionId = kooSearchConnectionProvider.getConnectionIdByKnowledgeBaseId(knowledgeBaseId);
        LakeSearchConnection lakeSearchConnection;
        if (!KnowledgeSourceEnum.CUSTOM.toString().equalsIgnoreCase(knowledgeSource)) {
            lakeSearchConnection = lakeSearchConnectionProvider.getKnowledgeSourceConnection(connectionId);
            lakeSearchEndpoint = lakeSearchConnection.getEndpoint();
            authMode = lakeSearchConnection.getAuthMode();
        }
        // 获取lakeSearch的endpoint
        if (StringUtils.isBlank(lakeSearchEndpoint)) {
            throw new AgentStudioException(StudioError.INVALID_KNOWLEDGE_REPO_REQUEST);
        }
        return URI.create(lakeSearchEndpoint);
    }

    private HttpHeaders getHeaders(String knowledgeBaseId) {
        String connectionId = kooSearchConnectionProvider.getConnectionIdByKnowledgeBaseId(knowledgeBaseId);

        LakeSearchConnection lakeSearchConnection = new LakeSearchConnection();
        if (!KnowledgeSourceEnum.CUSTOM.toString().equalsIgnoreCase(knowledgeSource)) {
            lakeSearchConnection = lakeSearchConnectionProvider.getKnowledgeSourceConnection(connectionId);
            authMode = lakeSearchConnection.getAuthMode();
        }
        HttpHeaders headers = new HttpHeaders();
        String userId = RequestContextUtils.getRequestUserId();
        headers.add(X_USER_ID, userId);
        if (KnowledgeSourceEnum.CUSTOM.toString().equalsIgnoreCase(knowledgeSource)) {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            headers.add(USER_ID, request.getHeader(CUSTOM_USER_ID));
            headers.add(TOKEN, request.getHeader(CUSTOM_TOKEN));
        }
        log.info("Call lakeSearch request with userId:{}", userId);
        if (RagAuthMode.NONE.toString().equalsIgnoreCase(authMode)) {
            return headers;
        }
        // Basic认证，获取lakeSearch的authorization，密文
        String lakeSearchAuthorization = lakeSearchConnection.getLsBasicAuth().getLakeSearchAuthorization();
        if (StringUtils.isBlank(lakeSearchAuthorization)) {
            throw new AgentStudioException(StudioError.INVALID_KNOWLEDGE_REPO_REQUEST, "authorization");
        }
        // 解密
        try {
            String realAuth = CryptoUtils.decrypt(lakeSearchAuthorization);
            headers.add(AUTHORIZATION, realAuth);
        } catch (Exception exception) {
            log.error("Fail to decrypt authorization: [{}]", exception.getMessage(), exception);
            throw new AgentStudioException(StudioError.INVALID_KNOWLEDGE_REPO_REQUEST, "authorization");
        }
        return headers;
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
