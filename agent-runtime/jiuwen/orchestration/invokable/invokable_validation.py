#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
"""conversation and plan message definition"""

__all__ = (
    "ValueV2",
    "ValueList",
    "ClassInfo",
    "ComponentSchema",
    "ComponentSchemaList",
)

from enum import Enum
from typing import Any, List, Union

from jiuwen.orchestration.common.validation import (
    LanguageTypeChecks,
    is_expr,
    id_validator,
)
from pydantic import (
    field_validator,
    model_validator,
    BaseModel,
    RootModel,
    Field,
    StrictStr,
    ValidationInfo,
    ValidationError,
)
from pydantic_core import InitErrorDetails

_ALLOWED_LANGUAGES = ["python", "go", "java"]  # 允许的编程语言


class ComponentCatalog(str, Enum):
    """Use `COMPOSITE` to identify a composite Invokable."""

    COMPOSITE = "compositeComponent"


class IrInfo(BaseModel):
    """A reference information to an AgentIR for composite Invokable."""

    id: str
    version: str
    default: bool = None  # as False

    @field_validator("id")  # noqa: pydantic.field_validator accepts a classmethod
    @classmethod
    def __check_id(cls, v):
        return v if is_expr(v) else id_validator(v)

    @field_validator("version")  # noqa: pydantic.field_validator accepts a classmethod
    @classmethod
    def __check_version(cls, v):
        if not is_expr(v):
            LanguageTypeChecks.validate_version(v)
        return v


class ValueV2(BaseModel):
    """Value description for all kinds of values in Invokable and AgentIR."""

    id: StrictStr = Field(min_length=1, max_length=128)
    description: StrictStr = None
    schema: Any = None
    type: StrictStr
    key: StrictStr = None
    value: Any = None
    constraint: dict[str, Any] = None


class ValueList(RootModel):
    """A list of Value objects intended for validating the legality of object attribute values."""

    root: List[ValueV2]


_K_CLS_LANG = "programLanguage"
_K_CLS_PATH = "filePath"
_K_CLS_NAME = "className"
_K_CLS_MOD = "modulePath"
_K_CLS_OPT = "irOptions"


class _UseIrOptions:
    """To simulate a model named 'ClassInfo' that contains a list of IR options."""

    class ClassInfo(BaseModel):
        """The class info for composite components which use IR options."""

        options: list[IrInfo] = Field(
            default_factory=list, alias=_K_CLS_OPT, min_length=1
        )


class _UseClassDef:
    """To simulate a model named 'ClassInfo' that contains 3 or 4 fields about a class."""

    class ClassInfo(BaseModel):
        """The class info for components which annotates a class."""

        program_language: StrictStr = Field(alias=_K_CLS_LANG)
        path: StrictStr = Field(alias=_K_CLS_PATH)
        classname: StrictStr = Field(alias=_K_CLS_NAME)
        # these 3 fields preceding are optional only while a composite component has `irOptions`

        module_path: StrictStr = Field(None, alias=_K_CLS_MOD)

        # this field is optional only while language is 'java'

        @model_validator(mode="wrap")  # noqa: pydantic.model_validator accepts a classmethod
        @classmethod
        def __check_module_path(cls, v: Any, handler):
            if not (isinstance(v, (dict, cls)) and (_K_CLS_LANG in v)):
                return handler(v)  # raise error by pydantic
            language = v[_K_CLS_LANG]
            has_mod = (_K_CLS_MOD in v) and (v[_K_CLS_MOD] != "")
            errmsg, loc = None, _K_CLS_MOD
            # modulePath is required by go/python and should be empty for java
            if isinstance(language, str):
                if language not in _ALLOWED_LANGUAGES:
                    errmsg, loc = (
                        f"must be one of the following: {_ALLOWED_LANGUAGES}",
                        _K_CLS_LANG,
                    )
                elif language in ("python", "go") and (not has_mod):
                    errmsg = f"the key '{_K_CLS_MOD}' must exist when '{_K_CLS_LANG}' is {language}"
                elif language == "java" and has_mod:
                    errmsg = f"the key '{_K_CLS_MOD}' must not exist when '{_K_CLS_LANG}' is {language}"
            errors = (
                []
                if not errmsg
                else [
                    InitErrorDetails(
                        type="value_error",
                        loc=(loc,),
                        input=None,
                        ctx=dict(error=errmsg),
                    )
                ]
            )
            # get type error by catch pydantic exception
            ret = None
            try:
                ret = handler(v)
            except ValidationError as e:
                e: ValidationError
                errors.extend(e.errors())  # type: ignore # ErrorDetails is compatible with InitErrorDetails
            if errors or (ret is None):
                raise ValidationError.from_exception_data("ClassInfo", errors)
            return ret


class ClassInfo(BaseModel):
    """Necessary fields depends on whether this is a composite component, check them in `ComponentInstance`."""

    program_language: Union[StrictStr, None] = Field(None, alias=_K_CLS_LANG)
    path: Union[StrictStr, None] = Field(None, alias=_K_CLS_PATH)
    classname: Union[StrictStr, None] = Field(None, alias=_K_CLS_NAME)
    # these 3 fields preceding are optional only while a composite component has `irOptions`

    module_path: Union[StrictStr, None] = Field(None, alias=_K_CLS_MOD)
    # this field is optional only while language is 'java'

    options: Union[list[IrInfo], None] = Field(None, alias=_K_CLS_OPT)


class ComponentSchema(BaseModel):
    """New invokable version definition used for register and update."""

    schema_version: StrictStr = Field(alias="schemaVersion", min_length=1)
    invokable_type: StrictStr = Field(alias="type", min_length=1, max_length=128)
    invokable_version: StrictStr = Field(alias="version")
    component_catalog: ComponentCatalog = Field(None, alias="componentCatalog")
    description: StrictStr = None
    class_info: ClassInfo = Field(alias="classInfo")
    inputs: list[ValueV2] = Field(default_factory=list)
    outputs: list[ValueV2] = Field(default_factory=list)
    configs: list[ValueV2] = Field(default_factory=list)

    @field_validator("inputs", "outputs", "configs")  # noqa: pydantic.validator accepts a classmethod
    @classmethod
    def __check_values(cls, v):
        names = set()
        for item in v:
            if item.id in names:
                raise ValueError("Duplicate values are not allowed for the 'id' field")
            names.add(item.id)
        return v

    @field_validator("invokable_version")  # noqa: pydantic.field_validator accepts a classmethod
    @classmethod
    def __check_version(cls, v):
        LanguageTypeChecks.validate_version(v)
        return v

    @field_validator("invokable_type")  # noqa: pydantic.field_validator accepts a classmethod
    @classmethod
    def __check_type(cls, v):
        LanguageTypeChecks.validate_type(v)
        return v

    @field_validator("class_info", mode="plain")  # noqa: pydantic.field_validator accepts a classmethod
    @classmethod
    def _check_class_info(cls, v: Any, info: ValidationInfo):
        if isinstance(v, BaseModel):
            v = v.model_dump(by_alias=True, exclude_none=True)
        if not isinstance(v, dict):
            raise ValidationError.from_exception_data(
                "ClassInfo",
                [
                    InitErrorDetails(
                        type="model_type",
                        loc=(),
                        input=v,
                        ctx=dict(class_name="ClassInfo"),
                    )
                ],
            )
        catalog: ComponentCatalog = info.data.get("component_catalog")
        if isinstance(catalog, ComponentCatalog) and ((_K_CLS_OPT in v) or not v):
            return ClassInfo(
                irOptions=_UseIrOptions.ClassInfo.model_validate(v).options
            )
        # manual validate. Required fields
        validated = _UseClassDef.ClassInfo.model_validate(v)
        return ClassInfo(
            programLanguage=validated.program_language,
            filePath=validated.path,
            className=validated.classname,
            modulePath=validated.module_path,
        )


class ComponentSchemaList(RootModel):  # 组件注册接口
    """A list of Component definition."""

    root: list[ComponentSchema]
