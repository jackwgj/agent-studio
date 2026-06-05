/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import static com.openjiuwen.studio.agent.agentbase.common.constant.CommonConstant.CustomModel.EMBEDDING;
import static com.openjiuwen.studio.agent.agentbase.common.constant.CommonConstant.CustomModel.RERANK;
import static com.openjiuwen.studio.agent.agentbase.common.constant.KnowledgeLockConstant.KNOWLEDGE_TEMPLATE_MGMT_LOCK;
import static com.openjiuwen.studio.agent.agentbase.common.constant.KooSearchRepoType.TEMPLATE;
import static com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseServiceImpl.NORMAL_MODEL_STATUS;

import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeRepoInfo;
import com.openjiuwen.studio.agent.agentbase.model.ListKnowledgeRepoResp;
import com.openjiuwen.studio.agent.agentbase.model.ModelSearchCriteria;
import com.openjiuwen.studio.agent.agentbase.model.TemplateRepoInfo;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.CssUniSearchService;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeConnectionRouterService;
import com.openjiuwen.studio.agent.common.enums.KnowledgeSourceEnum;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeRepoReq;
import com.openjiuwen.studio.agent.manager.dto.ModelInfo;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 模板知识库管理类（目前仅KooSearch类知识库使用此类）
 */
@Slf4j
@Service
public class KbRepoTemplateMgmtService {

    private final RedisClient redisClient;

    public static final double KNOWLEDGE_TEMPLATE_USAGE_RATIO = 0.8;

    /**
     * knowledgebase容量排序项
     */
    public static final String KNOWLEDGE_TEMPLATE_NUM_ORDER = "share_repo_quota";

    @Value("${knowledge.source}")
    private String knowledgeSource;

    @Value("${knowledge.repo-share.enable}")
    private boolean logicKnowledgeRepoEnabled;

    @Value("${knowledge.repo-share.embedding-model-range:}")
    private List<String> embeddingModelRange;

    @Value("${knowledge.repo-share.rerank-model-range:}")
    private List<String> rerankModelRange;

    @Value("${knowledge.repo-share.check-interval:}")
    private int shareRepoCheckInterval;

    private final KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    private final CssUniSearchService cssUniSearchService;

    @Autowired
    public KbRepoTemplateMgmtService(RedisClient redisClient,
        KnowledgeConnectionRouterService knowledgeConnectionRouterService, CssUniSearchService cssUniSearchService) {
        this.redisClient = redisClient;
        this.knowledgeConnectionRouterService = knowledgeConnectionRouterService;
        this.cssUniSearchService = cssUniSearchService;
    }

    /**
     * 每20分钟执行一次template管理逻辑
     */
    @Scheduled(fixedRateString = "${knowledge.repo-share.check-interval:}", timeUnit = TimeUnit.MINUTES)
    void templateRepoManagementScheduleTask() {
        if (!KnowledgeSourceEnum.KOOSEARCH.toString().equalsIgnoreCase(knowledgeSource)) {
            log.info("current knowledge source is {}, do not have to trigger templateRepoManagementScheduleTask",
                knowledgeSource);
            return;
        }
        if (!logicKnowledgeRepoEnabled) {
            log.info("do not open the logic knowledge repo capability");
            return;
        }
        RedisLock lock = redisClient.getLock(KNOWLEDGE_TEMPLATE_MGMT_LOCK);
        try {
            // 尝试加锁，最多等待 0 秒，锁自动释放时间为 间隔的一半 分钟（防止死锁）
            log.info("check interval is {}", shareRepoCheckInterval);
            if (lock.tryLock(Duration.ofMinutes(shareRepoCheckInterval / 2))) {
                log.info("succeed to acquire redis lock: {}", KNOWLEDGE_TEMPLATE_MGMT_LOCK);
                // template模板管理
                templateRepoMgmt();
            } else {
                log.warn("failed to acquire redis lock: {}", KNOWLEDGE_TEMPLATE_MGMT_LOCK);
            }
        } catch (RuntimeException exception) {
            log.error("fail to manage template knowledge repo, reason is {}", exception);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, exception);
        } catch (Exception e) {
            log.error("fail to manage template knowledge repo reason is {}", e);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
    }

    private void templateRepoMgmt() {
        List<String> freeConnectionIds = knowledgeConnectionRouterService.findFreeConnectionIds();
        freeConnectionIds.forEach(connectionId -> {
            // 查询所有的embedding模型列表
            ModelSearchCriteria embeddingModelSearchCriteria = ModelSearchCriteria.builder()
                .modelType(EMBEDDING)
                .repoId(null)
                .modelStatus(NORMAL_MODEL_STATUS)
                .pageNum(1)
                .pageSize(100)
                .build();
            List<ModelInfo> embeddingModelInfos = cssUniSearchService.listModels(embeddingModelSearchCriteria,
                connectionId);
            log.info("successfully get embeddingModels from kooSearch connection is {}, total number is {}",
                connectionId, embeddingModelInfos.size());
            // 查询所有的rerank模型列表
            ModelSearchCriteria rerankModelSearchCriteria = ModelSearchCriteria.builder()
                .modelType(RERANK)
                .repoId(null)
                .modelStatus(NORMAL_MODEL_STATUS)
                .pageNum(1)
                .pageSize(100)
                .build();
            List<ModelInfo> rerankModelInfos = cssUniSearchService.listModels(rerankModelSearchCriteria, connectionId);
            log.info("successfully get rerankModels from kooSearch connection is {}, total number is {}", connectionId,
                rerankModelInfos.size());
            // 根据rerank模型类型和embedding模型种类得出需要有多少中模板知识库
            List<TemplateRepoInfo> templateRepoInfos = embeddingModelInfos.stream()
                .flatMap(s1 -> rerankModelInfos.stream().flatMap(s2 -> {
                    String embeddingModel = s1.getName();
                    String rerankModel = s2.getName();
                    if (CollectionUtils.isNotEmpty(embeddingModelRange)) {
                        if (!embeddingModelRange.contains(embeddingModel)) {
                            return Stream.empty();
                        }
                    }
                    if (CollectionUtils.isNotEmpty(rerankModelRange)) {
                        if (!rerankModelRange.contains(rerankModel)) {
                            return Stream.empty();
                        }
                    }
                    TemplateRepoInfo templateRepoInfo = new TemplateRepoInfo();
                    templateRepoInfo.setEmbeddingModel(embeddingModel);
                    templateRepoInfo.setRerankModel(rerankModel);
                    return Stream.of(templateRepoInfo);
                }))
                .toList();
            log.info("template repo type number is {}", templateRepoInfos.size());
            if (CollectionUtils.isNotEmpty((templateRepoInfos))) {
                // 检查并逐个更新模板知识库数量
                templateRepoInfos.forEach(v -> checkAndUpdateTemplateRepoPool(v, connectionId));
            }
            log.error("cannot get template repo type under this connectionId: {}", connectionId);
        });
    }

    private void checkAndUpdateTemplateRepoPool(TemplateRepoInfo templateRepoInfo, String connectionId) {
        int batchQuerySize = 1;
        int pageNum = 1;
        // 查询前1个使用量最小的的templateRepo即可
        log.info("query template repo list from koosearch, pageNum is {}", pageNum);
        ListKnowledgeRepoReq listKnowledgeRepoRe = new ListKnowledgeRepoReq().setType(TEMPLATE)
            .setPageNum(pageNum)
            .setPageSize(batchQuerySize);
        ListKnowledgeRepoResp knowledgeRepoResp = cssUniSearchService.listTemplateKnowledgeRepos(listKnowledgeRepoRe,
            connectionId, KNOWLEDGE_TEMPLATE_NUM_ORDER, TEMPLATE, templateRepoInfo.getEmbeddingModel(),
            templateRepoInfo.getRerankModel());
        List<KnowledgeRepoInfo> templateRepos = knowledgeRepoResp.getDataList();
        String embeddingModel = templateRepoInfo.getEmbeddingModel();
        String rerankModel = templateRepoInfo.getRerankModel();
        if (CollectionUtils.isEmpty(templateRepos)) {
            log.info("template repo with embedding model:{} and rerank model: {} is none!", embeddingModel,
                rerankModel);
            // 如果完全没有对应的模板知识库，则直接创建
            cssUniSearchService.createTemplateRepo(connectionId, embeddingModel, rerankModel);
            return;
        }
        KnowledgeRepoInfo knowledgeRepoInfo = templateRepos.get(0);
        if (!capacityIsEnough(knowledgeRepoInfo)) {
            log.info("capacity is not enough, need to create template repo!");
            // 如果最少容量的模板知识库的用量已经大于总容量的XX%（可配置），则代表容量不足，需要新创建模板知识库
            cssUniSearchService.createTemplateRepo(connectionId, embeddingModel, rerankModel);
        }
    }

    private boolean capacityIsEnough(KnowledgeRepoInfo templateKnowledgeRepoInfo) {
        // 如果最少容量的模板知识库的用量已经大于总容量的XX%（可配置），则代表容量不足，需要新创建模板知识库
        @NotNull Integer leftQuota = templateKnowledgeRepoInfo.getShareRepoQuota();
        @NotNull Integer used = templateKnowledgeRepoInfo.getShareRepoUsed();
        int total = leftQuota + used;
        String repoId = templateKnowledgeRepoInfo.getId();
        log.info("templateRepoId is {}, total capacity is {}, left capacity is {}", repoId, total, leftQuota);
        // 如果剩余的配额大于一定使用率下剩余配额，说明配额足够，否则不够
        return total * (1 - KNOWLEDGE_TEMPLATE_USAGE_RATIO) < leftQuota;
    }
}
