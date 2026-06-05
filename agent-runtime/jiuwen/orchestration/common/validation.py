#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""Extra validation rule definition. Used for validating AgentIR and InvokableIR."""

__all__ = ("LanguageTypeChecks", "ID_EXPR", "id_validator", "TYPE_SEGMENTS", "is_expr")

import re
from typing import Callable

ID_EXPR = r"^([A-Za-z0-9-_]{1,128})$"
TYPE_SEGMENTS = tuple[str, str, str]
"""A tuple of [package, class, version]"""


def id_validator(v: str) -> str:
    """Used to validate id of Invokable and AgentIR."""
    if not re.fullmatch(ID_EXPR, v):
        raise ValueError(
            "The ID can contain only ASCII letters, digits, and these two special "
            "characters: Minus(-) and Underline(_), and length should"
            " be in range 1 to 128"
        )
    return v


def is_expr(v: str) -> bool:
    """Judge if `v` contains a structure as `${xxx}`"""
    return ("${" in v) and ("}" in v.split("${")[1])


class LanguageTypeChecks:
    """Language type description check."""

    TYPE_MAX_LEN = 4096
    """Maximum length for a type string."""

    _PY_SYMBOL_EXPR = r"[A-Za-z][A-Za-z0-9_]*"
    _PY_MODULE_EXPR = f"{_PY_SYMBOL_EXPR}(\\.{_PY_SYMBOL_EXPR})*"
    _JAVA_SYMBOL_EXPR = r"[A-Za-z$_][A-Za-z0-9$_]*"
    _JAVA_TYPE_EXPR = f"{_JAVA_SYMBOL_EXPR}(\\.{_JAVA_SYMBOL_EXPR})+"
    _GO_MODULE_SEG_EXPR = r"[A-Za-z][A-Za-z0-9_-]*"
    _GO_MODULE_EXPR = f"{_GO_MODULE_SEG_EXPR}([./]{_GO_MODULE_SEG_EXPR})+"
    _GO_CLASS_NAME = r"[A-Z][A-Za-z0-9_]*"

    @staticmethod
    def validate_version(ver: str, extra_desc=" (last segment of type)") -> None:
        """Validate a string if it meets the format required by version."""
        if not re.fullmatch(r"[A-Za-z0-9-._]+", ver):
            raise ValueError(
                f"The value of version{extra_desc} can contain only letters (ASCII), digits,"
                " and these three special characters: Dot(.), Minus(-) and Underline(_)."
            )

    @staticmethod
    def validate_type(invokable_type: str) -> None:
        """Validate a string if it meets the format required by type."""
        if not re.fullmatch(r"[A-Za-z0-9\-_.:]+", invokable_type):
            raise ValueError(
                "The value of type can contain only letters (ASCII), digits,"
                " and these four special characters: Colon(:), Dot(.), Minus(-) and Underline(_)."
            )

    @classmethod
    def python_style(cls, typename: str, language: str = "python") -> TYPE_SEGMENTS:
        """
        Check typename which should be in format '{full.package.name}:{ClassName}:{version}'
        and symbol not start with Underline.
        """
        if len(typename) > cls.TYPE_MAX_LEN:
            raise ValueError(f"Maximum length of type is {cls.TYPE_MAX_LEN}.")
        split = typename.split(":")
        if (
            (len(split) != 3)
            or (not re.fullmatch(cls._PY_MODULE_EXPR, split[0]))
            or not re.fullmatch(cls._PY_SYMBOL_EXPR, split[1])
        ):
            raise ValueError(
                f"Type of {language} must be a three-segment structure separated by colons ':'"
                " in the format of 'package_and_file_name:ClassName:Version', every segment"
                " of package_and_file_name and ClassName cannot start with Underline(_),"
                " and blank characters are not allowed."
            )
        cls.validate_version(split[2])
        return tuple(split)  # type: ignore

    @classmethod
    def golang_style(cls, typename: str, language: str = "go") -> None:
        """Check typename which is like golang style '{module[.any]/package/name}:{ClassName}:{version}'."""
        if len(typename) > cls.TYPE_MAX_LEN:
            raise ValueError(f"Maximum length of type is {cls.TYPE_MAX_LEN}.")
        split = typename.split(":")
        if (
            (len(split) != 3)
            or (not re.fullmatch(cls._GO_MODULE_EXPR, split[0]))
            or not re.fullmatch(cls._GO_CLASS_NAME, split[1])
        ):
            raise ValueError(
                f"Type of {language} must be a three-segment structure separated by colons ':'"
                " in the format of 'module.name/package/name:ClassName:Version',"
                " and blank characters are not allowed."
            )
        cls.validate_version(split[2])

    @classmethod
    def java_style(cls, typename: str, language: str = "java") -> TYPE_SEGMENTS:
        """Check typename which is like java style '{package.name}.{ClassName}[:{version}]'."""
        if len(typename) > cls.TYPE_MAX_LEN:
            raise ValueError(f"Maximum length of type is {cls.TYPE_MAX_LEN}.")
        split = typename.split(":")
        ve = ValueError(
            f"Type of {language} must be a two-segment structure separated by colons ':'"
            " in the format of 'package.ClassName:Version' or 'package.ClassName',"
            " and blank characters are not allowed."
        )
        ver = ""
        if len(split) not in (1, 2):
            raise ve
        if not re.fullmatch(cls._JAVA_TYPE_EXPR, split[0]):
            raise ve
        if len(split) == 2:
            ver = split[1]
            cls.validate_version(ver)
        return tuple(split[0].rsplit(".", 1) + [ver])  # type: ignore

    @classmethod
    def optional_version_style(cls, typename: str, language: str) -> TYPE_SEGMENTS:
        """Check typename which is '{anything}:{version}' or '{anything}'."""
        if len(typename) > cls.TYPE_MAX_LEN:
            raise ValueError(f"Maximum length of type is {cls.TYPE_MAX_LEN}.")
        split = typename.rsplit(":", 1)
        if not split[0]:
            raise ValueError(f"Type of {language} cannot be empty.")
        if len(split) == 2:
            cls.validate_version(split[1])
            return "", split[0], split[1]
        return "", typename, ""

    @classmethod
    def validate(cls, typename: str, language: str) -> TYPE_SEGMENTS:
        """Pick a validator and then validate the type of Invokable by the language specified in metadata.

        Parse `typename` to a tuple of [package, class, version] depends on `language`.
        If origin_type is illegal (such as it has an empty version), or no available validator, raise a ValueError.

        Args:
            typename: the origin type from json.
            language: the Invokable program language.

        Returns:
            A tuple of [package, class, version]. If any segment allows empty, it will be filled with an empty string.
        """
        v = _LANGUAGE_VALIDATOR_MAP.get(language)
        if not v:
            raise ValueError(f"Program language {language!r} not supported.")
        return v(typename, language)


_LANGUAGE_VALIDATOR_MAP: dict[str, Callable[[str, str], TYPE_SEGMENTS]] = dict(
    python=LanguageTypeChecks.python_style,
    java=LanguageTypeChecks.java_style,
    go=LanguageTypeChecks.optional_version_style,
)
