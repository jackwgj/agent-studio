/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.agentbase.common.enums.KnowledgeBaseType;
import com.openjiuwen.studio.agent.agentbase.enums.RepoTypeEnum;
import com.openjiuwen.studio.agent.common.dto.knowledge.ParseConf;
import com.openjiuwen.studio.agent.manager.dto.RagChunkParserConf;
import com.openjiuwen.studio.agent.common.dto.knowledge.SplitConf;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 知识库，包含若干文档
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Accessors(chain = true)
public class KnowledgeRepo {

    private String knowledgeRepoId;

    private String projectId;

    private String displayName;

    private String description;

    private KnowledgeBaseType type;

    private String source;

    private RepoTypeEnum repoType;

    private Long size;

    private Long maxSize;

    private Long remainSize;

    private String creator;

    private String updateUserName;

    private ParseConf parseConf;

    private SplitConf splitConf;

    private RagChunkParserConf ragChunkParserConf;

    private String embeddingModel;

    private String rerankModel;

    private String icon;

    private String iconName;

    private String status;

    private String metadata;

    private Long createTime;

    private Long updateTime;

    private String apiKey;

    private String workspaceId;

}