#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
from typing import Any

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.flow.constant import USER_FIELDS
from pydantic import BaseModel, Field


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

    def first_non_empty(self, lst: list):
        """获取第一个非空元素：
        若是字符串，获取第一个非空的字符串；
        其它类型，获取第一个非 None 的元素。
        """
        if not lst:
            return None
        if not isinstance(lst[0], str):
            return next((x for x in lst if x is not None), None)
        return next((x for x in lst if x), None)

    def validate_param_type(self, lst: list):
        """判断数据类型是否相等"""
        if not lst:
            return
        if isinstance(lst[0], dict):
            self.validate_dic_type(lst)
            return
        for item in lst:
            if not isinstance(item, type(lst[0])):
                raise JiuWenBaseException(
                    error_code=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.code,
                    message=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.errmsg,
                )

    def validate_dic_type(self, lst: list):
        """对于字典类型的判断内置数据类型是否相等"""
        dict_flag = lst[0]
        dic_flag_key = dict_flag.keys()
        for item in lst:
            for key in dic_flag_key:
                if key not in item or not isinstance(item[key], type(dict_flag[key])):
                    raise JiuWenBaseException(
                        error_code=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.code,
                        message=StatusCode.WORKFLOW_AGGREGATE_TYPE_ERROR.errmsg,
                    )

    def invoke(self, inputs: dict[str, Any], **kwargs):
        """Invoke to assemble final answer."""
        mode = self.conf.get("mode", "first-non-null")
        groups = self.conf.get("groups", {})
        inputs = inputs.get(USER_FIELDS, {})

        res = {}
        if mode == "first-non-null":
            for k, item_list in groups.items():
                value_list = [inputs.get(item, None) for item in item_list]
                self.validate_param_type(value_list)
                res.update({k: self.first_non_empty(value_list)})
        else:
            raise JiuWenBaseException(
                error_code=StatusCode.WORKFLOW_INPUT_FIELD_EMPTY_ERROR.code,
                message=StatusCode.WORKFLOW_INPUT_FIELD_EMPTY_ERROR.errmsg.format(
                    agg_strategy=mode
                ),
            )
        return {USER_FIELDS: res}
