import json
from copy import deepcopy
from enum import Enum
from typing import Any

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.log.base import logger
from jiuwen.orchestration.flow.components.loop import PRE_DEFINED_FIELDS
from jiuwen.orchestration.flow.constant import (
    USER_FIELDS,
    SYSTEM_FIELDS,
    WORKFLOW_EXECUTE_DEBUG_INFO,
    WORKFLOW_EXECUTE_USER_ID,
    GLOBAL_CONFIG,
    MEM_MAP,
    WORKFLOW_EXECUTE_CONVERSATION_ID,
    MEMORY_VARIABLES,
)
from jiuwen.plugin.common.constant import WORKFLOW_ID
from utils.constants import (
    LONG_TREM_MEMORY_VALUES,
    VALUE_PARSE_MODE,
    IR_LONG_TREM_MEMORY_ENABLE,
    RUNTIME_CONTEXT,
    VALUE_MODEL_PARSE_MODE,
    OUTPUTS,
    REDIS_GLOBAL_VALS_NAME,
)
from utils.status_code import EIStatusCode

MEMORY = "memory"
ASSIGNMENT = "assignment"
ASSIGNMENT_SESSION = "session"
ASSIGNMENT_PERMANENT = "permanent"
OBJECT = "object"
ARRAY = "array"


class DataType(Enum):
    INTEGER = "integer"
    BOOLEAN = "boolean"
    NUMBER = "number"
    STRING = "string"
    OBJECT = "object"
    ARRAY = "array"


def invoke(self, inputs: dict[str, Any], **kwargs):
    type_to_function = {
        DataType.INTEGER: int,
        DataType.BOOLEAN: bool,
        DataType.NUMBER: float,
        DataType.STRING: str,
        DataType.OBJECT: {},
        DataType.ARRAY: [],
    }

    def transform_type(data_type, data_value, data_id=""):
        res = None
        if data_type != "string" and (data_value == "" or data_value is None):
            return None
        # 抛出详细异常
        try:
            # 获取转换函数并转换数据
            conversion_func = type_to_function.get(DataType(data_type))
            if conversion_func:
                return conversion_func(data_value)
        except Exception as e:
            raise JiuWenBaseException(
                error_code=EIStatusCode.GLOBAL_VALS_OBJECT_TYPE_ERROR.code,
                message=f"memory variable {data_id} type transform error: " + str(e),
            )
        return res

    # 将默认值赋给[会话]变量和[永久]变量，其中[永久]变量查看在OBS中是否有值，obs中没有值则赋值给默认值，否则赋值obs中的永久值
    def extract_assignment_values(data, time_type):
        result = {}
        if isinstance(data, list):
            for item in data:
                result.update(extract_assignment_values(item, time_type))
        elif isinstance(data, dict):
            if (
                data.get("storage_method") == "assignment"
                and data.get("aging_level") == time_type
            ):
                if "schema" not in data:
                    # 基本类型
                    if "default_value" in data and "type" in data:
                        result[data["id"]] = transform_type(
                            data["type"].lower(), data["default_value"], data["id"]
                        )
                    else:
                        result[data["id"]] = None
                else:
                    process_object_type(data, result)
            # 递归处理 schema，但不直接更新到 result 中
            if "schema" in data and not data.get("storage_method") == ASSIGNMENT:
                result.update(extract_assignment_values(data["schema"], time_type))
        return result

    def process_object_type(data, result):
        # 数组类型，返回的结果需要时数组类型
        if "type" in data and data["type"].lower() == "array":
            schema_data = data["default_value"]
            if "schema" in data and "schema" in data["schema"]:
                # list类型为非基本类型
                schema_data = (
                    _format_list_by_scheme(
                        json.loads(schema_data), data["schema"]["schema"], data["id"]
                    )
                    if schema_data
                    else []
                )
            else:
                # list类型为基本类型
                schema_data = (
                    _format_simple_list_by_scheme(
                        json.loads(schema_data), data["schema"], data["id"]
                    )
                    if schema_data
                    else []
                )
            result[data["id"]] = schema_data
        else:
            # 对象类型
            result[data["id"]] = (
                _format_object_by_scheme(
                    json.loads(data["default_value"]), data["schema"], data["id"]
                )
                if data["default_value"]
                else {}
            )

    def init_global_var(workflow_id, obj_id, values_define):
        from memory.global_vals_callback_handler import redis_client

        if not (workflow_id and obj_id and values_define and redis_client):
            return {}
        store_key = f"{REDIS_GLOBAL_VALS_NAME}.{workflow_id}.{obj_id}"
        memory_values = redis_client.get(store_key)
        return values_define | (
            {k: v for k, v in json.loads(memory_values).items() if k in values_define}
            if memory_values
            else {}
        )

    # 从redis上获取永久值
    def get_assignment_permanent_values(**params):
        workflow_id = (
            params.get(RUNTIME_CONTEXT, {})
            .get(WORKFLOW_EXECUTE_DEBUG_INFO, {})
            .get(WORKFLOW_ID)
        )
        user_id = (
            params.get(RUNTIME_CONTEXT, {})
            .get(WORKFLOW_EXECUTE_DEBUG_INFO, {})
            .get(WORKFLOW_EXECUTE_USER_ID)
        )
        values_define = extract_assignment_values(
            get_assignment_inputs(), ASSIGNMENT_PERMANENT
        )
        return init_global_var(workflow_id, user_id, values_define)

    # 从redis上获取会话级变量
    def get_assignment_conversion_values(**params):
        workflow_id = (
            params.get(RUNTIME_CONTEXT, {})
            .get(WORKFLOW_EXECUTE_DEBUG_INFO, {})
            .get(WORKFLOW_ID)
        )
        conversation_id = (
            params.get(RUNTIME_CONTEXT, {})
            .get(WORKFLOW_EXECUTE_DEBUG_INFO, {})
            .get(WORKFLOW_EXECUTE_CONVERSATION_ID)
        )
        values_define = extract_assignment_values(
            get_assignment_inputs(), ASSIGNMENT_SESSION
        )
        return init_global_var(workflow_id, conversation_id, values_define)

    # 获取节点赋值的inputs数据
    def get_assignment_inputs():
        if self.conf is None:
            return {}
        if "preDefinedFields" not in self.conf:
            return {}
        if "inputs" not in self.conf["preDefinedFields"]:
            return {}
        return self.conf["preDefinedFields"]["inputs"]

    # 全局变量预处理
    def pre_global_var():
        assignment_inputs = get_assignment_inputs()
        session_vars = extract_assignment_values(assignment_inputs, ASSIGNMENT_SESSION)
        permanent_vars = extract_assignment_values(
            assignment_inputs, ASSIGNMENT_PERMANENT
        )
        return (
            session_vars
            | permanent_vars
            | get_assignment_permanent_values(**kwargs)
            | get_assignment_conversion_values(**kwargs)
        )

    """Invoke to assemble final answer."""
    self._validate_inputs(inputs)
    inputs_copy = _fill_default_values(self, deepcopy(inputs))
    store_memory_vals(self, **kwargs)
    res = dict(
        {
            SYSTEM_FIELDS: {
                "query": inputs_copy.pop("query", ""),
                "dialogueHistory": [],
                "sys": inputs_copy.pop("sys", {}),
                "conversationHistory": inputs_copy.pop("conversationHistory", []),
            },
            USER_FIELDS: fill_dict_default(self.conf, inputs_copy),
            "memory": pre_global_var()
            | _user_input_memory_var(self, inputs_copy)
            | _fill_long_term_memory_values(self, **kwargs)
            | _parent_memory_var(self, **kwargs),
            "permanent_vars": extract_assignment_values(
                get_assignment_inputs(), ASSIGNMENT_PERMANENT
            ),
            "session_vars": extract_assignment_values(
                get_assignment_inputs(), ASSIGNMENT_SESSION
            ),
        }
    )
    return res


def _user_input_memory_var(self, inputs_copy):
    not_vars = ["query", "sys", "conversationId", "userId", "conversationHistory"]
    memory_variables = {}
    for key, value in inputs_copy.items():
        if key not in not_vars:
            memory_variables[key] = value
    return memory_variables


def _parent_memory_var(self, **kwargs):
    """
    如果该工作流为子工作流，尝试从中读取inputs.memory中读取父工作流关联给子工作流的记忆变量输入
    "memory": pre_global_var() | _fill_long_term_memory_values(self, **kwargs) | _parent_memory_var()
    最高优先级覆盖
    Args:
        self: 当前类实例
        kwargs: 包含运行时上下文的字典，至少需要包含:
            - runtime_context: 运行时上下文对象

    Returns:
            dict: 包含从父工作流继承的记忆变量字典，格式为:
        {
            "变量名": 变量值,
            ...
        }
    """

    mem_map = kwargs.get(RUNTIME_CONTEXT).get(MEM_MAP, {})
    memory_variables = kwargs.get(RUNTIME_CONTEXT).get(MEMORY_VARIABLES, {})

    # get当前workflow id ，看看自己有没有要获取的父工作流参数
    workflow_id = (
        kwargs.get(RUNTIME_CONTEXT, {})
        .get(WORKFLOW_EXECUTE_DEBUG_INFO, {})
        .get(WORKFLOW_ID)
    )

    filtered_mem_map = {
        key: value
        for key, value in mem_map.items()
        if isinstance(key, str) and key.startswith(workflow_id)
    }

    # init子工作流记忆变量接收字典
    memory_dict = {}

    # 如果字典为空则该workflow非子工作流, 跳过取值步骤
    if filtered_mem_map:
        # 按照filtered_mem_map从memory_variables中获取对应的值，生成新字典
        new_key_prefix = f"{workflow_id}.memory."

        for mem_key, mem_value in filtered_mem_map.items():
            try:
                # 1. 解析新字典的键 (Key) -> 即子工作流中的变量名
                new_key = mem_key.replace(new_key_prefix, "")

                # 2. 解析源键 (Source Key) -> 即父工作流的变量完整路径
                source_key = mem_value

                # 3. 查找并赋值 (这部分逻辑不变)
                if source_key in memory_variables:
                    new_value = memory_variables[source_key]
                    memory_dict[new_key] = new_value
                else:
                    logger.info(f"警告：在 memory_variables 中未找到键: {source_key}")

            except Exception as e:
                logger.info(f"处理键 {mem_key} 时出错: {e}")

    return memory_dict


def fill_dict_default(config, inputs):
    type_check = {
        "object": (dict, {}),
        "array": (list, []),
    }
    if not config or not config.get("userFields", {}).get("outputs", []):
        return inputs
    for i in config.get("userFields", {}).get("outputs", []):
        name = i.get("id", "")
        val_type = i.get("type", "")
        if val_type in type_check:
            valid_type, valid_def = type_check.get(val_type)
            if not isinstance(inputs.get(name), valid_type):
                inputs[name] = valid_def
    return inputs


def _fill_default_values(self, inputs):
    user_defined_variables = self.conf.get(USER_FIELDS, {}).get("inputs", [])
    default_maps = {
        var["id"]: var["default_value"]
        for var in user_defined_variables
        if "id" in var
        and var.get("default_value") is not None
        and var.get("default_value") != ""
    }

    inputs_copy = default_maps | inputs
    # 单智能体调用工作流工具保存的会话历史，适配为dict
    sys_conversation_history = inputs_copy.get("sys", {}).get("conversationHistory", [])
    conversation_history = inputs_copy.get("conversationHistory", [])
    for idx, item in enumerate(sys_conversation_history):
        if not isinstance(item, dict):
            sys_conversation_history[idx] = item.model_dump()

    for idx, item in enumerate(conversation_history):
        if not isinstance(item, dict):
            conversation_history[idx] = item.model_dump()

    inputs_copy["conversationHistory"] = conversation_history
    inputs_copy["sys"]["conversationHistory"] = sys_conversation_history
    return inputs_copy


desc_to_type = {
    DataType.INTEGER.value: int,
    DataType.BOOLEAN.value: bool,
    DataType.NUMBER.value: (float, int, complex),
    DataType.STRING.value: str,
    DataType.OBJECT.value: dict,
    DataType.ARRAY.value: list,
}


def _format_list_by_scheme(value, scheme, prefix):
    if not isinstance(value, list):
        return [_format_object_by_scheme(value, scheme, prefix)]
    return [_format_object_by_scheme(x, scheme, prefix) for x in value]


# 递归校验读取默认值
def _format_object_by_scheme(value, scheme, prefix):
    if not value:
        return value

    new_value = {}
    for i in scheme:
        current_key = prefix + "." + i.get("id")
        if i.get("id") not in value:
            continue
        if not isinstance(value.get(i.get("id")), desc_to_type.get(i.get("type"))):
            raise JiuWenBaseException(
                error_code=EIStatusCode.GLOBAL_VALS_OBJECT_TYPE_ERROR.code,
                message=EIStatusCode.GLOBAL_VALS_OBJECT_TYPE_ERROR.errmsg.format(
                    key=current_key
                ),
            )

        if i.get("type") == DataType.OBJECT:
            current_data = _format_object_by_scheme(
                value.get(i.get("id")), i.get("schema"), current_key
            )
        elif i.get("type") == DataType.ARRAY:
            current_data = [
                _format_object_by_scheme(x, i.get("schema"), current_key)
                for x in value.get(i.get("id"))
            ]
        else:
            current_data = value.get(i.get("id"))

        new_value[i.get("id")] = current_data
    return new_value


def _format_simple_list_by_scheme(value, scheme, name):
    if not value:
        return value

    new_value = value if isinstance(value, list) else [value]
    for i in new_value:
        if not isinstance(i, desc_to_type.get(scheme.get("type"))):
            raise JiuWenBaseException(
                error_code=EIStatusCode.GLOBAL_VALS_OBJECT_TYPE_ERROR.code,
                message=EIStatusCode.GLOBAL_VALS_OBJECT_TYPE_ERROR.errmsg.format(
                    key=name
                ),
            )
    return new_value


def store_memory_vals(self, **kwargs):
    if (
        not kwargs.get(RUNTIME_CONTEXT, {})
        .get(GLOBAL_CONFIG, {})
        .get(IR_LONG_TREM_MEMORY_ENABLE, False)
    ):
        return
    memory_schema = next(
        (
            x.get("schema")
            for x in self.conf.get(PRE_DEFINED_FIELDS, {}).get(OUTPUTS, [])
            if x.get("id") == "memory"
        ),
        None,
    )
    kwargs[RUNTIME_CONTEXT].get(GLOBAL_CONFIG)[LONG_TREM_MEMORY_VALUES] = (
        [x for x in memory_schema if VALUE_MODEL_PARSE_MODE == x.get(VALUE_PARSE_MODE)]
        if memory_schema
        else []
    )


def _fill_long_term_memory_values(self, **kwargs):
    if (
        not kwargs.get(RUNTIME_CONTEXT, {})
        .get(GLOBAL_CONFIG, {})
        .get(IR_LONG_TREM_MEMORY_ENABLE, False)
    ):
        return {}
    workflow_id = (
        kwargs.get(RUNTIME_CONTEXT, {})
        .get(WORKFLOW_EXECUTE_DEBUG_INFO, {})
        .get(WORKFLOW_ID)
    )
    user_id = (
        kwargs.get(RUNTIME_CONTEXT, {})
        .get(WORKFLOW_EXECUTE_DEBUG_INFO, {})
        .get(WORKFLOW_EXECUTE_USER_ID)
    )
    values_define = {
        x.get("id"): x.get("default_value")
        for x in kwargs.get(RUNTIME_CONTEXT, {})
        .get(GLOBAL_CONFIG, {})
        .get(LONG_TREM_MEMORY_VALUES)
    }
    if not (workflow_id and user_id and values_define):
        return values_define
    from memory.memory_callback_handler import Memory4CallbackHandler

    memory_values = Memory4CallbackHandler.load_exist_data(workflow_id, user_id)
    return values_define | (
        {k: v for k, v in memory_values.items() if k in values_define}
        if memory_values
        else {}
    )
