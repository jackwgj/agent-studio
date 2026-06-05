#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
check Parammeter
"""

from jiuwen.serve.common.exception.exceptions import ParamCheckFailedException


class CheckInfo:
    """Utility class for validating input parameters."""

    @staticmethod
    def check_str_content(value, field_name):
        """check str content"""
        if not isinstance(value, str):
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! "
                f"value type not correct, expected string"
            )
        if not value:
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! value is empty"
            )
        return value

    @staticmethod
    def check_dict_content(value, field_name):
        """check dict content"""
        if not isinstance(value, dict):
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! "
                f"value type not correct, expected dict"
            )
        if not value:
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! value is empty"
            )
        return value

    @staticmethod
    def check_list_content(value, field_name):
        """check str content"""
        if not isinstance(value, list):
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! "
                f"value type not correct, expected list"
            )
        if not value:
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! value is empty"
            )
        return value

    @staticmethod
    def check_message_list_content(value, field_name):
        """check message list content"""
        if not isinstance(value, list):
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! "
                f"value type not correct, expected list"
            )
        if not value:
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! value is empty"
            )
        if value[-1].get("role", "") != "assistant":
            raise ParamCheckFailedException(
                f"the role of 'assistant' is not in the {field_name} field"
            )
        return value

    @staticmethod
    def check_int_content(value, field_name):
        """check int content"""
        if not isinstance(value, int):
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! "
                f"value type not correct, expected int"
            )
        if not value:
            raise ParamCheckFailedException(
                f"input value field name: {field_name} check failed! value is empty"
            )
        return value
