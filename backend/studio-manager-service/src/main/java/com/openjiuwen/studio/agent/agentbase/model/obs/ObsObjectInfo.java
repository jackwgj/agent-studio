/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.obs;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : ObsObject
 * @Description :
 * @Date : Created in 2023/9/28 10:57
 * @Version : V1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObsObjectInfo {
    /**
     * 是否为文件
     */
    @JsonProperty("is_object")
    private Boolean isObject;

    @JsonProperty("bucket_name")
    private String bucketName;

    @JsonProperty("object_key")
    private String objectKey;

    @JsonProperty("file_name")
    private String fileName;

    /**
     * 文件最新修改时间
     * <br>
     * CST时间：Thu Jun 20 11:24:41 CST 2024
     */
    @JsonProperty("last_modified")
    private String lastModified;

    /**
     * 文件大小:xxx B
     */
    @JsonProperty("content_length")
    private Long contentLength;
}
