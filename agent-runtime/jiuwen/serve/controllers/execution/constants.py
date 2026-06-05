#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""This module defines some constants used in the execution module."""

from datetime import timezone, timedelta

DEFAULT_SUPPORTED_IR_VERSIONS = '["0.6.0"]'

DEFAULT_TTL = 3 * 24 * 60 * 60

# conversation_id的长度限制
MIN_CONVERSATION_ID_LENGTH = 1

# 用户请求的长度限制
MIN_QUERY_LENGTH = 1

# 历史消息中消息角色的长度限制
MIN_MESSAGE_ROLE_LENGTH = 1

# 历史消息中内容的长度限制
MIN_CONTENT_LENGTH = 1

# ir_path的长度限制
MIN_IR_PATH_LENGTH = 1

# plugin_id的长度限制
MIN_PLUGIN_ID_LENGTH = 1


tzinfo = timezone(timedelta(hours=8.0))
