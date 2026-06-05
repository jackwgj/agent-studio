/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.enums;

/**
 * 九问流式接口Event取值枚举值
 *
 */
public enum JiuwenEventType {
    START,
    WORKFLOW_START,
    MESSAGE,
    MESSAGE_END,
    WORKFLOW_END,
    ERROR,
    EXCEPTION,
    FUNCTION_CALL_END,
    FUNCTION_CALL,
    API_EXEC_DATA,
    STATISTIC_DATA,
    SUMMARY_RESPONSE,
    DONE,
    WORKFLOW_NODE_MESSAGE,
    AGENT_NODE_MESSAGE,
    INTERMEDIATE_MESSAGE,
    WORKFLOW_BLOCKED,
    WORKFLOW_RESUME,
    TASK_TERMINATED,
    NOT_EXIST,
    TASK_START,
    TASK_END,
    WAITING_USER_INPUT,
    HEARTBEAT,
    AGENT_INTERRUPTED,
    SCENE_MATCH,
    PLAN_END,
    STEP_START,
    STEP_END,
    TASK_COMPLETE,
    PLAN_START,
    INTENT
}
