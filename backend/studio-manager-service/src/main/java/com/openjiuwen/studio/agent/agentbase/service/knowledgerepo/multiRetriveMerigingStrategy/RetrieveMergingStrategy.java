/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.multiRetriveMerigingStrategy;

import com.openjiuwen.studio.agent.manager.dto.SearchKnowledgeRepoResponseBody;

import java.util.List;

public interface RetrieveMergingStrategy {

    SearchKnowledgeRepoResponseBody mergingRetrievedResult(
        List<SearchKnowledgeRepoResponseBody> batchRetrieveKnowledgeBaseRsps, Integer topK);

}
