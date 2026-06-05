/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.Data;

@Data
public class KnowledgeFileRef {

    @JSONField(name = "knowledge_base_id")
    private String knowledgeBaseId;

    @JSONField(name = "file_id")
    private String fileId;

    @JSONField(name = "type")
    private String type;

    @JSONField(name = "document_name")
    private String documentName;

    @JSONField(name = "knowledge_base_type")
    private String knowledgeBaseType;
}
