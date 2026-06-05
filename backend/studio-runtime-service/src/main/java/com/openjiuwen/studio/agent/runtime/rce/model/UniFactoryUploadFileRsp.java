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
 * UniFactory上传文件响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniFactoryUploadFileRsp {
    /**
     * 文档解析任务id
     */
    @JsonProperty("task_id")
    private String taskId;
}
