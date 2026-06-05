/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.multiRetriveMerigingStrategy;

import com.openjiuwen.studio.agent.manager.dto.ChatReferenceInfo;
import com.openjiuwen.studio.agent.manager.dto.SearchKnowledgeRepoResponseBody;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 此策略按照KooSearch的评分直接全排序取最前topK
 */

@Service("KooSearchScoreMergingStrategy")
public class KooSearchScoreMergingStrategy implements RetrieveMergingStrategy {

    @Override
    public SearchKnowledgeRepoResponseBody mergingRetrievedResult(
        List<SearchKnowledgeRepoResponseBody> batchRetrieveKnowledgeBaseRsps, Integer topK) {
        if (batchRetrieveKnowledgeBaseRsps.size() == 1) {
            return batchRetrieveKnowledgeBaseRsps.get(0);
        }
        List<ChatReferenceInfo> retrieveResultInfos = new ArrayList<>();
        batchRetrieveKnowledgeBaseRsps.forEach(v -> retrieveResultInfos.addAll(
            Optional.ofNullable(v.getSearchResultList()).orElse(Collections.emptyList())));
        retrieveResultInfos.sort(Comparator.comparing(ChatReferenceInfo::getScore).reversed());
        List<ChatReferenceInfo> mergedRetrieveInfos = retrieveResultInfos.subList(0,
            Math.min(topK, retrieveResultInfos.size()));
        SearchKnowledgeRepoResponseBody batchRetrieveKnowledgeBaseRsp = new SearchKnowledgeRepoResponseBody();
        batchRetrieveKnowledgeBaseRsp.setTotal(0);
        batchRetrieveKnowledgeBaseRsp.setSearchResultList(mergedRetrieveInfos);
        return batchRetrieveKnowledgeBaseRsp;
    }

}
