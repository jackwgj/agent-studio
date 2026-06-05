/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.Data;

import java.util.List;

/**
 * IdentifyResult
 *
 */
@Data
public class IdentifyResult {
    private String name;

    private String imageURL;

    private String detectChannel;

    private String status;

    private String taskId;

    private String securityResult;

    private List<String> riskLabels;

    private RiskDegree riskDegree;

    private List<String> subRiskLabels;
}
