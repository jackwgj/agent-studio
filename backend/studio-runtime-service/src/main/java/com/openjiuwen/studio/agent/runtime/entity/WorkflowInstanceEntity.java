/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowInstanceEntity {

    private String id;

    private String externalId;

    private String conversationId;

    private String workflowId;

    private String projectId;

    private Object inputs;

    private Object outputs;

    private String status;

    private String runInfo;

    private Long startTime;

    private Long endTime;

    private String errorInfo;

    private List<JiuwenEvent> eventList;
}
