# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""common"""

import importlib.util
import inspect
import os
import re
from datetime import datetime, timezone, timedelta
from typing import List

from jiuwen.common.log.base import logger

DEFAULT_ACTION_INPUT = ["context", "config"]
BUILTIN_ACTIONS_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "prebuilt_actions"
)
BUILTIN_ACTIONS_MODULE = "jiuwen.planner.rails.prebuilt_actions"


def get_current_date():
    """get current date"""
    # 创建一个 tzinfo 对象,表示东八区(北京时区)
    tz_beijing = timezone(timedelta(hours=8))
    # 获取当前 UTC 时间
    now_utc = datetime.now(timezone.utc)
    # 将 UTC 时间转换为北京时间
    today = now_utc.astimezone(tz_beijing)
    logger.info(f"get current date is {today}")
    # 转换格式
    return dict(year=today.year, month=today.month, day=today.day)


def _import_all_modules_from_directory(directory, parent_module):
    for entry in os.listdir(directory):
        entry_path = os.path.join(directory, entry)
        if os.path.isdir(entry_path):
            _import_all_modules_from_directory(entry_path, f"{parent_module}.{entry}")
        elif (
            entry.endswith(".py") or entry.endswith(".pyc")
        ) and entry != "__init__.py":
            module_name = os.path.splitext(entry)[0]
            try:
                # 动态导入模块
                real_module_name = f"{parent_module}.{module_name}"
                if "__pycache__" in real_module_name:
                    continue
                module = importlib.import_module(real_module_name)
                logger.info(f"Loading imported path: {type(real_module_name)}")
                # 动态导入模块
                __dynamic_call_wrapped_action(module)
            except ModuleNotFoundError as e:
                logger.error(
                    f"Failed to load builtin module path: {e}",
                    simple_log="Failed to load builtin module path",
                )


def import_all_modules_from_default_directory():
    """import all modules from default directory"""
    for filename in os.listdir(BUILTIN_ACTIONS_DIR):
        if (
            filename.endswith(".py") or filename.endswith(".pyc")
        ) and filename != "__init__.py":
            module_name = os.path.splitext(os.path.basename(filename))[0]
            try:
                # 动态导入模块
                real_module_name = f"{BUILTIN_ACTIONS_MODULE}.{module_name}"
                if "__pycache__" in real_module_name:
                    continue
                module = importlib.import_module(real_module_name)
                logger.info(f"Loading imported path: {real_module_name}")
                # 动态导入模块
                __dynamic_call_wrapped_action(module)
            except ModuleNotFoundError as e:
                logger.error(
                    f"Failed to load builtin module path: {e}",
                    simple_log="Failed to load builtin module path",
                )
    _import_all_modules_from_directory(BUILTIN_ACTIONS_DIR, BUILTIN_ACTIONS_MODULE)


def __dynamic_call_wrapped_action(module):
    for name, obj in inspect.getmembers(module, inspect.isfunction):
        if hasattr(obj, "__wrapped__"):  # 检查是否被装饰
            # 获取函数对象
            func = getattr(module, name, None)
            # 检查函数是否存在并调用
            if callable(func):
                func()
            else:
                logger.error(
                    f"Function {name} not found in module {module}",
                    simple_log="Function not found in module",
                )


def import_all_modules_user_directory(module_dir: str):
    """import all modules user directory"""
    for filename in os.listdir(module_dir):
        if (
            filename.endswith(".py") or filename.endswith(".pyc")
        ) and filename != "__init__.py":
            module_name = os.path.splitext(os.path.basename(filename))[0]
            file_path = os.path.join(module_dir, filename)
            try:
                spec = importlib.util.spec_from_file_location(module_name, file_path)
                module = importlib.util.module_from_spec(spec)
                spec.loader.exec_module(module)
                logger.info(f"Loading imported path: {type(spec)}")
                # 动态导入模块
                __dynamic_call_wrapped_action(module)
            except ModuleNotFoundError as e:
                logger.error(
                    f"Failed to load user defined module path: {e}",
                    simple_log="Failed to load user defined module path",
                )


def if_str_contain_trigger_words(
    query: str, trigger_words: List[str], trigger_words_exclude: List[str] = None
):
    """check if string contains trigger words"""
    trigger_pattern = r"|".join(re.escape(word) for word in trigger_words)
    exclude_pattern = (
        r"|".join(re.escape(word) for word in trigger_words_exclude)
        if trigger_words_exclude
        else None
    )
    if re.search(trigger_pattern, query, re.IGNORECASE):
        if exclude_pattern is None:
            return True
        if not re.search(exclude_pattern, query, re.IGNORECASE):
            return True
    return False
