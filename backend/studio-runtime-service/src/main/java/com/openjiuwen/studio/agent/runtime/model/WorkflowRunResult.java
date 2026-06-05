/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import com.openjiuwen.studio.agent.common.dto.AgentInvokeInfo;
import com.openjiuwen.studio.agent.runtime.dto.KnowledgeBaseFileInfo;
import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.runtime.dto.NodeMessage;
import com.openjiuwen.studio.agent.common.dto.agent.NodeRunInfo;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunInfo;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunStreamRsp;
import com.openjiuwen.studio.agent.runtime.entity.WorkflowInstanceEntity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WorkflowRunResult {
    private WorkflowInstanceEntity instance;

    private List<NodeRunInfo> nodeRunInfoList = new ArrayList<>();

    private WorkflowRunInfo workflowRunInfo;

    // 保存助手返回信息
    private List<NodeMessage> messageList = new ArrayList<>();

    // 保存单智能体返回的消息体
    private List<Message> agentMessageList = new ArrayList<>();

    // 保存单智能体调用链路
    private List<AgentInvokeInfo> invokeList = new ArrayList<>();

    // 保存知识检索结果中文档相关信息
    private List<KnowledgeBaseFileInfo> knowledgeBaseFileInfoList = new ArrayList<>();

    /**
     * 九问引擎事件（透传data）
     */
    private List<WorkflowRunStreamRsp> eventList = new ArrayList<>();

    private boolean taskEnd;

    private boolean workflowEnd;
}
