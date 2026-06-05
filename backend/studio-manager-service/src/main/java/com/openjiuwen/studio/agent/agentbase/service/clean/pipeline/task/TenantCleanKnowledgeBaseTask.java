/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task;

import com.openjiuwen.studio.agent.agentbase.common.enums.KnowledgeBaseType;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeRepoMapper;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeRepo;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineContext;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineTask;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.subtask.TenantCleanKnowledgeTableSubtask;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeRepoContext;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 租户知识库清理任务
 * <p>
 * 负责清理自建知识库内容和相关数据表：
 * 1. 先调用平台接口清理自建知识库
 * 2. 清理相关数据表（包括表数据）
 *
 * @since 2025-12-20
 */
@Slf4j
@Component
@Order(10) // 最先执行知识库清理
public class TenantCleanKnowledgeBaseTask implements TenantCleanPipelineTask {

    private static final String LOG_CLEAN_INTERNAL_KB_START
        = "Starting internal knowledge base cleanup for domainId: {}, knowledgeBase: {}";

    private static final String LOG_CLEAN_INTERNAL_KB_SUCCESS
        = "Successfully cleaned internal knowledgeBase: {} for domainId: {}";

    private static final String LOG_CLEAN_INTERNAL_KB_FAILED
        = "Failed to clean internal knowledgeBase: {} for domainId: {}, error: {}";

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final KnowledgeRepoMapper knowledgeRepoMapper;

    private final KnowledgeRepoContext knowledgeRepoContext;

    @Resource
    private TenantCleanKnowledgeTableSubtask cleanKnowledgeTableSubtask;

    public TenantCleanKnowledgeBaseTask(KnowledgeBaseMapper knowledgeBaseMapper,
        KnowledgeRepoMapper knowledgeRepoMapper, KnowledgeRepoContext knowledgeRepoContext) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeRepoMapper = knowledgeRepoMapper;
        this.knowledgeRepoContext = knowledgeRepoContext;
    }

    @Override
    public void execute(TenantCleanPipelineContext context) {
        try {
            String domainId = context.getDomainId();
            log.info("Starting knowledge base cleanup for domainId: {}", domainId);

            // 1. 查询需要清理的知识库
            List<KnowledgeBaseEntity> knowledgeBases = knowledgeBaseMapper.selectByDomainId(domainId);
            if (knowledgeBases.isEmpty()) {
                log.info("No knowledge bases found for domainId: {}, skipping cleanup", domainId);
                return;
            }

            log.info("Found {} knowledge bases for domainId: {}", knowledgeBases.size(), domainId);

            // 2. 先清理自建知识库（调用平台接口）
            cleanupInternalKnowledgeBases(knowledgeBases);

            // 3. 清理知识库相关表数据
            cleanKnowledgeTableSubtask.executeCleanup(domainId);

            log.info("Knowledge base cleanup completed successfully for domainId: {}", domainId);

        } catch (Exception e) {
            log.error("Knowledge base cleanup failed for domainId: {}, error: {}", context.getDomainId(),
                e.getMessage());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public String getName() {
        return "KnowledgeBaseCleanup";
    }

    /**
     * 清理自建知识库（需要调用平台接口）
     * 如果自建知识库清理失败，抛出异常触发事务回滚
     *
     * @param knowledgeBases 所有需要清理的知识库列表
     */
    private void cleanupInternalKnowledgeBases(List<KnowledgeBaseEntity> knowledgeBases) {
        List<KnowledgeBaseEntity> internalKnowledgeBases = knowledgeBases.stream()
            .filter(this::isInternalKnowledgeBase)
            .toList();

        if (CollectionUtils.isEmpty(internalKnowledgeBases)) {
            log.info("No internal knowledge bases found, skipping platform interface cleanup");
            return;
        }

        log.info("Found {} internal knowledge bases to cleanup", internalKnowledgeBases.size());

        for (KnowledgeBaseEntity kb : internalKnowledgeBases) {
            deleteInternalKnowledgeBase(kb);
        }
    }

    /**
     * 判断是否为自建知识库（需要调用知识库平台接口删除）
     */
    private boolean isInternalKnowledgeBase(KnowledgeBaseEntity knowledgeBase) {
        return KnowledgeBaseType.INTERNAL.getValue().equals(knowledgeBase.getType());
    }

    /**
     * 删除自建知识库（调用平台接口）
     * 如果删除失败抛出异常
     *
     * @param knowledgeBase 知识库实体
     */
    private void deleteInternalKnowledgeBase(KnowledgeBaseEntity knowledgeBase) {
        if (knowledgeBase.getExternalId() == null) {
            log.warn("KnowledgeBase externalId is null for knowledgeBaseId: {}, skipping platform delete",
                knowledgeBase.getId());
            return;
        }

        try {
            log.info(LOG_CLEAN_INTERNAL_KB_START, knowledgeBase.getDomainId(), knowledgeBase.getId());

            // 查询KnowledgeRepoEntity
            KnowledgeRepoEntity knowledgeRepoEntity = knowledgeRepoMapper.selectByPrimaryKey(knowledgeBase.getId(),
                knowledgeBase.getProjectId());

            if (knowledgeRepoEntity == null) {
                log.warn("KnowledgeRepoEntity not found for knowledgeBaseId: {}, treating as success",
                    knowledgeBase.getId());
                return;
            }

            // 构建KnowledgeRepo对象进行删除
            KnowledgeRepo knowledgeRepo = new KnowledgeRepo().setKnowledgeRepoId(knowledgeBase.getExternalId())
                .setMetadata(knowledgeRepoEntity.getMetadata())
                .setType(KnowledgeBaseType.fromValue(knowledgeRepoEntity.getType()));

            // 获取知识库源
            String source = knowledgeRepoEntity.getSource();

            // 调用知识库平台接口删除知识库
            knowledgeRepoContext.getService(source).deleteKnowledgeRepo(knowledgeRepo);

            log.info(LOG_CLEAN_INTERNAL_KB_SUCCESS, knowledgeBase.getId(), knowledgeBase.getDomainId());

        } catch (Exception e) {
            log.error(LOG_CLEAN_INTERNAL_KB_FAILED, knowledgeBase.getId(), knowledgeBase.getDomainId(), e.getMessage());
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
    }
}
