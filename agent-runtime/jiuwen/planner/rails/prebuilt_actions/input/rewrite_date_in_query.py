# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
import json
import re
from datetime import datetime, timezone
from typing import Optional, Any, Dict

from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.planner.rails.common import utils
from jiuwen.planner.rails.common.decorator import action
from jiuwen.planner.rails.config import ActionConfig
from jiuwen.planner.rails.prebuilt_actions.base_action import Action
from jiuwen.planner.rails.prebuilt_actions.constants import DateAndTimeActionConst


class DateQueryAction(Action):
    _instance = None

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super(DateQueryAction, cls).__new__(cls)
        return cls._instance

    def __init__(self, action_config: ActionConfig):
        # Only initialize once
        if not hasattr(self, "_initialized"):
            super().__init__(action_config)
            self.total_query = ""
            self.date_format = action_config.action_extra_args.get(
                "date_format", DateAndTimeActionConst.DATE_DEFAULT_DATEFORMAT
            )
            self.base_date: dict = utils.get_current_date()
            self.init_time_and_date_prompt(DateAndTimeActionConst.DATE_PROMPT_NAME)
            self._initialized = True

    def invoke(self, query):
        """
        执行入口
        """
        prompt = self.create_prompt(
            query, self.few_shot, DateAndTimeActionConst.DATE_PROMPT_NAME
        )
        logger.info(
            f"\n<LLM Input>: {prompt}",
            simple_log="\n<LLM Input>: create prompt successfully",
        )
        inputs = LanguageModelInput(messages=ModelUtil.switch_message(prompt), tools=[])
        output_result = self.model_service.invoke(inputs=inputs)

        logger.info(
            f"date reason action output result is {output_result}",
            simple_log="get date reason action output result successfully",
        )
        transform_date_res = self.transform_date_list(json.loads(output_result.content))
        logger.info(
            f"transform date res: {transform_date_res}",
            simple_log="transform date res successfully",
        )
        return self.generate_date_time_result(output_result, transform_date_res)

    def transform_date_list(self, values):
        """transform date list"""
        output = []
        for value in values:
            new_value = self.transform_date(value)
            output.append(new_value)
        return output

    def transform_date(self, value):
        """transform date"""
        try:
            date_value = datetime.strptime(value, self.date_format)
            utc_time = datetime.now(tz=timezone.utc)
            if date_value <= utc_time:
                date_value = date_value.replace(year=utc_time.year + 1)
            return date_value.strftime(self.date_format)
        except ValueError:
            return self._handle_alternate_date_formats(value)

    def handle_date_with_comma(self, value: str):
        """handle date with comma"""
        date_list = value.split("，")
        base = date_list[0]
        delta = date_list[1]
        cn_num = r"[一二两三四五六七八九十]{1,20}[天周]"
        delta = re.search(cn_num, delta).group()
        delta = delta + "后"
        base = self.transform_date(base)
        base = datetime.strptime(base, self.date_format)
        rs = self.time_parser(delta, time_base=base)["time"][0]
        rs = datetime.strptime(rs, DateAndTimeActionConst.TIME_DEFAULT_DATEFORMAT)
        rs = rs.strftime(self.date_format)
        return rs

    def _handle_alternate_date_formats(self, value):
        try:
            if "，" in value:
                return self.handle_date_with_comma(value)
            parsed_time = self.time_parser(value, time_base=self.base_date)["time"][0]
            rs = datetime.strptime(
                parsed_time, DateAndTimeActionConst.TIME_DEFAULT_DATEFORMAT
            )
            return rs.strftime(self.date_format)
        except (KeyError, ValueError) as e:
            logger.error(
                f"failed to transform_date: {e}", simple_log="failed to transform_date"
            )
            return ""


@action
def rewrite_date_in_query(
    context: Optional[dict] = None,
    config: Optional[ActionConfig] = None,
) -> Dict[str, Any]:
    """日期改写"""
    date_reason_action = DateQueryAction(config)
    query = context.get("query")
    date_desc = date_reason_action.invoke(query)
    return dict(query=date_desc + query)
