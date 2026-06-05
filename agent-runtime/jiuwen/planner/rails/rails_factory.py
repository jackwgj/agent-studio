# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""Rails Manager"""

from jiuwen.planner.rails.config import RailsConfig
from jiuwen.planner.rails.rails.execution_rails import ExecutionRails
from jiuwen.planner.rails.rails.input_rails import InputRails


class RailsFactory:
    """
    RailsFactory is a class responsible for managing Rails configurations.

    Attributes:
        config (RailsConfig): The configuration object for Rails.
        config_dir_path (str)： The path of configuration object for Rails.
    Methods:
        get_input_rails() -> InputRails:
            Returns an instance of InputRails configured with the provided RailsConfig.
    """

    def __init__(self, config: RailsConfig = None, config_dir_path: str = ""):
        if not config and not config_dir_path:
            raise ValueError("Either 'config' or 'config_dir_path' must be provided.")
        if not config and config_dir_path:
            self.config = RailsConfig.config_file_dir(config_dir_path)
        else:
            self.config = config
        self.config_dir_path = config_dir_path

    def get_input_rails(self) -> InputRails:
        """返回一个 InputRails 实例."""
        return InputRails(self.config)

    def get_execution_rails(self) -> ExecutionRails:
        """返回一个 ExecutionRails 实例."""
        return ExecutionRails(self.config)
