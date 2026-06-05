/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import lombok.Data;

import org.springframework.http.ResponseEntity;

/**
 * LakeSearchClient请求结果
 *
 * @since 2025-06-13
 */
@Data
public class ResultModel {
    private int statusCode;

    private String content;

    private ResponseEntity<byte[]> responseEntity;

    private ErrorInfo errorDetail;
}
