# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
import json
from datetime import datetime
from typing import Optional, Dict, Any

from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.planner.common.exception import PlannerTimeParseException
from jiuwen.planner.rails.common.decorator import action
from jiuwen.planner.rails.config import ActionConfig
from jiuwen.planner.rails.prebuilt_actions.base_action import Action
from jiuwen.planner.rails.prebuilt_actions.constants import DateAndTimeActionConst


class TimeQueryAction(Action):
    _instance = None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super(TimeQueryAction, cls).__new__(cls)
        return cls._instance

    def __init__(self, action_config: ActionConfig):
        super().__init__(action_config)
        self.time_format = action_config.action_extra_args.get(
            "time_format", DateAndTimeActionConst.TIME_DEFAULT_DATEFORMAT
        )
        self.date_format = action_config.action_extra_args.get(
            "date_format", DateAndTimeActionConst.DATE_DEFAULT_DATEFORMAT
        )
        self.init_time_and_date_prompt(DateAndTimeActionConst.TIME_PROMPT_NAME)
        self.query_list = []
        self._initialized = True

    def invoke(self, query):
        """
        执行入口
        """
        self.query_list.append(query)
        total_query = "，".join(self.query_list)
        prompt = self.create_prompt(
            total_query, self.few_shot, DateAndTimeActionConst.TIME_PROMPT_NAME
        )
        logger.info(f"\n<LLM Input>: {type(prompt)}")

        inputs = LanguageModelInput(messages=ModelUtil.switch_message(prompt), tools=[])
        output_result = self.model_service.invoke(inputs=inputs)

        logger.info(f"time query action: {type(output_result)}")
        try:
            transform_time_res = self.transform_time_list(
                json.loads(output_result.content)
            )
        except Exception as e:
            logger.error(f"transform_time_list throw exception: {e.__str__()}")
            return ""
        return self.generate_date_time_result(output_result, transform_time_res)

    def transform_time_list(self, values):
        """transform time list"""
        output = []
        for value in values:
            new_value = self.transform_time(value)
            output.append(new_value)
        return output

    def transform_time(self, value):
        """transform time"""
        try:
            time_value = datetime.strptime(
                value, DateAndTimeActionConst.TIME_DEFAULT_DATEFORMAT
            )
            return time_value.strftime(DateAndTimeActionConst.DATE_DEFAULT_DATEFORMAT)
        except ValueError:
            logger.error("Failed to parse time in the default format.")

        try:
            rs = self.time_parser(value, time_base=self.base_date)
            rs1, rs2 = rs["time"][0], rs["time"][1]

            if rs["type"] == "time_span":
                rs1 = self.format_time(rs1)
                rs2 = self.format_time(rs2)
                result = f"{rs1}-{rs2}".replace("-inf", "").replace("inf-", "")
            elif rs["type"] == "time_point":
                result = self.format_time(rs1)
            else:
                raise PlannerTimeParseException("Unknown time type encountered.")
        except Exception as e:
            raise PlannerTimeParseException(f"An error occurred: {str(e)}") from e
        return result

    def format_time(self, time_str):
        """format time"""
        if time_str == "inf":
            return "inf"
        try:
            time_obj = datetime.strptime(
                time_str, DateAndTimeActionConst.TIME_DEFAULT_DATEFORMAT
            )
            return time_obj.strftime(self.time_format)
        except ValueError as e:
            raise PlannerTimeParseException("Failed to format time.") from e


@action
def rewrite_date_and_time_in_query(
    context: Optional[dict] = None,
    config: Optional[ActionConfig] = None,
) -> Dict[str, Any]:
    """改写日期和时间"""
    time_reason_action = TimeQueryAction(config)
    query = context.get("query")
    time_desc = time_reason_action.invoke(query)
    return dict(query=time_desc + query)
