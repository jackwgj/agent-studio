/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.multiretrivemerigingstrategy;


import com.openjiuwen.studio.agent.runtime.dto.RagResultReference;

import java.util.List;

public interface RetrieveMergingStrategy {

    List<RagResultReference> mergingRetrievedResult(List<List<RagResultReference>> batchRetrieveKnowledgeBaseRsps,
        Integer topK);

}
