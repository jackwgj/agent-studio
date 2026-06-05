/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CSS文档信息实体类
 *
 * @since 2024-04-24
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

    private List<String> tags;

    @JsonProperty("upload_desc")
    private String uploadDesc;

    @JsonProperty("create_user")
    private String createUser;

    @JsonProperty("create_time")
    private String createTime;
}
