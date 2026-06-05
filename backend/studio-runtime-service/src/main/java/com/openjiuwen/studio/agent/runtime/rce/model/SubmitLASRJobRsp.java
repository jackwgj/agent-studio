/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交录音文件识别任务响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitLASRJobRsp {
    /**
     * 任务id
     */
    @JsonProperty("job_id")
    private String jobId;
}
