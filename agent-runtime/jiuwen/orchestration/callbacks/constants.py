#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
from enum import Enum


class TriggerEventName(Enum):
    """
    触发事件名称枚举类。

    定义了组件生命周期中不同触发事件的名称常量。
    """

    ON_INVOKE = "on_invoke"
    """调用事件触发时的名称。"""

    ON_PRE_INVOKE = "on_pre_invoke"
    """调用事件触发前的名称。"""

    ON_POST_INVOKE = "on_post_invoke"
    """调用事件触发后的名称。"""

    ON_POST_INTERACT_INVOKE = "on_post_interact_invoke"
    """交互调用事件触发后的名称。"""


class OnInvokeEventType(Enum):
    """
    On invoke event type. Used to customize the event type when the on_invoke event is triggered.
    """

    # Record child debug info, for example: the calling token of LLM
    RECORD_CHILD_DEBUG_INFO = "RecordChildDebugInfo"
