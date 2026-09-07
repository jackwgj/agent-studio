/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.repository;

import com.openjiuwen.studio.conversation.domain.model.Conversation;
import com.openjiuwen.studio.conversation.domain.model.ConversationMessage;
import com.openjiuwen.studio.conversation.domain.model.ConversationWorkflowNode;
import com.openjiuwen.studio.conversation.domain.model.valueobject.ExecutionRef;
import com.openjiuwen.studio.conversation.domain.model.valueobject.FileRef;
import com.openjiuwen.studio.conversation.domain.model.valueobject.TokenUsage;
import com.openjiuwen.studio.conversation.domain.model.valueobject.ToolRef;
import com.openjiuwen.studio.conversation.domain.repository.ConversationRepository;
import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationEntity;
import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationRunEntity;
import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationSubRunEntity;
import com.openjiuwen.studio.conversation.infrastructure.entity.ConversationWorkflowEntity;

import com.alibaba.fastjson2.JSON;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 会话聚合仓库 JPA 实现（领域模型 ↔ 持久化模型映射）
 */
@Repository
public class ConversationRepositoryImpl implements ConversationRepository {

    // TODO 将常量统一构建常量表，不要每个类单独构建，做统一管理和引用
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;

    private final ConversationEntityRepository conversationEntityRepository;
    private final ConversationRunEntityRepository runEntityRepository;
    private final ConversationSubRunEntityRepository subRunEntityRepository;
    private final ConversationWorkflowEntityRepository workflowEntityRepository;

    public ConversationRepositoryImpl(ConversationEntityRepository conversationEntityRepository,
                                      ConversationRunEntityRepository runEntityRepository,
                                      ConversationSubRunEntityRepository subRunEntityRepository,
                                      ConversationWorkflowEntityRepository workflowEntityRepository) {
        this.conversationEntityRepository = conversationEntityRepository;
        this.runEntityRepository = runEntityRepository;
        this.subRunEntityRepository = subRunEntityRepository;
        this.workflowEntityRepository = workflowEntityRepository;
    }

    @Override
    public Optional<Conversation> findById(String conversationId) {
        return conversationEntityRepository.findById(conversationId)
            .filter(entity -> !Objects.equals(entity.getDeleted(), DELETED))
            .map(this::toDomain);
    }

    @Override
    public List<Conversation> listByOwner(String projectId, String workspaceId, String ownerUserId, int page,
                                          int size) {
        return conversationEntityRepository
            .findByProjectIdAndWorkspaceIdAndOwnerUserIdAndDeletedOrderByUpdatedOnDesc(
                projectId, workspaceId, ownerUserId, NOT_DELETED, PageRequest.of(page, size))
            .stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public long countByOwner(String projectId, String workspaceId, String ownerUserId) {
        return conversationEntityRepository
            .countByProjectIdAndWorkspaceIdAndOwnerUserIdAndDeleted(projectId, workspaceId, ownerUserId, NOT_DELETED);
    }

    @Override
    public Conversation save(Conversation conversation) {
        ConversationEntity entity = new ConversationEntity();
        entity.setConversationId(conversation.getConversationId());
        entity.setTitle(conversation.getTitle());
        entity.setProjectId(conversation.getProjectId());
        entity.setWorkspaceId(conversation.getWorkspaceId());
        entity.setDomainId(conversation.getDomainId());
        entity.setOwnerDomainId(conversation.getOwnerDomainId());
        entity.setOwnerUserId(conversation.getOwnerUserId());
        entity.setSource(conversation.getSource());
        entity.setStatus(conversation.getStatus());
        entity.setDeleted(NOT_DELETED);
        conversationEntityRepository.save(entity);
        return conversation;
    }

    @Override
    @Transactional
    public void appendMessages(String conversationId, List<ConversationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        ConversationEntity entity = conversationEntityRepository.findById(conversationId).orElse(null);
        if (entity == null) {
            return;
        }
        List<ConversationRunEntity> runs = new ArrayList<>();
        List<ConversationSubRunEntity> subRuns = new ArrayList<>();
        for (ConversationMessage message : messages) {
            ExecutionRef ref = message.getExecutionRef();
            if (ref != null && ref.getParentRunId() != null) {
                subRuns.add(toSubRunEntity(conversationId, entity, message, ref));
            } else {
                runs.add(toRunEntity(conversationId, entity, message));
            }
        }
        if (!runs.isEmpty()) {
            runEntityRepository.saveAll(runs);
        }
        if (!subRuns.isEmpty()) {
            subRunEntityRepository.saveAll(subRuns);
        }
        // 触碰会话 updated_on，保证历史栏按最近活跃倒序
        entity.setUpdatedOn(new Date());
        conversationEntityRepository.save(entity);
    }

    @Override
    @Transactional
    public void appendWorkflowNodes(String conversationId, List<ConversationWorkflowNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        ConversationEntity conversation = conversationEntityRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return;
        }
        List<ConversationWorkflowEntity> entities = nodes.stream().map(node -> {
            ConversationWorkflowEntity entity = new ConversationWorkflowEntity();
            entity.setConversationId(conversationId);
            entity.setToolId(node.getToolId());
            entity.setParentRunId(node.getParentRunId());
            entity.setWorkflowId(node.getWorkflowId());
            entity.setNodeId(node.getNodeId());
            entity.setNodeName(node.getNodeName());
            entity.setNodeType(node.getNodeType());
            entity.setNodeIndex(node.getNodeIndex());
            entity.setStatus(node.getStatus());
            entity.setInputContent(node.getInputContent());
            entity.setOutputContent(node.getOutputContent());
            entity.setErrorCode(node.getErrorCode());
            entity.setErrorMessage(node.getErrorMessage());
            entity.setStartedOn(node.getStartedOn());
            entity.setFinishedOn(node.getFinishedOn());
            entity.setProjectId(conversation.getProjectId());
            entity.setWorkspaceId(conversation.getWorkspaceId());
            entity.setDomainId(conversation.getDomainId());
            entity.setCreatorId(node.getCreatorId());
            entity.setCreatedOn(new Date());
            entity.setUpdatedOn(new Date());
            entity.setDeleted(NOT_DELETED);
            return entity;
        }).toList();
        workflowEntityRepository.saveAll(entities);
        conversation.setUpdatedOn(new Date());
        conversationEntityRepository.save(conversation);
    }

    @Override
    @Transactional
    public void softDeleteAndScheduleCleanup(String conversationId) {
        int changed = conversationEntityRepository.markDeletedAndPendingCleanup(
            conversationId, new Date());
        if (changed != 1) {
            throw new IllegalStateException("conversation delete state changed concurrently: " + conversationId);
        }
    }

    private Conversation toDomain(ConversationEntity entity) {
        List<ConversationMessage> messages = new ArrayList<>();
        runEntityRepository.findByConversationIdAndDeletedOrderByCreatedOnAscIdAsc(
                entity.getConversationId(), NOT_DELETED)
            .forEach(run -> messages.add(toDomainMessage(run)));
        subRunEntityRepository.findByConversationIdAndDeletedOrderByCreatedOnAscIdAsc(
                entity.getConversationId(), NOT_DELETED)
            .forEach(subRun -> messages.add(toDomainMessage(subRun)));

        // 主/子消息按 created_on 合并排序（同一时间戳保持稳定）
        messages.sort((a, b) -> {
            int cmp = Objects.compare(a.getCreatedAt(), b.getCreatedAt(),
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            return cmp;
        });

        return Conversation.builder()
            .conversationId(entity.getConversationId())
            .title(entity.getTitle())
            .projectId(entity.getProjectId())
            .workspaceId(entity.getWorkspaceId())
            .domainId(entity.getDomainId())
            .ownerDomainId(entity.getOwnerDomainId())
            .ownerUserId(entity.getOwnerUserId())
            .source(entity.getSource())
            .status(entity.getStatus())
            .messages(messages)
            .createdAt(entity.getCreatedOn())
            .updatedAt(entity.getUpdatedOn())
            .build();
    }

    private ConversationMessage toDomainMessage(ConversationRunEntity run) {
        return ConversationMessage.builder()
            .role(run.getRole())
            .content(run.getContent())
            .toolRef(run.getToolId() == null ? null : new ToolRef(run.getToolId(), run.getToolName(), run.getToolArgs()))
            .fileRefs(parseFileRefs(run.getFileIds()))
            .executionRef(new ExecutionRef(run.getRunId(), run.getParentRunId(), run.getAgentId(), run.getExecutionType()))
            .workflowId(run.getWorkflowId())
            .nodeId(run.getNodeId())
            .eventIndex(run.getEventIndex())
            .tokenUsage(new TokenUsage(run.getPromptTokens(), run.getCompletionTokens(), run.getTotalTokens()))
            .event(run.getEvent())
            .modelDeploymentId(run.getModelDeploymentId())
            .createdAt(run.getCreatedOn())
            .build();
    }

    private ConversationMessage toDomainMessage(ConversationSubRunEntity subRun) {
        return ConversationMessage.builder()
            .role(subRun.getRole())
            .content(subRun.getContent())
            .toolRef(subRun.getToolId() == null ? null : new ToolRef(subRun.getToolId(), subRun.getToolName(), subRun.getToolArgs()))
            .fileRefs(parseFileRefs(subRun.getFileIds()))
            .executionRef(new ExecutionRef(subRun.getRunId(), subRun.getParentRunId(), subRun.getAgentId(), subRun.getExecutionType()))
            .workflowId(subRun.getWorkflowId())
            .nodeId(subRun.getNodeId())
            .eventIndex(subRun.getEventIndex())
            .tokenUsage(new TokenUsage(subRun.getPromptTokens(), subRun.getCompletionTokens(), subRun.getTotalTokens()))
            .event(subRun.getEvent())
            .createdAt(subRun.getCreatedOn())
            .build();
    }

    private ConversationRunEntity toRunEntity(String conversationId, ConversationEntity entity,
                                               ConversationMessage message) {
        ConversationRunEntity run = new ConversationRunEntity();
        run.setConversationId(conversationId);
        run.setProjectId(entity.getProjectId());
        run.setWorkspaceId(entity.getWorkspaceId());
        run.setDomainId(entity.getDomainId());
        run.setRole(message.getRole());
        run.setContent(message.getContent());
        run.setDeleted(NOT_DELETED);
        if (message.getToolRef() != null) {
            run.setToolId(message.getToolRef().getToolId());
            run.setToolName(message.getToolRef().getToolName());
            run.setToolArgs(message.getToolRef().getArgs());
        }
        run.setFileIds(encodeFileRefs(message.getFileRefs()));
        if (message.getExecutionRef() != null) {
            run.setRunId(message.getExecutionRef().getRunId());
            run.setParentRunId(message.getExecutionRef().getParentRunId());
            run.setAgentId(message.getExecutionRef().getAgentId());
            run.setExecutionType(message.getExecutionRef().getExecutionType());
        }
        run.setWorkflowId(message.getWorkflowId());
        run.setNodeId(message.getNodeId());
        run.setEventIndex(message.getEventIndex());
        if (message.getTokenUsage() != null) {
            run.setPromptTokens(message.getTokenUsage().getPromptTokens());
            run.setCompletionTokens(message.getTokenUsage().getCompletionTokens());
            run.setTotalTokens(message.getTokenUsage().getTotalTokens());
        }
        run.setEvent(message.getEvent());
        run.setModelDeploymentId(message.getModelDeploymentId());
        // 按传入时间写入 created_on（监听器按到达序赋值，保证读序"先调用先渲染"；null 时回退 DB 默认）
        run.setCreatedOn(message.getCreatedAt());
        return run;
    }

    private ConversationSubRunEntity toSubRunEntity(String conversationId, ConversationEntity entity,
                                                    ConversationMessage message, ExecutionRef ref) {
        ConversationSubRunEntity subRun = new ConversationSubRunEntity();
        subRun.setConversationId(conversationId);
        subRun.setProjectId(entity.getProjectId());
        subRun.setWorkspaceId(entity.getWorkspaceId());
        subRun.setDomainId(entity.getDomainId());
        subRun.setRole(message.getRole());
        subRun.setContent(message.getContent());
        subRun.setDeleted(NOT_DELETED);
        if (message.getToolRef() != null) {
            subRun.setToolId(message.getToolRef().getToolId());
            subRun.setToolName(message.getToolRef().getToolName());
            subRun.setToolArgs(message.getToolRef().getArgs());
        }
        subRun.setFileIds(encodeFileRefs(message.getFileRefs()));
        subRun.setRunId(ref.getRunId());
        subRun.setParentRunId(ref.getParentRunId());
        subRun.setAgentId(ref.getAgentId());
        subRun.setExecutionType(ref.getExecutionType());
        subRun.setWorkflowId(message.getWorkflowId());
        subRun.setNodeId(message.getNodeId());
        subRun.setEventIndex(message.getEventIndex());
        if (message.getTokenUsage() != null) {
            subRun.setPromptTokens(message.getTokenUsage().getPromptTokens());
            subRun.setCompletionTokens(message.getTokenUsage().getCompletionTokens());
            subRun.setTotalTokens(message.getTokenUsage().getTotalTokens());
        }
        subRun.setEvent(message.getEvent());
        // 按传入时间写入 created_on（同 run 表）
        subRun.setCreatedOn(message.getCreatedAt());
        return subRun;
    }

    private List<FileRef> parseFileRefs(String fileIds) {
        if (fileIds == null || fileIds.isBlank()) {
            return null;
        }
        try {
            List<FileRef> refs = JSON.parseArray(fileIds, FileRef.class);
            if (refs != null && !refs.isEmpty() && refs.get(0).getObjectKey() != null) {
                return refs;
            }
        } catch (Exception ignored) {
            // 兼容历史裸字符串数组。
        }
        try {
            return JSON.parseArray(fileIds, String.class).stream().map(FileRef::new).toList();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String encodeFileRefs(List<FileRef> fileRefs) {
        if (fileRefs == null || fileRefs.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(fileRefs);
    }
}
