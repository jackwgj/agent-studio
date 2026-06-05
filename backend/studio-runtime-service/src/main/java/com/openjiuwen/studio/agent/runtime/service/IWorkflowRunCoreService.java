/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.runtime.model.Conversation;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.WorkflowRunResult;

/**
 * 工作流核心运行接口
 *
 */
public interface IWorkflowRunCoreService {
    /**
     * 运行工作流
     *
     * @param executeParams 运行参数
     * @param conversation 历史会话
     * @return 返回运行结果
     */
    WorkflowRunResult runWorkflow(ExecuteParams executeParams, Conversation conversation);
}
