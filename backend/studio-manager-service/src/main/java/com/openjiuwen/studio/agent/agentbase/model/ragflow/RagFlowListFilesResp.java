/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import lombok.Data;

import java.util.List;

@Data
public class RagFlowListFilesResp {

    private Integer total;

    private List<Document> docs;
}
