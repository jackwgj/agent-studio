/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件传输类
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentBuilderFileVo {
    @JsonProperty("id")
    private String id;

    /**
     * kooPageId存出对应的ID
     */
    @JsonProperty("koo_page_id")
    private String kooPageId;

    @JsonProperty("name")
    private String name;

    /**
     * 文件相对路径
     * task_id/文件名
     */
    @JsonProperty("uri")
    private String uri;

    @JsonProperty("url")
    private String url;

    /**
     * 文件内容 Base64编码
     */
    @JsonProperty("content")
    private String content;

    /**
     * 文件下载目录
     */
    @JsonProperty("workspace")
    private String workspace;

    /**
     * 文件来源
     */
    @JsonProperty("source")
    private String source;

    /**
     * 文件来源id
     */
    @JsonProperty("source_id")
    private String sourceId;

    /**
     * 文件存储类型 0：数据库；1：obs
     */
    @JsonProperty("storage_type")
    private int storageType;

    /**
     * 文件是否需要下载，大于4096kb的文件运行时不会返回，需要调用接口下载
     */
    @JsonProperty(value = "need_download", access = JsonProperty.Access.WRITE_ONLY) // 仅在反序列化时使用
    private boolean needDownload;
}