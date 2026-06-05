/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeRepoMapper;
import com.openjiuwen.studio.agent.agentbase.model.ListTaskCriteria;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeRepoContext;
import com.openjiuwen.studio.agent.agentbase.service.validate.KnowledgeDatasetValidateService;
import com.openjiuwen.studio.agent.agentbase.utils.ShareScopeValidUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.manager.dto.CommonBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeTaskResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeTaskReq;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTasksResponseBody;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeTaskManagementService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class KnowledgeBaseTaskServiceImpl implements IKnowledgeTaskManagementService {

    @Value("${knowledge.source}")
    private String knowledgeSource;

    private final KnowledgeRepoContext knowledgeRepoContext;

    private final KnowledgeRepoMapper knowledgeRepoMapper;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    private final KnowledgeDatasetValidateService knowledgeDatasetValidateService;

    public KnowledgeBaseTaskServiceImpl(KnowledgeRepoContext knowledgeRepoContext,
        KnowledgeRepoMapper knowledgeRepoMapper, KnowledgeBaseMapper knowledgeBaseMapper,
        KnowledgeDatasetValidateService knowledgeDatasetValidateService) {
        this.knowledgeRepoContext = knowledgeRepoContext;
        this.knowledgeRepoMapper = knowledgeRepoMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.knowledgeDatasetValidateService = knowledgeDatasetValidateService;
    }

    @Override
    public CreateKnowledgeTaskResponseBody createKnowledgeTask(String projectId, String workspaceId,
        String knowledgeBaseId, CreateKnowledgeTaskRequestBody body) {
        log.info("operation log projectId: {}, userId:{}, createKnowledgeBaseTask", projectId,
            RequestContextUtils.getRequestUserId());
        final KnowledgeBaseEntity knowledgeBaseEntity = getKnowledgeBase(knowledgeBaseId);
        final KnowledgeRepoEntity knowledgeRepoEntity = getKnowledgeRepo(knowledgeBaseId);
        if (!knowledgeDatasetValidateService.fileBelongToKnowledgeRepo(body.getFileIds(), knowledgeBaseEntity,
            knowledgeRepoEntity, knowledgeRepoContext)) {
            log.error("The file [{}] are not belong current knowledge base [{}]", body.getFileIds(), knowledgeBaseId);
            throw new AgentBaseException(ErrorCode.CREATE_RETRY_FILES_TASK_ERROR);
        }
        return knowledgeRepoContext.getService(knowledgeSource)
            .createTask(knowledgeBaseEntity.getExternalId(), body.getTaskType().toString(), body.getFileIds());
    }

    @Override
    public CommonBatchDeleteRsp deleteKnowledgeTask(String projectId, String knowledgeBaseId, String workspaceId,
        DeleteKnowledgeTaskReq body) {
        log.info("operation log projectId: {}, userId:{}, deleteKnowledgeBaseTask", projectId,
            RequestContextUtils.getRequestUserId());
        final KnowledgeBaseEntity knowledgeBaseEntity = getKnowledgeBase(knowledgeBaseId);
        // 调用知识库平台接口批量删除任务
        return knowledgeRepoContext.getService(knowledgeSource)
            .deleteKnowledgeTask(knowledgeBaseEntity.getExternalId(), body.getTaskIds());
    }

    @Override
    public ListKnowledgeTasksResponseBody listKnowledgeTasks(String projectId, String knowledgeBaseId,
        ListKnowledgeTasksQo listKnowledgeBaseTaskQo) {
        ListTaskCriteria listTaskCriteria = ListTaskCriteria.builder()
            .pageNum(listKnowledgeBaseTaskQo.getOffset() / listKnowledgeBaseTaskQo.getLimit() + 1)
            .pageSize(listKnowledgeBaseTaskQo.getLimit())
            .taskStatus(listKnowledgeBaseTaskQo.getTaskStatus())
            .taskType(listKnowledgeBaseTaskQo.getTaskType())
            .fileName(listKnowledgeBaseTaskQo.getFileName())
            .build();
        final KnowledgeBaseEntity knowledgeBaseEntity = getKnowledgeBase(knowledgeBaseId);

        ShareScopeValidUtil.checkKnowledgeScope(knowledgeBaseId, knowledgeBaseEntity.getWorkspaceId(),
            knowledgeBaseEntity);
        // 调用知识库平台接口查询任务列表
        return knowledgeRepoContext.getService(knowledgeSource)
            .listKnowledgeTask(knowledgeBaseEntity.getExternalId(), listTaskCriteria);
    }

    private KnowledgeBaseEntity getKnowledgeBase(String knowledgeBaseId) {
        final KnowledgeBaseEntity knowledgeBaseEntity = knowledgeBaseMapper.selectById(
            RequestContextUtils.getRequestProjectId(), knowledgeBaseId);
        if (Objects.isNull(knowledgeBaseEntity)) {
            log.error("knowledgebase {} is not exist", knowledgeBaseId);
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST);
        }
        return knowledgeBaseEntity;
    }

    private KnowledgeRepoEntity getKnowledgeRepo(String knowledgeBaseId) {
        final KnowledgeRepoEntity knowledgeRepoEntity = knowledgeRepoMapper.selectByPrimaryKey(knowledgeBaseId,
            RequestContextUtils.getRequestProjectId());
        if (Objects.isNull(knowledgeRepoEntity)) {
            log.error("knowledge base [{}] is not exist", knowledgeBaseId);
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST);
        }
        return knowledgeRepoEntity;
    }
}
