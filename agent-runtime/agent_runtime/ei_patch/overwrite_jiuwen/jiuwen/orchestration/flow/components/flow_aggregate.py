#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
import json
import os
from typing import Any

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.constant import USER_FIELDS, TYPE_DEFAULT_VALUE_DICT
from pydantic import BaseModel, Field

RECORD_AGGREGATE_DETAIL_LOG = (
    "true" == os.getenv("RECORD_AGGREGATE_DETAIL_LOG", "false").lower()
)


class GroupConfig(BaseModel):
    id: str
    value_list: list[str]


class AggregateConfig(BaseModel):
    """
    变量聚合组件 配置校验
    """

    mode: str = Field(default="first-non-null", alias="mode")
    groups: list[GroupConfig]


class FlowAggregate(Invokable):
    """Aggregate node."""

    def __init__(self, conf: dict):
        self.conf = conf

    def first_non_empty(self, lst: list, k_type):
        """获取第一个非空元素："""
        empty_values = [None, [], {}, ""]
        for item in lst:
            if item not in empty_values:
                return item
        res = TYPE_DEFAULT_VALUE_DICT.get(k_type, None)
        return res

    def validate_param_type(self, lst: list, k_type):
        """判断数据类型是否相等"""
        type_flag = self.first_non_empty(lst, k_type)
        if type_flag is None:
            return
        if isinstance(type_flag, dict):
            self.validate_dic_type(type_flag, lst)
            return
        for item in lst:
            if item and not isinstance(item, type(type_flag)):
                raise JiuWenBaseException(
                    error_code=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.code,
                    message=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.errmsg,
                )

    def validate_dic_type(self, type_flag: dict, lst: list):
        """对于字典类型的判断内置数据类型是否相等"""
        dic_flag_key = type_flag.keys()
        for item in lst:
            if item is None:
                continue
            for key in dic_flag_key:
                if key not in item:
                    raise JiuWenBaseException(
                        error_code=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.code,
                        message=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.errmsg,
                    )
                if (
                    item[key]
                    and type_flag[key]
                    and not isinstance(item[key], type(type_flag[key]))
                ):
                    raise JiuWenBaseException(
                        error_code=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.code,
                        message=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.errmsg,
                    )

    def get_group_id_type(self, idname):
        res = "string"
        try:
            outputlist = self.conf.get("userFields", {}).get("outputs", [])
            for item in outputlist:
                if item["id"] == idname:
                    res = item["type"]
        except Exception:
            raise JiuWenBaseException(
                error_code=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.code,
                message=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.errmsg,
            )
        return res

    def invoke(self, inputs: dict[str, Any], **kwargs):
        """Invoke to assemble final answer."""
        mode = self.conf.get("mode", "first-non-null")
        groups = self.conf.get("groups", {})
        inputs = inputs.get(USER_FIELDS, {})

        res = {}
        if mode == "first-non-null":
            for k, item_list in groups.items():
                value_list = [inputs.get(item, None) for item in item_list]
                k_type = self.get_group_id_type(k)
                self.validate_param_type(value_list, k_type)
                res.update({k: self.first_non_empty(value_list, k_type)})
        else:
            logger.error(
                f"Aggregate node mode is {mode}, inputs: {json.dumps(inputs, ensure_ascii=False)} "
            )
            raise JiuWenBaseException(
                error_code=StatusCode.WORKFLOW_INPUT_FIELD_EMPTY_ERROR.code,
                message=StatusCode.WORKFLOW_INPUT_FIELD_EMPTY_ERROR.errmsg.format(
                    agg_strategy=mode
                ),
            )

        if RECORD_AGGREGATE_DETAIL_LOG:
            logger.info(
                f"Aggregate node detail. inputs: {json.dumps(inputs, ensure_ascii=False)} outputs:{json.dumps(res, ensure_ascii=False)}"
            )
        return {USER_FIELDS: res}
