/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import lombok.Data;

/**
 * Workflow对话返回的chunk
 */
@Data
public class WorkflowChatResp {
    /**
     * workflow_started：工作流开始事件，表示工作流开始运行。
     * workflow_finished：工作流结束事件，表示工作流结束运行。
     * message：消息事件，表示工作流执行过程中流式返回的消息。
     * error：错误事件，表示工作流执行错误信息。
     * end：结束事件，标识请求结束。
     */
    private String event;

    private WorkflowChatDetail data;
}
