# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""base action module"""

import json
import os
from abc import abstractmethod
from typing import Optional

from jiuwen.common.configs.env_constants import MODEL_CFG_SSL_MODE_KEY
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.log.base import logger
from jiuwen.controller.task_planner.planning_modules.prompt_module import ChatContent
from jiuwen.planner.planning_modules.base import PromptPlanningModule
from jiuwen.planner.rails.common import utils
from jiuwen.planner.rails.config import ActionConfig
from jiuwen.planner.rails.time_parser.parse_time import TimeParser
from jiuwen.prompt import TemplateManager, Template


class Action:
    def __init__(self, action_config: Optional[ActionConfig] = None):
        os.environ.setdefault(MODEL_CFG_SSL_MODE_KEY, "true")
        self.config = action_config
        if self._has_valid_model_config():
            chat_agent = ModelFactory().get_model(
                model_type=action_config.model.type or "agentBuilder",
                model_name=action_config.model.model,
                top_p=0.0,
            )
            self.model_service = chat_agent

        self.prompt_template_content = action_config.prompt
        self.few_shot = action_config.few_shot
        self.base_date: dict = self.init_base_date(action_config)
        self.time_parser = TimeParser()

    @staticmethod
    def generate_date_time_result(output_result, transform_date_res):
        """generate date time result"""
        query_results = []
        for date_desc, transform_date in zip(
            json.loads(output_result.content), transform_date_res
        ):
            if transform_date != "error":
                query_results.append(f"{date_desc}是{transform_date}")
        if query_results:
            res = "，".join(query_results) + "。"
        else:
            res = ""
        logger.info(f"query_result: {res}", simple_log="query_result")
        return res

    @staticmethod
    def create_prompt(query, few_shot, template_name):
        """create prompt"""
        prompt_info = {"query": query, "FEW_SHOT": few_shot}
        prompt_engine = PromptPlanningModule()
        prompt = prompt_engine.format(template_name=template_name, keywords=prompt_info)
        prompt.append(dict(role="user", content=query))
        return prompt

    @staticmethod
    def init_base_date(action_config):
        """init base date"""
        if (
            action_config
            and hasattr(action_config, "action_extra_args")
            and action_config.action_extra_args
        ):
            base_date = (
                action_config.action_extra_args.get("base_date")
                or utils.get_current_date()
            )
        else:
            base_date = utils.get_current_date()
        return base_date

    @abstractmethod
    def invoke(self, query: str) -> str:
        """invoke"""
        pass

    def init_time_and_date_prompt(self, prompt_name):
        """init time and date prompt"""
        if self.prompt_template_content:
            msgs = [
                ChatContent(msg.get("role"), msg.get("content")).__dict__
                for msg in self.prompt_template_content
            ]
            template_manager = TemplateManager()
            template = None
            try:
                template = template_manager.get(name=prompt_name)
            except JiuWenBaseException:
                logger.warning(
                    f"prompt {prompt_name} is not found, start to register",
                    simple_log="prompt is not found, start to register",
                )
            if template is None:
                template_manager.register(
                    template=Template(name=prompt_name, content=msgs)
                )

    def _has_valid_model_config(self):
        # check if available for model config
        return (
            self.config.model
            and hasattr(self.config.model, "type")
            and hasattr(self.config.model, "model")
            and self.config.model.model
        )
