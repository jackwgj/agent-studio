/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.repository;

import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 会话表 JPA 仓库
 */
public interface ConversationEntityRepository extends JpaRepository<ConversationEntity, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE ConversationEntity c SET c.deleted = 1, c.cleanupStatus = 'PENDING', "
        + "c.cleanupAttempts = 0, c.cleanupUpdatedAt = :updatedAt, c.cleanupError = null "
        + "WHERE c.conversationId = :conversationId AND c.deleted = 0")
    int markDeletedAndPendingCleanup(@Param("conversationId") String conversationId,
                                     @Param("updatedAt") Date updatedAt);

    List<ConversationEntity> findByDeletedAndCleanupStatusInAndCleanupAttemptsLessThanOrderByCleanupUpdatedAtAsc(
        Integer deleted, List<String> cleanupStatuses, Integer maxAttempts, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE ConversationEntity c SET c.cleanupStatus = 'PROCESSING', "
        + "c.cleanupAttempts = c.cleanupAttempts + 1, c.cleanupUpdatedAt = :updatedAt "
        + "WHERE c.conversationId = :conversationId AND c.deleted = 1 "
        + "AND (c.cleanupStatus IN ('PENDING', 'FAILED') "
        + "OR (c.cleanupStatus = 'PROCESSING' AND c.cleanupUpdatedAt < :leaseExpiredAt)) "
        + "AND c.cleanupAttempts < :maxAttempts")
    int claimCleanup(@Param("conversationId") String conversationId, @Param("updatedAt") Date updatedAt,
                     @Param("leaseExpiredAt") Date leaseExpiredAt,
                     @Param("maxAttempts") Integer maxAttempts);

    /**
     * 按工作空间维度查询未删除会话，updated_on 倒序分页
     *
     * @param projectId   租户
     * @param workspaceId 工作空间
     * @param ownerUserId 拥有者用户
     * @param deleted     逻辑删除标记
     * @param pageable    分页
     * @return 会话列表
     */
    List<ConversationEntity> findByProjectIdAndWorkspaceIdAndOwnerUserIdAndDeletedOrderByUpdatedOnDesc(
        String projectId, String workspaceId, String ownerUserId, Integer deleted, Pageable pageable);

    /**
     * 按工作空间维度统计会话数
     *
     * @param projectId   租户
     * @param workspaceId 工作空间
     * @param ownerUserId 拥有者用户
     * @param deleted     逻辑删除标记
     * @return 会话数
     */
    long countByProjectIdAndWorkspaceIdAndOwnerUserIdAndDeleted(
        String projectId, String workspaceId, String ownerUserId, Integer deleted);
}
