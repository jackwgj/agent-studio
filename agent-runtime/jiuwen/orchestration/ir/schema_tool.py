# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Convert and generate schema dict for special nodes (currently is start/end)."""

from copy import deepcopy
from typing import Any, Union

from pydantic import BaseModel, Extra, Field, ConfigDict

_STR = "String"
_JSON = "json"
_VAL = "value"
_SEP = ", "
_CONF = "configs"

_END_EXEC_MOD = "executeMode"
_END_RESP_MOD = "responseMode"
_END_RESP_CONTENT = "responseContent"
_END_OUT_PARAM = "outputParameters"
_END_SRC = "sourceType"
_END_REF = "reference"


class _EndConf(BaseModel):
    model_config = ConfigDict()
    model_config["extra"] = Extra.forbid

    exec_mode: Union[str, None] = Field(None, alias=_END_EXEC_MOD)
    resp_mode: Union[str, None] = Field(None, alias=_END_RESP_MOD)
    resp_content: Union[str, None] = Field(None, alias=_END_RESP_CONTENT)
    out_params: dict[str, dict[str, str]] = Field(
        default_factory=dict, alias=_END_OUT_PARAM
    )


def _end_schema(
    exec_mode: str = None,
    resp_mode: str = None,
    resp_content: str = None,
    output_params: dict[str, dict[str, str]] = None,
) -> dict:
    """generate metadata/inputs/outputs/configs"""
    inputs = [
        dict(
            name=k,
            type=_JSON,
            value=v.get(_VAL) if (v.get(_END_SRC) == _END_REF) else None,
        )
        for k, v in (output_params or {}).items()
    ]
    output_list = [
        dict(name=n, type=_STR) for n in ("responseMode", "responseContent")
    ] + [
        dict(
            name=_END_OUT_PARAM,
            type="Map<String, T>",
            description="Output parameter dict with selected parameters in configs.",
        ),
        dict(name="result", type="String", value=""),
    ]
    return dict(
        metadata=dict(
            modulePath="jiuwen.orchestration.invokable.end",
            className="EndComponent",
            programLanguage="python",
            filePath="jiuwen/orchestration/invokable/end.pyc",
        ),
        inputs=inputs,
        configs=[
            dict(
                name=_END_EXEC_MOD,
                type=_STR,
                value=exec_mode,
                description='Graph execute mode, only allows "barch" (default) and "stream".',
            ),
            dict(
                name=_END_RESP_MOD,
                type=_STR,
                value=resp_mode,
                description='Response mode, only allows "directResponse" (default).',
            ),
            dict(
                name=_END_RESP_CONTENT,
                type=_STR,
                value=resp_content,
                description='Available while responseMode is "directResponse".',
            ),
            dict(
                name=_END_OUT_PARAM,
                type="Map<String, Map<String, String>>",
                value=output_params,
                description="Output parameter dict. A map from name to source type "
                '("reference" or "constant") and value.',
            ),
        ],
        outputs=output_list,
    )


def gen_end_schema(instance: dict[str, Any]) -> dict[str, Any]:
    """Generate and returns the end node schema.

    `instance` should be validated as a component instance.
    """
    instance = deepcopy(instance)
    configs = _EndConf.model_validate(instance.get(_CONF) or {})
    end = _end_schema(
        exec_mode=configs.exec_mode,
        resp_mode=configs.resp_mode,
        resp_content=configs.resp_content,
        output_params=configs.out_params,
    )
    instance.update(end)
    instance.setdefault("description", "End Node Invokable for workflow mode.")
    instance.setdefault("label", "End Node")
    return instance
