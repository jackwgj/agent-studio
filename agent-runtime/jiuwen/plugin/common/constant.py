#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Plugin constant"""

import secrets
from datetime import timezone, timedelta
from enum import Enum


def generate_random_float_list_with_secrets(length=768, min_value=0.0, max_value=1.0):
    """generate random float list using secrets"""
    random_float_list = []
    secrets_generator = secrets.SystemRandom()
    for _ in range(length):
        random_float = secrets_generator.uniform(min_value, max_value)
        random_float_list.append(random_float)
    return random_float_list


class AsyncPluginStatus(str, Enum):
    """async plugin status"""

    complete = "completed"
    fail = "fail"


class HttpMethod(str, Enum):
    """http method"""

    get = "get"
    post = "post"
    put = "put"
    head = "head"
    options = "options"
    delete = "delete"
    trace = "trace"
    connect = "connect"


class Status(Enum):
    """status code"""

    # 未公开状态
    private = 1
    # 公开状态
    public = 2


class SearchPolicy(Enum):
    """search policy"""

    SIMILARITY = 1
    PRICE = 2
    RATE = 3


ARGS_MAX_LENGTH = 200000
HTTP_METHOD = {
    "GET",
    "HEAD",
    "POST",
    "PUT",
    "DELETE",
    "CONNECT",
    "OPTIONS",
    "TRACE",
    "PATCH",
}
PLUGIN_DEFAULT_TABLE_NAME = "plugin"
MOCK_QUERY_EMBEDDING_RESULT = generate_random_float_list_with_secrets()
PLUGIN_WORKFLOW_REF_DEFAULT_TABLE_NAME = "plugin_workflow_ref"

# Plugin field name
PLUGIN_ID = "id"
PLUGIN_NAME = "plugin_name"
PLUGIN_DESCRIPTION = "plugin_description"
FUNCTION_NAME = "func_name"
FUNCTION_DESCRIPTION = "func_description"
CALLBACK_NAME = "callable_function"
PLUGIN_URL = "url"
PLUGIN_METHOD = "method"
PLUGIN_HEADERS = "headers"
PLUGIN_RESULT = "result"
PLUGIN_ARGUMENTS = "arguments"
PLUGIN_RELATED_INFO = "related_info"
PLUGIN_USER_ID = "user_id"
PLUGIN_AUTH = "auth"
PLUGIN_DEPENDENCY = "plugin_dependency"

# PLUGIN css field name
SIZE = "size"
OFFSET = "offset"
LIMIT = "limit"
FROM = "from"
SORT = "sort"
SORT_BY = "sortBy"
QUERY = "query"
SOURCE = "_source"

# Plugin Workflow Ref field name
VERSION = "version"
WORKFLOW_ID = "workflow_id"
PROJECT_ID = "project_id"
WORKSPACE_ID = "workspace_id"
TASK_ID = "task_id"
REF_PLUGIN_ID = "plugin_id"
TIMEZONE_NAME = timezone(timedelta(hours=8), "Asia/Shanghai")

# Response field name
CODE = "code"
MESSAGE = "message"
BODY = "body"
ID_LIST = "id_list"
EXTERNAL_ERROR = "external_error"

# RestFul Auth key
AUTH_SCOPE = "scope"
AUTH_SERVICE = "SERVICE"
AUTH_USER = "USER"
AUTH_CRYPT_METHOD = "crypt_method"
# Auth crypt method
AUTH_CRYPT_JIUWEN = "jiuwen"
# RestFul Res
ERR_CODE = "errCode"
ERR_MESSAGE = "errMessage"
RESTFUL_DATA = "data"

# Restful Api field name
COMPONENTS = "components"
SCHEMA = "schema"
SERVERS = "servers"
DEFINITIONS = "definitions"
REQUEST_BODY = "requestBody"
CONTENT = "content"
NAME = "name"
RESPONSES = "responses"
PATHS = "paths"
BASE_PATH = "basePath"
REQUEST_TIME_OUT = 50
MAX_RESULT_SIZE = 10 * 1024 * 1024

PLUGIN_MAX_PERIOD_ROUND = 20
PLUGIN_MAX_TIME_OUT = 7200
PLUGIN_DEFAULT_MAX_TIME_OUT = 600
PLUGIN_DEFAULT_PERIOD_ROUND = 10

RET_CODE = "ret_code"
RESULT = "result"

# pg code
PG_CODE_DICT = {
    "22001": "insert or update data exceeds the length limit of the column",
    "23502": "insert or update a column with a NULL value, while the column is defined as NOT NULL.",
    "23503": "insert or update a foreign key value that does not exist in the referenced table",
    "23505": "try to insert a value that already exists in the column of a unique constraint",
    "42601": "pgsql Syntax error",
    "42883": "undefined function in database",
    "42P01": "column or table not exist",
    "40001": "transaction serialization failed",
    "54000": "internal error of database",
    "08006": "database connect error",
}
