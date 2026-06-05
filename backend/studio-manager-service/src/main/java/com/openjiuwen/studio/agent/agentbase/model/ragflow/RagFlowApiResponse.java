/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import lombok.Data;

@Data
public class RagFlowApiResponse<T> {

    int code;

    String message;

    T data;
}
