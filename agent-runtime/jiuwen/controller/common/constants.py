#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

FUNCTION_ROLE = "tool"

DEFAULT_USER_PROMPT = "你是一个功能分类器，你可以根据用户的请求提问，和相应的功能类别描述，选择正确的功能帮助用户解决问题。"


class WorkflowConstants:
    """工作流相关常量类

    该类定义了工作流执行过程中使用的各种常量键名，
    包括查询参数、工作流序列、活动工作流等关键标识符。
    """

    # 工作流请求query
    QUERY = "query"
    # 工作流请求参数键
    WORKFLOW_REQ_PARAMS_KEY = "workflow_req_params"
    # 有效工作流序列键
    VALID_WORKFLOW_SEQUENCE_KEY = "valid_workflow_sequence"
    # 活动工作流键
    SEQUENCE_WORKFLOWS_KEY = "workflow_sequence"
    ACTIVE_WORKFLOWS_KEY = "active_workflows"
    START_WORKFLOW_EXECUTED_KEY = "start_workflow_executed"
    START_WORKFLOW_COMPLETED_KEY = "start_workflow_completed"
    CONTROLLER_GLOBAL_VARIABLES_KEY = "controller_global_variables"
    WORKFLOW_FIXED_RESPONSE = "response"
    INTENT = "intent"
    CONVERSATION_ID = "conversation_id"


class IntentionDetectConstants:
    """意图检测相关常量类

    该类定义了意图识别和检测过程中使用的各种常量，
    包括输入参数、提示词、分类信息、聊天历史等关键配置项。
    """

    INPUT = "input"
    USER_PROMPT = "user_prompt"
    CATEGORY_INFO = "category_info"
    CATEGORY_LIST = "category_list"
    DEFAULT_CLASS = "default_class"
    CHAT_HISTORY = "chat_history"
    ENABLE_HISTORY = "enable_history"
    ENABLE_INPUT = "enable_input"
    EXAMPLE_CONTENT = "example_content"
    CHAT_HISTORY_MAX_TURN = "chat_history_max_turn"
    INTENT_DETECTION_TEMPLATE = "intent_detection_template"
    ROLE = "role"
    CONTENT = "content"
    ROLE_MAP = {"user": "用户", "assistant": "助手", "system": "系统"}
    JSON_PARSE_FAIL_REASON = "当前意图识别的输出:'{result}'格式不符合有效的JSON规范，导致解析失败，因此返回默认分类。"
    CLASS_KEY_MISSING_REASON = "当前意图识别的输出 '{result}' 缺少必要的输出'class'分类信息，因此返回默认分类。"
    VALIDATION_FAIL_REASON = "当前意图识别的输出类别 '{intent_class}' 不在预定义的分类列表: '{category_list}'中，因此系统返回默认分类。"
    CLASS = "class"
    REASON = "reason"
    RESULT = "result"
