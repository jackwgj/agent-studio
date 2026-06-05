/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSS文档信息实体类
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CssFileInfo {
    private String id;

    private String name;

    private String type;

    private Long size;

    private String status;

    @JsonProperty("upload_desc")
    private String uploadDesc;

    @JsonProperty("create_user")
    private String createUser;

    @JsonProperty("create_time")
    private String createTime;
}
