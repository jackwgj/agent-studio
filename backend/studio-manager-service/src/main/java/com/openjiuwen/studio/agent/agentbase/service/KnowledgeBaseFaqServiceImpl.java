/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeRepoMapper;
import com.openjiuwen.studio.agent.common.dto.knowledge.FaqSearchCriteria;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeRepoContext;
import com.openjiuwen.studio.agent.agentbase.utils.ShareScopeValidUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqReq;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteFaqResp;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateFaqReq;
import com.openjiuwen.studio.agent.manager.dto.CreateFaqResp;
import com.openjiuwen.studio.agent.common.dto.knowledge.KnowledgeFaq;
import com.openjiuwen.studio.agent.manager.dto.ListFaqQo;
import com.openjiuwen.studio.agent.manager.dto.ListFaqResp;
import com.openjiuwen.studio.agent.manager.dto.ModifyFaqReq;
import com.openjiuwen.studio.agent.manager.dto.ModifyFaqResp;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeFaqManagementService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class KnowledgeBaseFaqServiceImpl implements IKnowledgeFaqManagementService {

    @Value("${knowledge.source}")
    private String knowledgeSource;

    private final KnowledgeRepoContext knowledgeRepoContext;

    private final KnowledgeRepoMapper knowledgeRepoMapper;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public KnowledgeBaseFaqServiceImpl(KnowledgeRepoMapper knowledgeRepoMapper,
        KnowledgeRepoContext knowledgeRepoContext, KnowledgeBaseMapper knowledgeBaseMapper) {
        this.knowledgeRepoContext = knowledgeRepoContext;
        this.knowledgeRepoMapper = knowledgeRepoMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public BatchDeleteFaqResp batchDeleteFaq(String projectId, String knowledgeBaseId, String workspaceId,
        BatchDeleteFaqReq body) {
        log.info("operation log projectId: {}, userId:{}, batchDeleteFaq", projectId,
            RequestContextUtils.getRequestUserId());
        final KnowledgeBaseEntity knowledgeBaseEntity = getKnowledgeBase(knowledgeBaseId);

        int deletedCount = knowledgeRepoContext.getService(knowledgeSource)
            .deleteFaqBatch(knowledgeBaseEntity.getExternalId(), body.getFaqIds());
        return new BatchDeleteFaqResp().setTotalCount(body.getFaqIds().size()).setDeletedCount(deletedCount);
    }

    @Override
    public CreateFaqResp createFaq(String projectId, String workspaceId, String knowledgeBaseId, CreateFaqReq body) {
        log.info("operation log projectId: {}, userId:{}, createFaq", projectId,
            RequestContextUtils.getRequestUserId());
        final KnowledgeBaseEntity knowledgeBaseEntity = getKnowledgeBase(knowledgeBaseId);
        KnowledgeFaq knowledgeFaq = new KnowledgeFaq().setKnowledgeRepoId(knowledgeBaseEntity.getExternalId())
            .setAnswer(body.getAnswer())
            .setQuestion(body.getQuestion())
            .setQuestion1(body.getQuestion1())
            .setQuestion2(body.getQuestion2())
            .setQuestion3(body.getQuestion3())
            .setQuestion4(body.getQuestion4())
            .setCategory(body.getCategory());
        String faqId = knowledgeRepoContext.getService(knowledgeSource).createFaq(knowledgeFaq);
        return new CreateFaqResp().setFaqId(faqId);
    }

    @Override
    public CommonDeleteRsp deleteFaq(String projectId, String knowledgeBaseId, String faqId, String workspaceId) {
        log.info("operation log projectId: {}, userId:{}, deleteFaq", projectId,
            RequestContextUtils.getRequestUserId());
        final KnowledgeBaseEntity knowledgeBaseEntity = getKnowledgeBase(knowledgeBaseId);

        knowledgeRepoContext.getService(knowledgeSource).deleteFaq(knowledgeBaseEntity.getExternalId(), faqId);
        return new CommonDeleteRsp().setId(faqId);
    }

    @Override
    public ListFaqResp listFaq(String projectId, String knowledgeBaseId, ListFaqQo listFaqQo) {
        final KnowledgeBaseEntity knowledgeBaseEntity = getKnowledgeBase(knowledgeBaseId);
        ShareScopeValidUtil.checkKnowledgeScope(knowledgeBaseId, listFaqQo.getWorkspaceId(), knowledgeBaseEntity);
        FaqSearchCriteria faqSearchCriteria = FaqSearchCriteria.builder()
            .repoId(knowledgeBaseEntity.getExternalId())
            .pageNum(listFaqQo.getOffset() / listFaqQo.getLimit() + 1)
            .pageSize(listFaqQo.getLimit())
            .question(listFaqQo.getQuestion())
            .build();
        return knowledgeRepoContext.getService(knowledgeSource).listFaq(faqSearchCriteria);
    }

    @Override
    public ModifyFaqResp modifyFaq(String projectId, String workspaceId, String knowledgeBaseId, String faqId,
        ModifyFaqReq body) {
        log.info("operation log projectId: {}, userId:{}, modifyFaq", projectId,
            RequestContextUtils.getRequestUserId());
        final KnowledgeBaseEntity knowledgeBaseEntity = getKnowledgeBase(knowledgeBaseId);

        KnowledgeFaq knowledgeFaq = new KnowledgeFaq().setKnowledgeRepoId(knowledgeBaseEntity.getExternalId())
            .setFaqId(faqId)
            .setAnswer(body.getAnswer())
            .setQuestion(body.getQuestion())
            .setQuestion1(body.getQuestion1())
            .setQuestion2(body.getQuestion2())
            .setQuestion3(body.getQuestion3())
            .setQuestion4(body.getQuestion4())
            .setCategory(body.getCategory());
        String modifiedFaqId = knowledgeRepoContext.getService(knowledgeSource).modifyFaq(knowledgeFaq);
        return new ModifyFaqResp().setFaqId(modifiedFaqId);
    }

    private KnowledgeBaseEntity getKnowledgeBase(String knowledgeBaseId) {
        final KnowledgeBaseEntity knowledgeBaseEntity = knowledgeBaseMapper.selectById(
            RequestContextUtils.getRequestProjectId(), knowledgeBaseId);
        if (Objects.isNull(knowledgeBaseEntity)) {
            log.error("knowledge base {} is not exist", knowledgeBaseId);
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST);
        }
        return knowledgeBaseEntity;
    }
}
