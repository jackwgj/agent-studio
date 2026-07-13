#!/usr/bin/env python
# -*- coding:utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""File-related util functions"""

import os
import re

# 安全文件名正则：仅允许字母、数字、下划线、连字符、点
_SAFE_FILENAME_PATTERN = re.compile(r"^[a-zA-Z0-9_\-\.]+$")


def validate_path_traversal(path: str) -> str:
    """校验路径是否包含路径遍历攻击字符（如 ../）。

    用于对来自环境变量、配置文件等不可信数据源的路径进行基本校验。

    Args:
        path: 待校验的路径字符串

    Returns:
        校验通过后的路径字符串

    Raises:
        ValueError: 当路径包含 ``..`` 路径遍历字符时抛出
    """
    if not path:
        raise ValueError("Path is empty")
    if ".." in path:
        raise ValueError(f"Path traversal detected: {path}")
    return path


def safe_resolve_path(untrusted_path: str, allowed_root: str) -> str:
    """对不可信路径进行规范化并校验是否在允许的根目录范围内。

    使用 ``os.path.realpath`` 解析符号链接和相对路径后，
    校验结果是否位于 ``allowed_root`` 目录下。

    Args:
        untrusted_path: 不可信的路径字符串（通常来自环境变量或外部输入）
        allowed_root: 允许的根目录

    Returns:
        规范化后的真实路径字符串

    Raises:
        ValueError: 当解析后的路径不在允许的根目录范围内时抛出
    """
    real_path = os.path.realpath(untrusted_path)
    allowed_real = os.path.realpath(allowed_root)
    if real_path != allowed_real and not real_path.startswith(
        allowed_real + os.sep
    ):
        raise ValueError(
            f"Path '{untrusted_path}' is outside of allowed root '{allowed_root}'"
        )
    return real_path


def validate_filename(filename: str) -> str:
    """校验文件名是否安全，防止路径遍历。

    检查文件名是否只包含安全字符，且不包含 ``..``、``/``、``\\`` 等路径分隔符。

    Args:
        filename: 待校验的文件名

    Returns:
        校验通过后的文件名

    Raises:
        ValueError: 当文件名不安全时抛出
    """
    if not filename or not _SAFE_FILENAME_PATTERN.match(filename):
        raise ValueError(f"Invalid filename: {filename}")
    if ".." in filename or "/" in filename or "\\" in filename:
        raise ValueError(f"Path traversal detected in filename: {filename}")
    return filename


def read_file(file_path):
    """
    Reads the file content.
    If the file is readable, the file content is returned.
    Otherwise, an error message is returned.
    """
    try:
        if os.access(file_path, os.R_OK):
            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    text = f.read()
                    return True, text
            except Exception:
                return False, "read file error"
        else:
            return False, "cannot read the file"
    except Exception:
        return False, "Unknown exception occurred while read file"
