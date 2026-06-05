#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""parse config file"""

import gc
import os

import yaml
from jiuwen.common.configs.env_constants import (
    GC_THRESHOLD_PART1_KEY,
    GC_THRESHOLD_PART2_KEY,
    GC_THRESHOLD_PART3_KEY,
)

# 设置GC可以基于环境变量配置
DEFAULT_GC_THRESHOLD_PART1 = 700
GC_THRESHOLD_PART1 = int(
    os.environ.get(GC_THRESHOLD_PART1_KEY, DEFAULT_GC_THRESHOLD_PART1)
)
DEFAULT_GC_THRESHOLD_PART2 = 10
GC_THRESHOLD_PART2 = int(
    os.environ.get(GC_THRESHOLD_PART2_KEY, DEFAULT_GC_THRESHOLD_PART2)
)
DEFAULT_GC_THRESHOLD_PART3 = 10
GC_THRESHOLD_PART3 = int(
    os.environ.get(GC_THRESHOLD_PART3_KEY, DEFAULT_GC_THRESHOLD_PART3)
)
gc.set_threshold(GC_THRESHOLD_PART1, GC_THRESHOLD_PART2, GC_THRESHOLD_PART3)


def load_config(config):
    """
    从设置文件中加载配置。

    功能描述：
    此函数接收一个文件路径作为参数，尝试打开并解析该路径指向的YAML文件，将解析结果作为字典返回。

    Args:
        config (str): YAML配置文件的路径。

    Returns:
        dict: 包含YAML文件解析结果的字典。

    Raises:
        ValueError: 如果读取或解析YAML文件失败，抛出此异常。

    Notes:
        - 使用`yaml.safe_load`方法来安全地解析YAML文件，避免执行潜在的危险操作。
        - 如果文件读取或解析过程中发生任何异常，将捕获该异常并抛出`ValueError`，同时保留原始异常信息。
    """
    try:
        # 1. 规范化路径（处理 ../ 等）
        abs_path = os.path.realpath(config)

        # 2. 检查路径是否在允许目录内
        if not os.path.exists(abs_path):
            raise ValueError(f"Config file does not exist: {abs_path}")

    except Exception as e:
        raise ValueError("Invalid config path") from e
    try:
        with open(abs_path, "r", encoding="utf-8") as config_yaml_file:
            config_dict = yaml.safe_load(config_yaml_file)
        return config_dict
    except Exception as e:
        raise ValueError("load yaml file failed.") from e
