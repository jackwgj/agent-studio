/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.agent.NodeRunInfo;
import com.openjiuwen.studio.agent.common.dto.agent.Status;

import lombok.Builder;
import lombok.Data;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流执行接口响应体
 *
 */
@Validated
@Data
@Builder
public class WorkflowRunRsp implements Serializable {

    // 静态代码检查G.SER.02：实现Serializable的类应显式声明serialVersionUID，避免类变更后反序列化兼容性问题
    private static final long serialVersionUID = 1L;

    @JsonProperty("conversation_id")
    private String conversationId;

    @Builder.Default
    private Map<String, Object> outputs = new HashMap<>();

    private List<NodeMessage> messages;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    private Map<String, Object> metadata;

    private Status status;

    @JsonProperty("start_time")
    private Long startTime;

    @JsonProperty("end_time")
    private Long endTime;

    @JsonProperty("knowledge_base_files_info")
    private List<KnowledgeBaseFileInfo> knowledgeBaseFileInfoList;

    @JsonProperty("node_info")
    @Builder.Default
    private List<NodeRunInfo> nodeInfo = new ArrayList<>();

    private List<WorkflowRunStreamRsp> events;
}
