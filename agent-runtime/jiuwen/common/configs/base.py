#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""reading configuration file"""

import os

import yaml


class SingletonConfig:
    """Reading configuration file"""

    __instance: yaml.constructor = None

    def __init__(self):
        if SingletonConfig.__instance is not None:
            raise Exception("This class should be a singleton!")

        config_path = os.path.join(
            os.path.dirname(os.path.abspath(__file__)), "../../config.yaml"
        )
        with open(config_path, encoding="utf-8") as file:
            # 加载配置文件
            SingletonConfig.__instance = yaml.safe_load(file)

    @staticmethod
    def get_config():
        """Obtaining Configuration Instances"""
        if SingletonConfig.__instance is None:
            SingletonConfig()
        return SingletonConfig.__instance


config = SingletonConfig.get_config()
