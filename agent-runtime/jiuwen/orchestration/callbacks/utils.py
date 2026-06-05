#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

import uuid


def generate_tracer_id():
    """
    生成追踪ID，即执行ID。

    该函数用于生成一个唯一的UUID作为追踪ID，用于跟踪和标识执行流程。

    Returns:
        str: 返回一个字符串形式的UUID。
    """
    return str(uuid.uuid4())
