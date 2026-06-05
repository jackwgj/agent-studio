# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
import re
from datetime import datetime, timezone, timedelta
from typing import Optional, Dict, Any

from jiuwen.common.log.base import logger
from jiuwen.planner.common.exception import PlannerTimeParseException
from jiuwen.planner.rails.common import utils
from jiuwen.planner.rails.common.decorator import action
from jiuwen.planner.rails.config import ActionConfig
from jiuwen.planner.rails.prebuilt_actions.base_action import Action
from jiuwen.planner.rails.prebuilt_actions.constants import DateAndTimeActionConst


class TimeParseAction(Action):
    """TimeParseAction"""

    def __init__(self, action_config: ActionConfig):
        super().__init__(action_config)
        self.config_format = self.set_format(action_config)
        self.init_time_and_date_prompt(DateAndTimeActionConst.TIME_PROMPT_NAME)
        self.query_list = []
        self._initialized = True
        self.date_trigger_words = ["天", "年", "月", "日", "周"]
        self.time_trigger_words = ["点", "刻", "分"]

    def set_format(self, action_config):
        """set format"""
        time_format = None
        if (
            action_config
            and hasattr(action_config, "action_extra_args")
            and action_config.action_extra_args
        ):
            time_format = action_config.action_extra_args.get("format")
        return time_format

    def invoke(self, query):
        """
        执行入口
        """
        parsed_time = ""
        if utils.if_str_contain_trigger_words(query, self.time_trigger_words, []):
            if not self.config_format:
                time_format = "%Y-%m-%d %H:%M"
            else:
                time_format = self.config_format
            try:
                return self.transform_time(query, time_format)
            except Exception:
                logger.error("transform_time_list throw exception")
                return parsed_time
        elif utils.if_str_contain_trigger_words(
            query, self.date_trigger_words, self.time_trigger_words
        ):
            if not self.config_format:
                time_format = "%Y-%m-%d"
            else:
                time_format = self.config_format
            try:
                date_value = datetime.strptime(query, time_format)
                utc_time = datetime.now(tz=timezone.utc) + timedelta(hours=8)
                if date_value <= utc_time:
                    date_value = date_value.replace(year=utc_time.year + 1)
                return date_value.strftime(time_format)
            except Exception:
                logger.error("transform date throw exception")
                return self._handle_alternate_date_formats(query, time_format)
        else:
            return parsed_time

    def transform_time(self, value, time_format):
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
                rs1 = self.format_time(rs1, time_format)
                rs2 = self.format_time(rs2, time_format)
                result = f"{rs1}-{rs2}".replace("-inf", "").replace("inf-", "")
            elif rs["type"] == "time_point":
                result = self.format_time(rs1, time_format)
            else:
                raise PlannerTimeParseException("Unknown time type encountered.")
        except Exception as e:
            raise PlannerTimeParseException(f"An error occurred: {str(e)}") from e
        return result

    def format_time(self, time_str, time_format):
        """format time"""
        if time_str == "inf":
            return "inf"
        try:
            time_obj = datetime.strptime(
                time_str, DateAndTimeActionConst.TIME_DEFAULT_DATEFORMAT
            )
            return time_obj.strftime(time_format)
        except ValueError as e:
            raise PlannerTimeParseException("Failed to format time.") from e

    def transform_date(self, value, time_format):
        """transform date"""
        try:
            date_value = datetime.strptime(value, time_format)
            utc_time = datetime.now(tz=timezone.utc)
            if date_value <= utc_time:
                date_value = date_value.replace(year=utc_time.year + 1)
            return date_value.strftime(time_format)
        except ValueError:
            return self._handle_alternate_date_formats(value, time_format)

    def handle_date_with_comma(self, value: str, time_format):
        """handle date with comma"""
        date_list = value.split("，")
        delta = date_list[1]
        cn_num = r"[一二两三四五六七八九十]{1,20}[天周]"
        delta = re.search(cn_num, delta).group()
        delta = delta + "后"
        rs = self.time_parser(delta, time_base=self.base_date)["time"][0]
        rs = datetime.strptime(rs, DateAndTimeActionConst.TIME_DEFAULT_DATEFORMAT)
        rs = rs.strftime(time_format)
        return rs

    def _handle_alternate_date_formats(self, value, time_format):
        try:
            if "，" in value:
                return self.handle_date_with_comma(value, time_format)
            parsed_time = self.time_parser(value, time_base=self.base_date)["time"][0]
            rs = datetime.strptime(
                parsed_time, DateAndTimeActionConst.TIME_DEFAULT_DATEFORMAT
            )
            return rs.strftime(time_format)
        except Exception as e:
            logger.error(
                f"failed to transform_date: {e}", simple_log="failed to transform_date"
            )
            return ""


@action
def parse_time(
    context: Optional[dict] = None,
    config: Optional[ActionConfig] = None,
) -> Dict[str, Any]:
    """改写日期和时间"""
    time_reason_action = TimeParseAction(config)
    args: dict = context.get("arguments")
    all_trigger_words = ["天", "年", "月", "日", "周", "点", "刻", "分"]
    for key, value in args.items():
        if isinstance(value, str) and utils.if_str_contain_trigger_words(
            value, all_trigger_words, []
        ):
            parsed_value = time_reason_action.invoke(value)
            if parsed_value:
                args.update({key: parsed_value})
    return dict(arguments=args)
