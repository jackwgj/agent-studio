#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Manager Interface of Invokable `InvokableMgrBase`, and default implement `InvokableManager`."""

__all__ = ("ComponentCatalog", "Value", "InvokableDefinition", "builtin_base_invokable")

import os
from enum import Enum, unique
from typing import Any

from jiuwen.orchestration.common.validation import LanguageTypeChecks, TYPE_SEGMENTS
from pydantic import BaseModel, Field, StrictStr, model_validator

_FILE_PATH_KEY = "filePath"
_LANGUAGE_KEY = "programLanguage"
_META_KEY = "metadata"
_META_MODULE_PATH_KEY = "modulePath"
_META_CLASSNAME_KEY = "className"
_IR_OPTION_KEY = "irOptions"
_ALLOWED_LANGUAGES = ["python", "go", "java"]

_RESOURCE_DIR = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), "../../resource"
)
_DIR_BASE = os.path.realpath(os.path.join(_RESOURCE_DIR, "ir/components/base"))
_DIR_COMPOSITE = os.path.realpath(
    os.path.join(_RESOURCE_DIR, "ir/components/composite")
)


@unique
class ComponentCatalog(str, Enum):
    """Use `COMPOSITE` to identify a composite Invokable."""

    COMPOSITE = "compositeComponent"


class Value(BaseModel):
    """Value description for all kinds of values in Invokable and AgentIR."""

    name: StrictStr = Field(min_length=1, max_length=128)
    schema: Any = None
    label: StrictStr = None
    description: StrictStr = None
    type: StrictStr
    key: StrictStr = None
    value: Any = None
    constraint: dict[str, Any] = None


class InvokableDefinition(BaseModel):
    """Invokable definition used for register and update."""

    invokable_type: StrictStr = Field(alias="type")
    component_catalog: ComponentCatalog = Field(None, alias="componentCatalog")
    label: StrictStr = None
    description: StrictStr = None
    metadata: dict[str, Any]
    inputs: list[Value] = Field(default_factory=list)
    outputs: list[Value] = Field(default_factory=list)
    configs: list[Value] = Field(default_factory=list)
    class_name: StrictStr = Field(None, alias="className")

    @staticmethod
    def __type_seg_to_class_name(seg: TYPE_SEGMENTS, language: str) -> str:
        if language == "python":
            return seg[1]
        if language == "java":
            return (seg[0] + "." + seg[1]) if seg[0] else seg[1]
        if language == "go":
            return (seg[0] + ":" + seg[1]) if seg[0] else seg[1]
        raise ValueError(f"Unknown language {language!r}")

    @classmethod
    def __type_check_v2_for_base(
        cls, lang: str, t: str, metadata: dict[str, Any]
    ) -> TYPE_SEGMENTS:
        """Type checking logic for base Invokable."""
        if (_META_MODULE_PATH_KEY not in metadata) and (
            _META_CLASSNAME_KEY not in metadata
        ):
            return LanguageTypeChecks.validate(t, lang)  # old style
        # new style of type is like: type=any:version, and metadata contains extra key: class and module(optional）
        version = t.rsplit(":", 1)[-1]
        LanguageTypeChecks.validate_version(version)
        module = metadata.get(_META_MODULE_PATH_KEY, "")  # module is optional
        clazz = metadata.get(_META_CLASSNAME_KEY, "")  # class is required for new style
        return module, clazz, version

    @model_validator(mode="after")
    def _type_check_and_generate_class_name(self):
        t: str = self.invokable_type
        if self.component_catalog:
            LanguageTypeChecks.optional_version_style(t, "composite")
            return self
        language: str = self.metadata.get(_LANGUAGE_KEY)
        type_seg = self.__type_check_v2_for_base(language, t, self.metadata)
        cls_name: str = self.class_name
        if not cls_name:
            self.class_name = self.__type_seg_to_class_name(type_seg, language)
        return self


def _load_builtin(
    path: str, usage: str
) -> tuple[tuple[str, list[dict[str, Any]], list[InvokableDefinition]], ...]:
    ret = []
    return tuple(ret)


_BUILTIN_BASE_INVOKABLE = _load_builtin(_DIR_BASE, "base")


def builtin_base_invokable():
    """Get JiuWen builtin basic Invokable."""
    return _BUILTIN_BASE_INVOKABLE
