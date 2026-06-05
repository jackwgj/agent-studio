/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.FileInfoResponse;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.KnowledgeRepo;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFaqFileReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFileReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListStudioKnowledgeFaqFilesResponseBody;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionEntity;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.KnowledgeRepoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 知识库Service
 *
 * @since 2025-01-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public abstract class AbstractThirdPartyKnowledgeService implements KnowledgeRepoService {

    private final KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper;

    protected List<KnowledgeBaseConnectionParam> getConnectionDefinition(String knowledgeBaseId) {
        KnowledgeBaseConnectionEntity knowledgeBaseConnectionEntity
            = knowledgeBaseConnectionMapper.findById(knowledgeBaseId);
        if (ObjectUtils.isEmpty(knowledgeBaseConnectionEntity)) {
            log.warn("can not find third part for knowledgeBaseId:{} ", knowledgeBaseId);
            throw new AgentStudioException(StudioError.THIRD_PARTY_KNOWLEDGE_CONNECTION_NOT_EXISTS, knowledgeBaseId);
        }
        return knowledgeBaseConnectionEntity.getParams();
    }


    @Override
    public ResponseEntity<byte[]> downloadFile(KnowledgeRepo knowledgeRepo, String fileId) {
        log.error("downloadFile not support in third party knowledge");
        throw new AgentStudioException(StudioError.THIRD_PARTY_KNOWLEDGE_BASE_UNSUPPORT_CURRENT_OPERATION);
    }

    @Override
    public Resource queryImage(KnowledgeRepo knowledgeRepo, String imageId) {
        log.error("queryImage not support in third party knowledge");
        throw new AgentStudioException(StudioError.THIRD_PARTY_KNOWLEDGE_BASE_UNSUPPORT_CURRENT_OPERATION);
    }

    @Override
    public FileInfoResponse listFiles(KnowledgeRepo knowledgeRepo, ListFileReq listFileReq) {
        log.error("listFiles not support in third party knowledge");
        throw new AgentStudioException(StudioError.THIRD_PARTY_KNOWLEDGE_BASE_UNSUPPORT_CURRENT_OPERATION);
    }

    @Override
    public ListStudioKnowledgeFaqFilesResponseBody listFaqFiles(KnowledgeRepo knowledgeRepo,
        ListFaqFileReq listFaqFileReq) {
        log.error("listFaqFiles not support in third party knowledge");
        throw new AgentStudioException(StudioError.THIRD_PARTY_KNOWLEDGE_BASE_UNSUPPORT_CURRENT_OPERATION);
    }

    @Override
    public ResponseEntity<byte[]> downloadFaqFile(KnowledgeRepo knowledgeRepo, String fileId) {
        log.error("downloadFaqFile not support in third party knowledge");
        throw new AgentStudioException(StudioError.THIRD_PARTY_KNOWLEDGE_BASE_UNSUPPORT_CURRENT_OPERATION);
    }
}
