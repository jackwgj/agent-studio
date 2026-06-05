/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.multiretrivemerigingstrategy;


import com.openjiuwen.studio.agent.runtime.dto.RagResultReference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 此策略按照KooSearch的评分直接全排序取最前topK
 */

@Slf4j
@Service("KooSearchScoreMergingStrategy")
@RequiredArgsConstructor
public class KooSearchScoreMergingStrategy implements RetrieveMergingStrategy {

    @Override
    public List<RagResultReference> mergingRetrievedResult(
        List<List<RagResultReference>> batchRetrieveKnowledgeBaseRsps, Integer topK) {
        if (batchRetrieveKnowledgeBaseRsps.size() == 1) {
            return batchRetrieveKnowledgeBaseRsps.get(0);
        }
        List<RagResultReference> retrieveResultInfos = new ArrayList<>();
        batchRetrieveKnowledgeBaseRsps.forEach(
            item -> retrieveResultInfos.addAll(Optional.ofNullable(item).orElse(Collections.emptyList())));
        retrieveResultInfos.sort(Comparator.comparing(RagResultReference::getScore).reversed());
        return retrieveResultInfos.subList(0, Math.min(topK, retrieveResultInfos.size()));
    }

}
