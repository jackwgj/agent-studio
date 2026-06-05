#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from functools import wraps

from jiuwen.common.log.base import logger
from jiuwen.orchestration.flow.bpmn_workflow import WORKFLOW_ID
from jiuwen.orchestration.flow.constant import (
    WORKFLOW_EXECUTE_DEBUG_INFO,
    WORKFLOW_EXECUTE_CONVERSATION_ID,
    WORKFLOW_EXECUTE_USER_ID,
)
from utils.constants import RUNTIME_CONTEXT, EXECUTION_ID


# 异常装饰器，异步线程里的不抛异常
def handle_exceptions(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        try:
            return func(*args, **kwargs)
        except Exception as e:
            logger.exception(f"[{func.__name__}] error: {str(e)}")
            return None

    return wrapper


def get_runtime_memory_key_ids(**kwargs):
    info = kwargs.get(RUNTIME_CONTEXT, {}).get(WORKFLOW_EXECUTE_DEBUG_INFO, {})
    conversation_id = info.get(WORKFLOW_EXECUTE_CONVERSATION_ID)
    workflow_id = info.get(WORKFLOW_ID)
    user_id = info.get(WORKFLOW_EXECUTE_USER_ID)
    execution_id = info.get(EXECUTION_ID)
    return conversation_id, workflow_id, user_id, execution_id
