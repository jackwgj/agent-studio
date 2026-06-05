/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Document extends RagFlowBaseEntity {
    @JsonProperty("id")
    private String id;

    @JsonProperty("thumbnail")
    private String thumbnail; // base64 string

    @JsonProperty("kb_id")
    private String kbId;

    @JsonProperty("parser_id")
    private String parserId;

    @JsonProperty("parser_config")
    private Object parserConfig;

    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("name")
    private String name;

    @JsonProperty("location")
    private String location;

    @JsonProperty("size")
    private Long size;

    @JsonProperty("token_num")
    private Integer tokenNum;

    @JsonProperty("chunk_num")
    private Integer chunkNum;

    @JsonProperty("progress")
    private Float progress;

    @JsonProperty("progress_msg")
    private String progressMsg;

    @JsonProperty("process_begin_at")
    private String processBeginAt;

    @JsonProperty("process_duation")
    private Float processDuration;

    @JsonProperty("meta_fields")
    private Object metaFields;

    @JsonProperty("run")
    private String run = "0";

    @JsonProperty("status")
    private String status = "1";
}
