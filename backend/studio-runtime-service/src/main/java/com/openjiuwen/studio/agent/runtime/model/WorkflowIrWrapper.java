/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import com.openjiuwen.studio.agent.common.sensitive.SensitiveEntity;

import lombok.Data;

import java.util.Map;

@Data
public class WorkflowIrWrapper {
    private String md5;

    private String workflowName;

    private WorkflowIrMetadata metadata;

    private Map<String, String> idNameMap;

    private SensitiveEntity sensitiveEntity;

    private boolean safetyBarrier;

    public boolean isContentReviewEnabled() {
        return sensitiveEntity != null && sensitiveEntity.isEnabled();
    }
}
