/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.KnowledgeConstant.KNOWLEDGE_FILE_CACHE_PREFIX;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.KnowledgeConstant.KNOWLEDGE_FILE_TYPE_DOC;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.KnowledgeConstant.KNOWLEDGE_FILE_TYPE_FAQ;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.KnowledgeConstant.KNOWLEDGE_IMAGE_CACHE_PREFIX;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.KnowledgeConstant.KNOWLEDGE_MAX_PAGE_SIZE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.openjiuwen.studio.agent.common.dto.knowledge.ChunkImageFormat;
import com.openjiuwen.studio.agent.common.dto.knowledge.ChunkUtils;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeRepoStatus;
import com.openjiuwen.studio.agent.common.enums.KnowledgeSourceEnum;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.common.utils.UuidUtils;
import com.openjiuwen.studio.agent.runtime.dto.ConnectionParamInfo;
import com.openjiuwen.studio.agent.runtime.dto.CreateKnowledgeRouterResponse;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeBaseReq;
import com.openjiuwen.studio.agent.runtime.dto.CreateStudioKnowledgeResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteStudioKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.KnowledgeIdTags;
import com.openjiuwen.studio.agent.runtime.dto.RagResultReference;
import com.openjiuwen.studio.agent.runtime.dto.RetrievalKnowledgeBasesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.RetrievalKnowledgeBasesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.SyncKnowledgeReq;
import com.openjiuwen.studio.agent.runtime.dto.UpdateKnowledgeConnectionResponse;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.FileInfo;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.FileInfoResponse;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.KnowledgeRepo;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFaqFileReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFileReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListStudioKnowledgeFaqFilesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.TypeEnum;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseInfo;
import com.openjiuwen.studio.agent.runtime.enums.ConnectorTypeEnum;
import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.runtime.rce.model.SearchTextResp;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.KnowledgeBaseConnectionService;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.KnowledgeBaseService;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.KnowledgeRepoServiceFactory;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.multiretrivemerigingstrategy.RetrieveMergingStrategyContext;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeRagSearchService implements IKnowledgeRagSearchService {

    private final KnowledgeRepoServiceFactory knowledgeRepoServiceFactory;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper;

    private final RedisClient redisClient;

    private final RetrieveMergingStrategyContext retrieveMergingStrategyContext;

    private final KnowledgeBaseService knowledgeBaseService;

    private final KnowledgeBaseConnectionService knowledgeBaseConnectionService;

    private final ExecutorService knowledgeRetrieveExecutor = new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(), r -> {
        Thread thread = new Thread(r);
        thread.setName("knowledge-retrieve-execute-" + thread.getId());
        return thread;
    });

    @Value("${knowledge.bound.limit}")
    private int agentKnowledgeBoundLimit;

    @Value("${knowledge.source}")
    private String knowledgeSource;

    /**
     * 图片在 Redis 中的缓存过期时间（单位：天）
     */
    @Value("${knowledge.retrieval-image-validity-days:7}")
    private int retrievalValidityDays;

    @Override
    public RetrievalKnowledgeBasesResponseBody searchRagKnowledge(String projectId,
        RetrievalKnowledgeBasesRequestBody body) {
        log.info("Start rag search with param: body [{}]", JsonUtils.toJson(body));
        List<RagResultReference> faqResult;
        if (body.isNeedExtrasFaqSearch()) {
            body.setSearchMode(RetrievalKnowledgeBasesRequestBody.SearchModeEnum.FAQ);
            faqResult = searchKnowledgeRepo(body);
            if (!CollectionUtils.isEmpty(faqResult)) {
                return new RetrievalKnowledgeBasesResponseBody().setOutputList(faqResult);
            }
        }
        faqResult = searchKnowledgeRepo(body);
        return new RetrievalKnowledgeBasesResponseBody().setOutputList(faqResult);
    }

    private List<RagResultReference> searchKnowledgeRepo(RetrievalKnowledgeBasesRequestBody body) {
        List<String> knowledgeBaseIds = body.getKnowledgeBases()
            .stream()
            .map(KnowledgeIdTags::getId)
            .collect(Collectors.toList());
        if (knowledgeBaseIds.size() > agentKnowledgeBoundLimit) {
            log.error("knowledgeBase ids: {} exceed limit.", String.join(",", knowledgeBaseIds));
            throw new AgentStudioException(StudioError.KNOWLEDGE_REPO_TYPE_INCONSISTENT, knowledgeBaseIds.toString());
        }
        if (KnowledgeSourceEnum.CUSTOM.toString().equalsIgnoreCase(knowledgeSource)) {
            return retrieveCUSTOMKnowledge(body);
        }
        List<KnowledgeBaseInfo> knowledgeBaseInfos = knowledgeBaseMapper.batchQueryKnowledgeBases(knowledgeBaseIds);
        Map<String, KnowledgeBaseInfo> kbMap = knowledgeBaseInfos.stream()
            .collect(Collectors.toMap(KnowledgeBaseInfo::getId, Function.identity()));
        for (String knowledgeBaseId : knowledgeBaseIds) {
            if (ObjectUtils.isNotEmpty(kbMap.get(knowledgeBaseId)) && StringUtils.equals(
                KnowledgeRepoStatus.CLOSE.toString(), kbMap.get(knowledgeBaseId).getStatus())) {
                // 移除被关闭的知识库id
                knowledgeBaseInfos.removeIf(knowledgeBaseEntity -> knowledgeBaseEntity.getId().equals(knowledgeBaseId));
            }
        }
        Set<String> connectorIdSet = knowledgeBaseInfos.stream()
            .map(KnowledgeBaseInfo::getConnectorId)
            .collect(Collectors.toSet());
        // 如果是内部知识库，调用kooSearch接口检索，如果是外部知识库，调用第三方的方法检索
        List<RagResultReference> response;
        Integer offset = 0;
        Integer limit = body.getTopK() == null ? 10 : body.getTopK();
        // 当前支持HC场景下KooSearch类知识库多路检索和召回，其他场景暂不支持
        if (supportMultiSourceRouting(connectorIdSet)) {
            response = multiRetrieveKooSearchKnowledge(offset, limit, body, knowledgeBaseInfos);
        } else {
            // 其他内部场景均只支持单知识库查询
            response = retrieveInnerKbOnce(offset, limit, body, knowledgeBaseInfos);
        }
        return response.stream().filter(info -> info.getScore() >= body.getRecallThreshold()).map(item -> {
            RagResultReference ragResultReference = new RagResultReference();
            BeanUtils.copyProperties(item, ragResultReference);
            if (RagResultReference.TypeEnum.FAQ.equals(
                RagResultReference.TypeEnum.fromValue(body.getSearchMode().toString()))) {
                ragResultReference.setType(RagResultReference.TypeEnum.FAQ);
            } else {
                ragResultReference.setType(RagResultReference.TypeEnum.DOC);
            }
            return ragResultReference;
        }).collect(Collectors.toList());
    }

    private List<RagResultReference> retrieveInnerKbOnce(Integer offset, Integer limit,
        RetrievalKnowledgeBasesRequestBody body, List<KnowledgeBaseInfo> knowledgeBaseInfos) {

        List<KnowledgeRepo> repos = knowledgeBaseInfos.stream()
            .map(item -> new KnowledgeRepo().setKnowledgeRepoId(item.getExternalId())
                .setId(item.getId())
                .setType(Objects.requireNonNull(ConnectorTypeEnum.fromValue(item.getConnectorId())).toTypeEnum()))
            .toList();
        int pageNum = offset / limit + 1;
        List<String> tags = new ArrayList<>();
        body.getKnowledgeBases().forEach(item -> {
            if (!CollectionUtils.isEmpty(item.getTags())) {
                tags.addAll(item.getTags());
            }
        });
        String searchMode = Optional.ofNullable(body.getSearchMode())
            .orElse(RetrievalKnowledgeBasesRequestBody.SearchModeEnum.DOC)
            .toString();
        SearchTextResp searchTextResp = knowledgeRepoServiceFactory.chooseKnowledgeService(
                ConnectorTypeEnum.fromValue(knowledgeBaseInfos.get(0).getConnectorId()))
            .searchText(repos, body.getQuery(), searchMode, tags, body.getRecallThreshold(), pageNum, limit);
        Map<String, String> externalMap = knowledgeBaseInfos.stream()
            .collect(Collectors.toMap(KnowledgeBaseInfo::getExternalId, KnowledgeBaseInfo::getId));
        Map<String, String> localFileIdCache = new HashMap<>();
        AtomicLong number = new AtomicLong(1);
        String uuid = UuidUtils.getUUID();
        return searchTextResp.getDocList().stream().map(item -> {
            String knowledgeBaseId = externalMap.get(item.getRepoId());
            String tempFileId = cacheFileId(knowledgeBaseId, item.getFileId(), item.getDocType(), localFileIdCache);
            return new RagResultReference().setFileId(tempFileId)
                .setContent(
                    cacheImageAndReplaceImageFormat(body.isRetrieveImage(), knowledgeBaseId, item.getContent()))
                .setKnowledgeBaseId(knowledgeBaseId)
                .setKnowledgeBaseType(repos.get(0).getType().toString())
                .setDocumentName(item.getTitle())
                .setScore(item.getScore())
                .setSubtitle(item.getSubtitle())
                .setSerialNumber(number.getAndIncrement())
                .setRetrievalId(uuid);
        }).toList();
    }

    /**
     * 控制图片缓存时间并格式化图片标签
     *
     * @param content
     */
    private String cacheImageAndReplaceImageFormat(boolean retrieveImage, String knowledgeBaseId, String content) {
        ChunkImageFormat retrievalChunkFormat = ChunkUtils.replaceImageAccessKeyAndFormat(content, retrieveImage);
        // 使用缓存图片索引
        if (MapUtils.isNotEmpty(retrievalChunkFormat.getImageAccessMaps())) {
            retrievalChunkFormat.getImageAccessMaps()
                .forEach(
                    (key, value) -> redisClient.set(KNOWLEDGE_IMAGE_CACHE_PREFIX + value, knowledgeBaseId + "," + key,
                        Duration.ofDays(retrievalValidityDays)));
        }
        return retrievalChunkFormat.getChunkResult();
    }

    // 生成虚拟fileId,并缓存起来。
    private String cacheFileId(String knowledgeBaseId, String fileId, String type,
        Map<String, String> localFileIdCache) {
        if (StringUtils.isEmpty(fileId)) {
            return null;
        }
        String fileType = KNOWLEDGE_FILE_TYPE_FAQ.equals(type) ? KNOWLEDGE_FILE_TYPE_FAQ : KNOWLEDGE_FILE_TYPE_DOC;
        // 局部去重,防止同一请求同一个文件生成多个uuid
        String cacheKey = knowledgeBaseId + ":" + fileId;
        return localFileIdCache.computeIfAbsent(cacheKey, k -> {
            String id = UUID.randomUUID().toString();
            redisClient.set(KNOWLEDGE_FILE_CACHE_PREFIX + id, knowledgeBaseId + "," + fileType + "," + fileId,
                Duration.ofDays(retrievalValidityDays));
            return id;
        });
    }

    private List<RagResultReference> multiRetrieveKooSearchKnowledge(Integer offset, Integer limit,
        RetrievalKnowledgeBasesRequestBody body, List<KnowledgeBaseInfo> knowledgeBaseInfos) {
        if (knowledgeBaseInfos.size() == 1) {
            return retrieveInnerKbOnce(offset, limit, body, knowledgeBaseInfos);
        }
        Map<String, List<KnowledgeBaseInfo>> kbGroupingByConnectionIdMap = knowledgeBaseInfos.stream()
            .collect(groupingBy(entity -> {
                String kbConnectionId = entity.getKnowledgeBaseConnectionId(); // 可能为 null
                return kbConnectionId != null ? kbConnectionId : "null_connection_id";
            }));
        log.info("start to retrieve from multi knowledge connections");
        // 为每个 entry 创建 CompletableFuture
        List<CompletableFuture<List<RagResultReference>>> futures = kbGroupingByConnectionIdMap.entrySet()
            .stream()
            .map(entry -> {
                CompletableFuture<List<RagResultReference>> futureRes = asyncRetrieveInnerKbOnce(offset, limit, body,
                    entry.getValue());
                return futureRes.handle((result, ex) -> {
                    if (ex != null) {
                        log.error("async retrieve knowledge failed: connectionId is {}, error is {}", entry.getKey(),
                            ex.getMessage());
                        return new ArrayList<RagResultReference>();
                    }
                    return result;
                });
            })
            .toList();

        // 提取结果集

        List<List<RagResultReference>> retrieveKnowledgeBaseResponseList = futures.stream()
            .map(CompletableFuture::join)
            .toList();
        // 融合重排
        return retrieveMergingStrategyContext.getRetrieveMergingStrategyFromContext()
            .mergingRetrievedResult(retrieveKnowledgeBaseResponseList, limit);
    }

    private CompletableFuture<List<RagResultReference>> asyncRetrieveInnerKbOnce(Integer offset, Integer limit,
        RetrievalKnowledgeBasesRequestBody body, List<KnowledgeBaseInfo> knowledgeBaseInfos) {
        return CompletableFuture.supplyAsync(() -> retrieveInnerKbOnce(offset, limit, body, knowledgeBaseInfos),
            knowledgeRetrieveExecutor);
    }

    private boolean supportMultiSourceRouting(Set<String> connectorIdSet) {
        log.info("connectorIdSet size is {}", connectorIdSet.size());
        if (connectorIdSet.size() == 1 && ConnectorTypeEnum.KOO_SEARCH_INSIDE.getValue()
            .equals(connectorIdSet.iterator().next())) {
            log.info("connectorId is {}", connectorIdSet.iterator().next());
            return true;
        }
        return false;
    }

    private List<RagResultReference> retrieveCUSTOMKnowledge(RetrievalKnowledgeBasesRequestBody body) {
        List<KnowledgeRepo> repos = body.getKnowledgeBases()
            .stream()
            .map(item -> new KnowledgeRepo().setId(item.getId()).setType(TypeEnum.INTERNAL))
            .toList();
        int pageNum = 1;
        List<String> tags = new ArrayList<>();
        body.getKnowledgeBases()
            .stream()
            .filter(item -> !CollectionUtils.isEmpty(item.getTags()))
            .forEach(item -> tags.addAll(item.getTags()));
        SearchTextResp searchTextResp = knowledgeRepoServiceFactory.chooseKnowledgeService(
                ConnectorTypeEnum.LAKE_SEARCH)
            .searchText(repos, body.getQuery(), body.getSearchMode().toString(), tags, body.getRecallThreshold(),
                pageNum, body.getTopK());
        AtomicLong number = new AtomicLong(1);
        String uuid = UuidUtils.getUUID();

        return searchTextResp.getDocList()
            .stream()
            .map(item -> new RagResultReference().setKnowledgeBaseId(item.getRepoId())
                .setFileId(item.getFileId())
                .setContent(item.getContent())
                .setDocumentName(item.getTitle())
                .setScore(item.getScore())
                .setSubtitle(item.getSubtitle())
                .setSerialNumber(number.getAndIncrement())
                .setRetrievalId(uuid)
                .setType(RagResultReference.TypeEnum.FAQ.toString().equals(item.getDocType())
                    ? RagResultReference.TypeEnum.FAQ
                    : RagResultReference.TypeEnum.DOC))
            .toList();
    }

    @Override
    public CreateStudioKnowledgeResponseBody createStudioKnowledgeBase(List<CreateStudioKnowledgeBaseReq> body) {
        List<KnowledgeBaseEntity> knowledgeBaseEntities = new ArrayList<>();
        body.forEach(item -> {
            KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
            knowledgeBaseEntity.setId(item.getKnowledgeBaseId());
            knowledgeBaseEntity.setKnowledgeBaseConnectionId(item.getKnowledgeBaseConnectionId());
            knowledgeBaseEntity.setExternalId(item.getExternalId());
            knowledgeBaseEntity.setStatus(item.getStatus().toString());
            knowledgeBaseEntities.add(knowledgeBaseEntity);
        });
        knowledgeBaseMapper.insert(knowledgeBaseEntities);
        return new CreateStudioKnowledgeResponseBody().setKnowledgeBaseIds(
            knowledgeBaseEntities.stream().map(KnowledgeBaseEntity::getId).toList());
    }

    @Override
    public CreateKnowledgeRouterResponse createStudioKnowledgeBaseConnection(
        CreateStudioKnowledgeBaseConnectionRequestBody body) {
        if (ObjectUtils.isNotEmpty(knowledgeBaseConnectionMapper.selectById(body.getId()))) {
            return new CreateKnowledgeRouterResponse().setKnowledgeConnectionId(body.getConnectionId());
        }
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity = new KnowledgeBaseConnectionEntity();
        knowledgeBaseConnectionEntity.setId(body.getId());
        String connectionId = body.getConnectionId();
        knowledgeBaseConnectionEntity.setConnectorId(connectionId);
        List<ConnectionParamInfo> params = body.getParams();
        List<KnowledgeBaseConnectionParam> connectorParams = params.stream().map(item -> {
            KnowledgeBaseConnectionParam connectorParamDefinition = new KnowledgeBaseConnectionParam();
            connectorParamDefinition.setCode(item.getCode());
            connectorParamDefinition.setValue(item.getValue());
            return connectorParamDefinition;
        }).toList();
        TypeEnum typeEnum = Objects.requireNonNull(ConnectorTypeEnum.fromValue(connectionId)).toTypeEnum();
        if (TypeEnum.INTERNAL.equals(typeEnum)) {
            connectorParams = encryptParams(connectionId, connectorParams);
        }

        knowledgeBaseConnectionEntity.setParams(connectorParams);
        knowledgeBaseConnectionMapper.insert(knowledgeBaseConnectionEntity);
        return new CreateKnowledgeRouterResponse().setKnowledgeConnectionId(
            knowledgeBaseConnectionEntity.getConnectorId());
    }

    private List<KnowledgeBaseConnectionParam> encryptParams(String connectionId,
        List<KnowledgeBaseConnectionParam> params) {
        return knowledgeRepoServiceFactory.chooseKnowledgeService(ConnectorTypeEnum.fromValue(connectionId))
            .EncryptParams(params);
    }

    @Override
    public DeleteStudioKnowledgeBaseResponseBody deleteStudioKnowledgeBase(String knowledgeBaseId) {
        knowledgeBaseMapper.delete(knowledgeBaseId);
        return new DeleteStudioKnowledgeBaseResponseBody().setId(knowledgeBaseId);
    }

    @Override
    public Resource downloadStudioKnowledgeFaqFile(String knowledgeBaseId, String fileAccessKey) {
        String fileStr = redisClient.get(KNOWLEDGE_FILE_CACHE_PREFIX + fileAccessKey);
        if (StringUtils.isBlank(fileStr)) {
            log.error("file is not exist in current knowledgeBase {} or is expired.", knowledgeBaseId);
            throw new AgentStudioException(StudioError.FILE_NOT_EXIST_OR_EXPIRED);
        }
        String[] fileDetail = fileStr.split(",");
        String realKnowledgeBaseId = fileDetail[0];
        String fileId = fileDetail[2];
        if (!knowledgeBaseId.equals(realKnowledgeBaseId)) {
            log.error("file {} not exist in knowledgeBase {} ", fileAccessKey, knowledgeBaseId);
            throw new AgentStudioException(StudioError.FILE_NOT_EXIST_OR_EXPIRED);
        }
        final KnowledgeBaseInfo knowledgeBaseInfo = getKnowledgeBase(knowledgeBaseId);
        // 校验文件是否在知识库下
        FileInfo fileInfo = retrieveFaqFile(fileId, knowledgeBaseInfo);
        // 下载文件
        byte[] fileBytes = knowledgeRepoServiceFactory.chooseKnowledgeService(
                ConnectorTypeEnum.fromValue(knowledgeBaseInfo.getConnectorId()))
            .downloadFaqFile(new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseInfo.getExternalId()), fileId)
            .getBody();
        if (fileBytes == null) {
            log.error("File [{}] content is empty,can not download", fileId);
            throw new AgentStudioException(StudioError.FILE_CANNOT_BE_EMPTY);
        }
        // 处理响应
        ResponseModel.TransferResource resource = new ResponseModel.TransferResource(
            new ByteArrayInputStream(fileBytes));
        resource.setFilename(URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8));
        resource.setLength(fileInfo.getFileSize());
        return resource;
    }

    private List<FileInfo> getAllFaqFiles(KnowledgeBaseInfo knowledgeBaseInfo) {
        int pageNum = 1;
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseInfo.getExternalId());
        ListStudioKnowledgeFaqFilesResponseBody fileInfoListRsp = knowledgeRepoServiceFactory.chooseKnowledgeService(
                ConnectorTypeEnum.fromValue(knowledgeBaseInfo.getConnectorId()))
            .listFaqFiles(knowledgeRepo,
                new ListFaqFileReq().setFileName(null).setPageNum(pageNum).setPageSize(KNOWLEDGE_MAX_PAGE_SIZE));
        int total = fileInfoListRsp.getCount();
        if (total == 0) {
            return new ArrayList<>();
        }
        List<FileInfo> allFiles = new ArrayList<>(fileInfoListRsp.getFileInfoList());

        // 知识库文件总数量大于单次查询的上限，分多次循环查询
        if (total > KNOWLEDGE_MAX_PAGE_SIZE) {
            for (int i = 0; i < total / KNOWLEDGE_MAX_PAGE_SIZE; i++) {
                pageNum++;
                List<FileInfo> tempFileInfos = knowledgeRepoServiceFactory.chooseKnowledgeService(
                        ConnectorTypeEnum.fromValue(knowledgeBaseInfo.getConnectorId()))
                    .listFaqFiles(knowledgeRepo,
                        new ListFaqFileReq().setFileName(null).setPageNum(pageNum).setPageSize(KNOWLEDGE_MAX_PAGE_SIZE))
                    .getFileInfoList();
                if (org.apache.commons.collections4.CollectionUtils.isEmpty(tempFileInfos)) {
                    break;
                }
                allFiles.addAll(tempFileInfos);
            }
        }
        return allFiles.stream()
            .collect(
                collectingAndThen(toCollection(() -> new TreeSet<>(comparing(FileInfo::getFileId))), ArrayList::new));
    }

    private FileInfo retrieveFaqFile(String fileId, KnowledgeBaseInfo knowledgeBaseInfo) {
        // 调用KooSearch接口查询全量文件，再根据fileId过滤（KooSearch没有查询指定文件的接口）
        List<FileInfo> allFileInfos = getAllFaqFiles(knowledgeBaseInfo);
        final List<FileInfo> fileInfos = allFileInfos.stream().filter(file -> fileId.equals(file.getFileId())).toList();
        if (CollectionUtils.isEmpty(fileInfos)) {
            log.error("File [{}] is not exist", fileId);
            throw new AgentStudioException(StudioError.FILE_NOT_EXIST_OR_EXPIRED, fileId);
        }
        return fileInfos.get(0);
    }

    private KnowledgeBaseInfo getKnowledgeBase(String knowledgeBaseId) {
        final KnowledgeBaseInfo knowledgeBaseInfo = knowledgeBaseMapper.find(knowledgeBaseId);
        if (Objects.isNull(knowledgeBaseInfo)) {
            log.error("The knowledgeBase {} not exist.", knowledgeBaseId);
            throw new AgentStudioException(StudioError.KNOWLEDGE_BASE_NOT_EXIST, knowledgeBaseId);
        }
        return knowledgeBaseInfo;
    }

    @Override
    public Resource downloadStudioKnowledgeFile(String knowledgeBaseId, String fileAccessKey) {
        String fileStr = redisClient.get(KNOWLEDGE_FILE_CACHE_PREFIX + fileAccessKey);
        if (StringUtils.isBlank(fileStr)) {
            log.error("file is not exist in current knowledgeBase {} or is expired.", knowledgeBaseId);
            throw new AgentStudioException(StudioError.FILE_NOT_EXIST_OR_EXPIRED);
        }
        String[] fileDetail = fileStr.split(",");
        String realKnowledgeBaseId = fileDetail[0];
        String fileId = fileDetail[2];
        if (!knowledgeBaseId.equals(realKnowledgeBaseId)) {
            log.error("file {} not exist in knowledgeBase {} ", fileAccessKey, knowledgeBaseId);
            throw new AgentStudioException(StudioError.FILE_NOT_EXIST_OR_EXPIRED);
        }
        final KnowledgeBaseInfo knowledgeBaseInfo = getKnowledgeBase(knowledgeBaseId);
        FileInfo fileInfo = retrieveFile(fileId, knowledgeBaseInfo);
        byte[] fileBytes = knowledgeRepoServiceFactory.chooseKnowledgeService(
                ConnectorTypeEnum.fromValue(knowledgeBaseInfo.getConnectorId()))
            .downloadFile(new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseInfo.getExternalId()), fileId)
            .getBody();
        if (fileBytes == null) {
            log.error("The file content is empty,file id = {},can not download", fileId);
            throw new AgentStudioException(StudioError.EMPTY_FILE_CONTENT, fileId);
        }

        // 处理响应
        ResponseModel.TransferResource resource = new ResponseModel.TransferResource(
            new ByteArrayInputStream(fileBytes));
        resource.setFilename(URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8));
        resource.setLength(fileInfo.getFileSize());
        return resource;
    }

    public FileInfo retrieveFile(String fileId, KnowledgeBaseInfo knowledgeBaseInfo) {
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseInfo.getExternalId());
        List<FileInfo> fileInfos = getAllFiles(knowledgeBaseInfo);
        final List<FileInfo> fileInfoList = fileInfos.stream().filter(file -> fileId.equals(file.getFileId())).toList();
        if (CollectionUtils.isEmpty(fileInfoList)) {
            log.error("The file {} not exist in knowledgeBase {}.", fileId, knowledgeRepo.getKnowledgeRepoId());
            throw new AgentStudioException(StudioError.THE_FILE_NOT_BELONG_KNOWLEDGE_BASE);
        }
        return fileInfoList.get(0);
    }

    private List<FileInfo> getAllFiles(KnowledgeBaseInfo knowledgeBaseInfo) {
        int pageNum = 1;
        FileInfoResponse fileInfoListRsp = knowledgeRepoServiceFactory.chooseKnowledgeService(
                ConnectorTypeEnum.fromValue(knowledgeBaseInfo.getConnectorId()))
            .listFiles(new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseInfo.getExternalId()),
                new ListFileReq().setFileName(null).setPageNum(pageNum).setPageSize(KNOWLEDGE_MAX_PAGE_SIZE));
        int total = fileInfoListRsp.getCount();
        if (total == 0) {
            return new ArrayList<>();
        }
        List<FileInfo> allFiles = new ArrayList<>(fileInfoListRsp.getFileInfoList());

        // 知识库文件总数量大于单次查询的上限，分多次循环查询
        if (total > KNOWLEDGE_MAX_PAGE_SIZE) {
            for (int i = 0; i < total / KNOWLEDGE_MAX_PAGE_SIZE; i++) {
                pageNum++;
                List<FileInfo> tempFileInfos = knowledgeRepoServiceFactory.chooseKnowledgeService(
                        ConnectorTypeEnum.fromValue(knowledgeBaseInfo.getConnectorId()))
                    .listFiles(new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseInfo.getExternalId()),
                        new ListFileReq().setFileName(null).setPageNum(pageNum).setPageSize(KNOWLEDGE_MAX_PAGE_SIZE))
                    .getFileInfoList();
                if (CollectionUtils.isEmpty(tempFileInfos)) {
                    break;
                }
                allFiles.addAll(tempFileInfos);
            }
        }
        // 根据文件id进行去重
        return allFiles.stream()
            .collect(
                collectingAndThen(toCollection(() -> new TreeSet<>(comparing(FileInfo::getFileId))), ArrayList::new));
    }

    @Override
    public Resource downloadStudioKnowledgeImage(String imageAccessKey) {
        log.info("operation log  userId:{}, queryImageByAccessKey", RequestContextUtils.getRequestUserId());
        String imageStr = redisClient.get(KNOWLEDGE_IMAGE_CACHE_PREFIX + imageAccessKey);
        if (StringUtils.isBlank(imageStr)) {
            throw new AgentStudioException(StudioError.IMAGE_NOT_EXIST_OR_EXPIRED);
        }
        String[] imageDetail = imageStr.split(",");
        String knowledgeBaseId = imageDetail[0];
        String imageId = imageDetail[1];
        final KnowledgeBaseInfo knowledgeBaseInfo = getKnowledgeBase(knowledgeBaseId);
        KnowledgeRepo knowledgeRepo = new KnowledgeRepo().setKnowledgeRepoId(knowledgeBaseInfo.getExternalId());
        return knowledgeRepoServiceFactory.chooseKnowledgeService(
            ConnectorTypeEnum.fromValue(knowledgeBaseInfo.getConnectorId())).queryImage(knowledgeRepo, imageId);
    }

    @Override
    public Boolean syncteKnowledgeConnection(List<SyncKnowledgeReq> body) {
        if (CollectionUtils.isEmpty(body)) {
            log.warn("SyncKnowledgeReq list is empty, skip syncing");
            return true;
        }

        // 拆分入参为 KnowledgeBaseEntity 和 KnowledgeBaseConnectionEntity
        List<KnowledgeBaseEntity> knowledgeBaseEntities = new ArrayList<>();
        List<KnowledgeBaseConnectionEntity> connectionEntities = new ArrayList<>();

        for (SyncKnowledgeReq req : body) {
            // 构建 KnowledgeBaseConnectionEntity
            KnowledgeBaseConnectionEntity connectionEntity = new KnowledgeBaseConnectionEntity();
            connectionEntity.setId(req.getKnowledgeBaseConnectionId());
            connectionEntity.setConnectorId(req.getConnectorId());
            // 转换 ConnectionParamInfo 为 KnowledgeBaseConnectionParam
            if (req.getParams() != null) {
                List<KnowledgeBaseConnectionParam> params = req.getParams()
                    .stream()
                    .map(info -> KnowledgeBaseConnectionParam.builder()
                        .code(info.getCode())
                        .value(info.getValue())
                        .build())
                    .toList();
                if (TypeEnum.INTERNAL.equals(
                    Objects.requireNonNull(ConnectorTypeEnum.fromValue(req.getConnectorId())).toTypeEnum())) {
                    params = encryptParams(req.getConnectorId(), params);
                }
                connectionEntity.setParams(params);
            }
            connectionEntities.add(connectionEntity);

            // 构建 KnowledgeBaseEntity
            KnowledgeBaseEntity knowledgeBaseEntity = new KnowledgeBaseEntity();
            knowledgeBaseEntity.setId(req.getId());
            knowledgeBaseEntity.setExternalId(req.getExternalId());
            knowledgeBaseEntity.setKnowledgeBaseConnectionId(req.getKnowledgeBaseConnectionId());
            // 将 StatusEnum 转换为 String
            knowledgeBaseEntity.setStatus(
                req.getStatus() != null ? req.getStatus().toString() : SyncKnowledgeReq.StatusEnum.OPEN.toString());
            knowledgeBaseEntities.add(knowledgeBaseEntity);
        }

        boolean success = true;
        try {
            knowledgeBaseConnectionService.saveOrUpdateBatch(connectionEntities);
            knowledgeBaseService.saveOrUpdateBatch(knowledgeBaseEntities);
        } catch (Exception e) {
            log.error("Sync knowledge connection failed", e);
            success = false;
        }

        if (success) {
            log.info("Sync knowledge connection success, knowledgeBase count: {}, connection count: {}",
                knowledgeBaseEntities.size(), connectionEntities.size());
        }
        return success;
    }

    @Override
    public UpdateKnowledgeConnectionResponse updateStudioKnowledgeConnection(String connectionId,
        List<ConnectionParamInfo> body) {
        List<KnowledgeBaseConnectionParam> connectorParams = body.stream().map(item -> {
            KnowledgeBaseConnectionParam connectorParamDefinition = new KnowledgeBaseConnectionParam();
            connectorParamDefinition.setCode(item.getCode());
            connectorParamDefinition.setValue(item.getValue());
            return connectorParamDefinition;
        }).toList();
        UpdateWrapper<KnowledgeBaseConnectionEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", connectionId).set("params", JsonUtils.toJson(connectorParams));
        knowledgeBaseConnectionMapper.update(updateWrapper);
        return new UpdateKnowledgeConnectionResponse().setKnowledgeConnectionId(connectionId);
    }

}
