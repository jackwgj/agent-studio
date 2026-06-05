#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Definition for common structure."""

from typing import Union, TypedDict

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode

_LOC_LENGTH = 1
_LOC_COMPONENTS = "components"


class ValidationException(JiuWenBaseException):
    """Validation error wrappers for anything."""

    Loc = tuple[Union[str, int], ...]

    class Info(TypedDict):
        """Detail about one error. Can be regarded as a normal dict."""

        loc: "ValidationException.Loc"
        msg: str

    def __init__(
        self,
        model: type,
        *errors: Info,
        code=StatusCode.ORCHESTRATION_VALIDATION_FAILED,
        id_dict: dict = None,
    ):
        super().__init__(
            error_code=code.code,
            message=self.__format_msg(model, errors, code.errmsg, id_dict),
        )
        self.__errors = errors

    @property
    def errors(self):
        """Get error details as structured."""
        return self.__errors

    @staticmethod
    def __join_loc(loc: Loc) -> str:
        return "".join(
            ("." + e) if isinstance(e, str) else f"[{e}]" for e in loc
        ).lstrip(".")

    @staticmethod
    def __join_msg(msg: str, id_dict: dict, loc: Loc) -> str:
        pos = None
        if len(loc) > _LOC_LENGTH and loc[0] == _LOC_COMPONENTS:
            pos = loc[1]
        if len(loc) > _LOC_LENGTH + 1 and loc[1] == _LOC_COMPONENTS:
            pos = loc[2]
        if isinstance(pos, int) and str(pos) in id_dict:
            return f"(Component {id_dict.get(str(pos))}) " + msg
        return msg

    @classmethod
    def __format_msg(
        cls, model: type, errors: tuple[Info, ...], template: str, id_dict=None
    ) -> str:
        if not isinstance(id_dict, dict):
            id_dict = {}
        return template.format(
            len=len(errors),
            model=model.__name__,
            s="" if len(errors) <= 1 else "s",
            description=" \n "
            + ";\n ".join(
                f"{cls.__join_loc(e['loc'])}: {cls.__join_msg(e['msg'], id_dict, e['loc'])}"
                for e in errors
            ),
        )
