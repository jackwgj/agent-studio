#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.

import ast
import json
import re
import time
from copy import deepcopy
from datetime import datetime

import jionlp as jio
import nltk
from dateutil import parser
from i18n.language_processer import LanguageManager
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.orchestration.flow.constant import QuestionerKeyword
from jiuwen.planner.planning_modules.base import ChatContent, PromptPlanningModule
from jiuwen.prompt import TemplateManager
from json_repair import json_repair
from utils.util import (
    format_args_input,
    is_base64_image,
    is_valid_china_postcode,
    is_valid_chinese_passport,
    is_valid_cn_mobile_number,
    is_valid_cn_plate,
    is_valid_country_code,
    is_valid_email,
    is_valid_ipv4_address,
    is_valid_mac_address,
    is_valid_url,
)

TIME_FORMATS = [
    # 英文日期格式
    "%Y-%m-%d %H:%M:%S",
    "%Y/%m/%d %H:%M:%S",
    "%m/%d/%Y %I:%M:%S %p",
    "%d/%m/%Y %H:%M:%S",
    "%d-%m-%Y %H:%M:%S",
    "%Y-%m-%d",
    "%Y-%m-%d %H",
    "%Y-%m-%d %H:%M",
    "%Y/%m/%d",
    "%d/%m/%Y",
    "%d-%m-%Y",
    "%B %d, %Y %I:%M %p",
    "%b %d, %Y",
    "%d %B %Y",
    "%I:%M:%S %d-%m-%Y",
    "%a, %d %b %Y %H:%M:%S %Z",
    "%Y%m%dT%H%M%S",
    "%Y.%m.%d",
    "%Y%m%d",
    "%I:%M %p %B %d, %Y",
    "%d %b, %Y",
    "%b %d %Y",
    "%A, %B %d, %Y",
    # 添加中文日期格式
    "%Y年%m月%d日 %H时%M分%S秒",
    "%Y年%m月%d日 %H:%M:%S",
    "%Y年%m月%d日",
    "%Y 年 %m 月 %d 日",
    "%Y年%m月",
    "%m月%d日 %H:%M",
    "%Y/%m/%d",
    "%Y年%m月%d日%H时%M分",
    "%Y年%m月%d日%H点%M分%S秒",
    "%Y年%m月%d日%H点%M分",
    "%Y 年 %m 月 %d 日 %H:%M:%S",
    "%Y年%m月%d日 %H点%M分",
    "%Y.%m.%d %H:%M",
    "%Y%m%d %H:%M:%S",
    "%Y年%m月%d日%H时",
    "%Y年",
    "%m月%d日",
    "%Y-%m-%d %H时%M分%S秒",
    "%Y/%m/%d %H时%M分%S秒",
    "%Y.%m.%d %H时%M分%S秒",
    "%Y年%m月%d日 星期%w",
    # 带中文星期的格式
    "%Y年%m月%d日 星期%a",
    "%Y年%m月%d日 星期%b",
    "%Y年%m月%d日 星期%A",
    # 短日期格式
    "%y-%m-%d",
    "%y/%m/%d",
    "%y%m%d",
    "%H:%M:%S",
    "%H:%M",
    "%H",
    "%M:%S",
    "%m/%d/%Y %I:%M %p",
]


class NL2JSONProcessor:
    def __init__(
        self,
        history: list,
        query: str,
        model,
        args_extract_rules: list,
        mode: str,
        extracted_args,
        extra_info: str = "",
        language: str = "zh-cn",
    ):
        """
        :param history: 对话历史 history = [{"role": "user", "content": "xxx"}, {"role": "assistant", "content": "xxx"}]
        :param query: 用户本轮query
        :param model: 模型相关配置。样例：{
                                            "modelName": "xxx",
                                            "modelType": "xxx",
                                            "runtime_context": {
                                                    "workflow_execute_requests_headers_name": "xxx",
                                                    "workflow_execute_project_id": "xxx"
                                            },
                                            "hyper_parameters": {'top_p': 0.5, 'temperature': 0.2},
                                            "extension": {}
                                            }
                                        }
        :param args_extract_rules: 参数相关配置，如果没有参数类型相关修复，则不需要有 rules， 类型校验暂时只支持：str, int, float, bool。样例：
                                  其中校验规则包括：NUM(数值范围校验) DATE(时间格式校验) ENUM(枚举值校验) CHAR(字符串长度校验) COMMON_FORMAT(通用数据格式校验)   如果有默认值 则增加默认值 default_value
                                  required 表示必选参数
            [
                {'description': '开始时间', 'name': 'start_time', 'type': 'string','validateRule': '%Y-%m-%d %H:%M:%S', 'validateType': 'DATE', 'validated': True},
                {'description': '人数',  'name': 'num', 'required': True, 'type': 'number', 'validateRule': '1,9','validateType': 'NUM', 'validated': True},
                {'description': '面试者电话',  'name': 'end_time', 'required': True, 'type': 'string','validateRule': '电话号码', 'validateType': 'COMMON_FORMAT', 'validated': True},
                {'description': '面试的具体时间，星期几',  'name': 'day', 'required': True, 'type': 'string','validateRule': '星期一，星期二，星期三，星期四，星期五', 'validateType': 'ENUM', 'validated': True},
                {'description': '面试纪要',  'name': 'case', 'required': True, 'type': 'string','validateRule': '20', 'validateType': 'CHAR', 'validated': True, "default_value": "这次面试很顺利"}
            ]
        :param mode: 模式相关配置。可选值：speed effect
        :param extracted_args: 之前已提取的参数,更新新提取的参数后返回。
        """
        self.extra_info = extra_info
        self.history = history
        self.query = query
        if isinstance(model, dict):
            self.model = ModelFactory().get_model(
                model_type=model.get("model_type", ""),
                model_name=model.get("model_name", ""),
                runtime_context=model.get("runtime_context", {}),
                **model.get("hyper_parameters", {}),
                **model.get("extension", {}),
            )
            self.model_name = model.get("deployment_id", "")
        else:
            self.model = model
            self.model_name = model.deployment_id
        args_formated = format_args_input(args_extract_rules)
        self.arguments = args_formated.get("args", {})
        self.args_rules = args_formated.get("rules", {})
        self.required_args_list = args_formated.get("required", {})
        self.mode = mode
        self.fields_check_failed = set()
        self.edit_distance = 1  # 枚举值编辑距离
        self.time_formats = TIME_FORMATS
        self.language = language
        self.language_config = LanguageManager().get_language_context_for_node(
            self.language, "nl2json"
        )
        self.initial_prompt()
        self.extracted_args = extracted_args if extracted_args else {}

    def initial_prompt(self):
        """
        initial prompt
        """
        self.prompt_engine = PromptPlanningModule()
        prompt = TemplateManager().get(
            name=self.language_config.get("template_nl2json"),
            filters={"model_name": self.model_name},
        )
        if prompt is not None and hasattr(prompt, "content"):
            self.prompt = prompt.content

        prompt_reflection = TemplateManager().get(
            name=self.language_config.get("template_nl2json_reflection"),
            filters={"model_name": self.model_name},
        )
        if prompt_reflection is not None and hasattr(prompt_reflection, "content"):
            self.prompt_reflection = prompt_reflection.content

        prompt_time = TemplateManager().get(
            name=self.language_config.get("template_time_extract"),
            filters={"model_name": self.model_name},
        )
        if prompt_time is not None and hasattr(prompt_time, "content"):
            self.prompt_time = prompt_time.content

    def _get_time_info(self, text):
        time_info = ""
        time_entities = jio.ner.extract_time(
            text, time_base=time.time(), with_parsing=True
        )
        if not time_entities:
            return time_info

        for entities in time_entities:
            time_param = entities["text"]
            time_type = entities["type"]
            if time_type == "time_span":
                start = entities["detail"]["time"][0]
                end = entities["detail"]["time"][1]
                # 这是中式说法，不需要翻译
                begin_month_pattern = r"^\d{1,2}月初$|^[一二三四五六七八九十]+月初$"
                begin_match = re.fullmatch(begin_month_pattern, time_param.strip())
                end_month_pattern = r"^\d{1,2}月底$|^[一二三四五六七八九十]+月底$"
                end_match = re.fullmatch(end_month_pattern, time_param.strip())
                if begin_match:
                    standard_time = start
                elif end_match:
                    standard_time = end
                else:
                    standard_time = self.language_config.get("standard_time").format(
                        start, end
                    )
            elif time_type == "time_point":
                try:
                    time1_obj = datetime.strptime(
                        entities["detail"]["time"][0], "%Y-%m-%d %H:%M:%S"
                    )
                    time2_obj = datetime.strptime(
                        entities["detail"]["time"][1], "%Y-%m-%d %H:%M:%S"
                    )
                    time_diff = abs((time2_obj - time1_obj).days)
                except Exception:
                    time1_obj = entities["detail"]["time"][0]
                    time2_obj = entities["detail"]["time"][1]
                    time_diff = 2
                if time_diff > 1:
                    standard_time = time2_obj
                else:
                    standard_time = time1_obj
            else:
                time_extracted = entities["detail"]["time"]
                standard_time = time_extracted
            temp = self.language_config.get("time_temp").format(
                time_param, standard_time
            )
            time_info += temp

        try:
            llm_res = text.replace("，", ",")
            model_res_list = ast.literal_eval(llm_res)
        except Exception:
            model_res_list = []

        text_str = ",".join(model_res_list)
        time_infoinput = time_info[:-2]
        if len(model_res_list) == len(time_entities):
            result = self.language_config.get("time_reminder").format(
                time_info=time_infoinput,
            )
        else:
            result = self.language_config.get("time_reminder_2").format(
                time_info=time_infoinput, text_str=text_str
            )

        return result

    def add_time_info_in_query(self):
        inputs = self._parse_input()
        try:
            response = self.model.invoke(inputs).content
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e

        additional_text = self._get_time_info(response)

        return additional_text

    async def async_add_time_info_in_query(self):
        inputs = self._parse_input()
        try:
            model_resp = await self.model.ainvoke(inputs)
            response = model_resp.content
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e

        additional_text = self._get_time_info(response)

        return additional_text

    def _parse_input(self):
        content_list = []
        for idx, dialog in enumerate(self.history):
            if isinstance(dialog, dict) and dialog.get("role") == "user":
                content_list.append(dialog.get("content", ""))
            elif hasattr(dialog, "role") and dialog.role == "user":
                content_list.append(dialog.content)
        content_list.append(self.query)
        content = " ".join(content_list)

        prompt_template = [
            ChatContent(
                role=p.get(QuestionerKeyword.ROLE),
                content=p.get(QuestionerKeyword.CONTENT),
            )
            for p in self.prompt_time
        ]
        current_time = datetime.now()
        current_month = current_time.month
        prompt_template_input = {"current_month": current_month, "input": content}
        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        inputs = LanguageModelInput(messages=ModelUtil.switch_message(prompt), tools=[])
        return inputs

    def check_common_data(self, data_type, value, query):
        check_value = value.replace(" ", "")
        check_query = query.replace(" ", "")
        common_data_func = {
            "电话号码": is_valid_cn_mobile_number,
            "护照号码": is_valid_chinese_passport,
            "图片base64": is_base64_image,
            "url": is_valid_url,
            "邮箱": is_valid_email,
            "电话号码国家码": is_valid_country_code,
            "IP地址": is_valid_ipv4_address,
            "MAC地址": is_valid_mac_address,
            "邮政编码": is_valid_china_postcode,
            "车牌号": is_valid_cn_plate,
        }
        if check_value not in check_query:
            return False
        if data_type in ["UUID", "文件路径", "银行卡号"]:
            # 银行卡号、UUID、文件路径 暂时只检查是否在文本中
            return True
        elif common_data_func[data_type](value):
            return True
        return False

    def common_data_format_check_action(self, args: dict, rules: dict):
        dio_history = deepcopy(self.history)
        query = (
            "\n".join(
                [
                    item["content"] if isinstance(item, dict) else item.content
                    for item in dio_history
                ]
            )
            + self.query
        )

        for key, rule_value in rules.items():
            extract_value = args.get(key)
            if extract_value is None:
                continue
            if not self.check_common_data(rule_value, extract_value, query):
                args.update({key: None})
        return args

    def update_parameters_with_defaults(self, args: dict, rules: dict):
        for key, default_value in rules.items():
            extract_value = args.get(key)
            if extract_value is None and default_value is not None:
                args.update({key: default_value})

        return args

    def _calculate_edit_distance(self, word, enum_list):
        res = ""
        for enum_value in enum_list:
            if not enum_value:
                continue
            if nltk.edit_distance(word, enum_value) <= self.edit_distance:
                res = enum_value
        return res

    def enum_legality_validate_action(self, args, rules):
        for key, enum_value in rules.items():
            extract_value = args.get(key)
            if extract_value is None:
                continue
            if extract_value in enum_value:
                continue

            # 取值不在枚举值中 枚举值精确查找
            args.update({key: None})
        return args

    def length_limit_validate_action(self, args: dict, rules):
        for key, rule_value in rules.items():
            extract_value = args.get(key)
            if extract_value is None:
                continue
            if not isinstance(extract_value, str):
                args.update({key: None})
                continue
            length = len(extract_value)
            if length > rule_value:
                args.update({key: None})
        return args

    def number_range_validate_action(self, args: dict, rules):
        for key, rule_value in rules.items():
            extract_value = args.get(key)
            if extract_value is None:
                continue
            try:
                extract_value = float(extract_value)
            except Exception:
                args.update({key: None})
                continue
            min_value = rule_value[0]
            max_value = rule_value[1]
            if not min_value <= extract_value <= max_value:
                args.update({key: None})
        return args

    def _time_parse(self, value, time_fmt):
        try:
            dt = parser.parse(value)
            formated_data = dt.strftime(time_fmt)
            return formated_data
        except Exception:
            for fmt in self.time_formats:
                try:
                    dt = datetime.strptime(value, fmt)
                    formated_data = dt.strftime(time_fmt)
                    return formated_data
                except (ValueError, TypeError):
                    continue
        return None

    def date_time_format_action(self, args: dict, rules):
        for key, time_fmt in rules.items():
            extract_value = args.get(key)
            if extract_value is None:
                continue

            formated_data = self._time_parse(extract_value, time_fmt)
            args.update({key: formated_data})
        return args

    def process_rails(self, cur_extracted_key_fields):
        cur_extracted_fields = deepcopy(cur_extracted_key_fields)
        if not self.args_rules:
            return cur_extracted_fields

        for rail_name, field_rules in self.args_rules.items():
            if rail_name == "common_data_format_check":
                cur_extracted_fields = self.common_data_format_check_action(
                    cur_extracted_fields, field_rules
                )
            elif rail_name == "update_default_values":
                cur_extracted_fields = self.update_parameters_with_defaults(
                    cur_extracted_fields, field_rules
                )
            elif rail_name == "date_time_format":
                cur_extracted_fields = self.date_time_format_action(
                    cur_extracted_fields, field_rules
                )
            elif rail_name == "enum_legality_validate":
                cur_extracted_fields = self.enum_legality_validate_action(
                    cur_extracted_fields, field_rules
                )
            elif rail_name == "length_limit_validate":
                cur_extracted_fields = self.length_limit_validate_action(
                    cur_extracted_fields, field_rules
                )
            elif rail_name == "number_range_validate":
                cur_extracted_fields = self.number_range_validate_action(
                    cur_extracted_fields, field_rules
                )

        return cur_extracted_fields

    def create_prompt_template_input(self, cur_extracted_key_fields) -> dict:
        """
        create input for prompt template
        """
        required_params = []
        i = 0
        for name, param in self.arguments.items():
            number = i + 1
            required_params.append(
                self.language_config.get("required_params_str").format(
                    number=str(number),
                    name=name,
                    cn_name=param.get("cn_name", ""),
                    desc=param.get("desc", ""),
                    data_type=param.get("type"),
                )
            )

        required_name = [
            param.get("cn_name", "") for _, param in self.arguments.items()
        ]
        required_name = (
            "、".join(required_name)
            + f"{len(self.arguments.items())}"
            + self.language_config.get("necessary_info")
        )

        required_params_list = []
        for name, param in self.arguments.items():
            required_params_list.append(
                "{name}: {cn_name} {desc}，type：{data_type}".format(
                    name=name,
                    cn_name=param.get("cn_name", ""),
                    desc=param.get("desc", ""),
                    data_type=param.get("type"),
                )
            )
        required_params_list_str = "\n".join(required_params_list)

        # 判断是否需要添加当前的query
        if len(self.history) == 0 or (
            hasattr(self.history[-1], "content")
            and self.history[-1].content != self.query
        ):
            self.history.append({"role": "user", "content": self.query})

        # 拼接最近的10条记录
        dig_history = "\n".join(
            [
                f"{unit['role']}：{unit['content']}"
                if isinstance(unit, dict)
                else f"{unit.role}：{unit.content}"
                for unit in self.history[-10:]
            ]
        )

        required_params_str = " \n " + " \n ".join(required_params)

        if not self.fields_check_failed:
            wrong_params_info = self.language_config.get("wrong_params_info_no")
        else:
            wrong_params_info = self.language_config.get(
                "wrong_params_info"
            ) + "、".join(self.fields_check_failed)

        # 获取当前时间
        now = datetime.now()
        week_index = now.weekday()
        week_list = self.language_config.get("week_list")
        week_str = week_list[week_index]
        # 格式化日期和时间
        formatted_time = now.strftime(
            self.language_config.get("time_info") + week_str + ">"
        )

        return {
            "required_params": required_params_str,
            "dig_history": dig_history,
            "extra_info": self.extra_info,
            "response": json.dumps(cur_extracted_key_fields, ensure_ascii=False),
            "wrong_params": wrong_params_info,
            "required_name": required_name,
            "required_params_list": required_params_list_str,
            "current_time": formatted_time,
        }

    def reflection(self, cur_extracted_key_fields):
        """
        reflection at primary parameter extraction level and improve the result
        """
        try:
            prompt_template_input = self.create_prompt_template_input(
                cur_extracted_key_fields
            )

            prompt_template = [
                ChatContent(
                    role=p.get(QuestionerKeyword.ROLE),
                    content=p.get(QuestionerKeyword.CONTENT),
                )
                for p in self.prompt_reflection
            ]
            prompt = self.prompt_engine.format(
                keywords=prompt_template_input, prompt_template=prompt_template
            )
            llm_inputs = LanguageModelInput(
                messages=ModelUtil.switch_message(prompt), tools=[]
            )
            try:
                new_response = self.model.invoke(llm_inputs).content
            except Exception as llm_invoke_exception:
                raise JiuWenBaseException(
                    message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                    error_code=StatusCode.INVOKE_LLM_FAILED.code,
                ) from llm_invoke_exception
            malformed_json = new_response.replace("None", "null").replace(
                '"None"', "null"
            )
            repaired_json = json_repair.repair_json(malformed_json)
            new_extracted_key_fields = {
                k: v
                for k, v in json.loads(repaired_json).items()
                if v is not None and str(v)
            }
            cur_extracted_key_fields.update(new_extracted_key_fields)
        except Exception as questioner_invoke:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_QUESTIONER_REFLECTION_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_QUESTIONER_REFLECTION_ERROR.code,
            ) from questioner_invoke
        return cur_extracted_key_fields

    def validate_and_convert(self, value, expected_type):
        """
        校验并转换参数的类型。如果类型不对，尝试转换类型；如果转换失败，置为空（None）。

        :param value: 需要校验和转换的参数
        :param expected_type: 预期的类型，可以是 Python 内置类型或者字符串类型名
        :return: 校验后的参数值，可能是转换后的值，也可能是 None
        """
        # 处理不同大小写的类型字符串
        if isinstance(expected_type, str):
            expected_type = expected_type.lower()

        # 类型转换处理
        try:
            # 字符串类型处理
            if expected_type in ["string", "str"]:
                return value if isinstance(value, str) else str(value)

            # 布尔类型处理
            if expected_type in ["boolean", "bool"] and isinstance(value, bool):
                return value
            if expected_type in ["boolean", "bool"] and isinstance(value, str):
                # 对字符串类型的布尔值进行处理
                if value.lower() in ["true", "false"]:
                    return value.lower() == "true"
                elif value.lower() in ["是", "否"]:
                    return value == "是"
                else:
                    return None  # 非法的布尔值字符串返回 None
            elif expected_type in ["boolean", "bool"]:
                return bool(value)

            # 整数类型处理
            if expected_type in ["int", "integer"]:
                if isinstance(value, int):
                    return value
                return int(value)

            # 浮点数类型处理
            elif expected_type in ["float", "float64", "number"]:
                if isinstance(value, float):
                    return value
                return float(value)
            else:
                # 如果是其他类型，尝试直接转换
                return expected_type(value)

        except (ValueError, TypeError):
            # 转换失败，返回 None
            return None

    def check_type(self, cur_extracted_key_fields: dict):
        for key, value in cur_extracted_key_fields.items():
            item = self.arguments.get(key, {})
            data_type = item.get("type")
            if not data_type:
                continue
            cur_extracted_key_fields.update(
                {key: self.validate_and_convert(value, data_type)}
            )
        return cur_extracted_key_fields

    def invoke(self, **kwargs):
        """

        :param kwargs:
        :return: args: 本轮提取的参数及之前提取的参数合并后的结果
                llm_info: 大模型请求输入输出信息,用于insight
        """
        # 首先判断是否需要进行时间增强 如果有时间格式校验要求 则进行时间增强
        prompt_template_input = self.create_prompt_template_input(self.extracted_args)
        prompt_template = [
            ChatContent(
                role=p.get(QuestionerKeyword.ROLE),
                content=p.get(QuestionerKeyword.CONTENT),
            )
            for p in self.prompt
        ]
        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        if self.args_rules.get("date_time_format"):
            content = prompt[0].content + self.add_time_info_in_query()
            prompt[0].content = content

        # 获取大模型参数提取结果
        llm_inputs = LanguageModelInput(
            messages=ModelUtil.switch_message(prompt), tools=[]
        )
        try:
            response = self.model.invoke(llm_inputs).content
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from e

        llm_info = {"llm_info": {"llm_inputs": llm_inputs, "llm_outputs": response}}
        # convert llm output from string to dictionary
        try:
            formed_json = response.replace("None", "null").replace('"None"', "null")
            repaired_json = json_repair.repair_json(formed_json)
            cur_extracted_key_fields = {}
            for k, v in json.loads(repaired_json).items():
                if v is not None and str(v):
                    cur_extracted_key_fields[k] = v
        except (SyntaxError, AttributeError, ValueError):
            logger.error(QuestionerKeyword.LLM_PARSE_FAIL_MESSAGE)
            return dict(args={}, llm_info=llm_info)
        except Exception as e:
            logger.error(QuestionerKeyword.LLM_UNKNOWN_ERROR)
            logger.error(e)
            return dict(args={}, llm_info=llm_info)

        extracted_key_fields_before_check = {
            k: v for k, v in cur_extracted_key_fields.items() if v is not None
        }

        cur_extracted_key_fields = self.check_type(cur_extracted_key_fields)

        # processing of custom rails
        processed_extracted_key_fields = self.process_rails(cur_extracted_key_fields)

        extracted_key_fields_after_check = {
            k: v for k, v in processed_extracted_key_fields.items() if v is not None
        }
        self.fields_check_failed = (
            extracted_key_fields_before_check.keys()
            - extracted_key_fields_after_check.keys()
        )

        if self.mode == "effect":
            # reflection
            processed_extracted_key_fields = self.reflection(
                extracted_key_fields_before_check
            )

            processed_extracted_key_fields = self.check_type(
                processed_extracted_key_fields
            )

            # processing of custom rails  反思后再校验一次
            processed_extracted_key_fields = self.process_rails(
                processed_extracted_key_fields
            )

        # 如果有些值 之前已经取到 但是本轮提取置空了 可能是参数本轮有修改意图  但是未提供新取值  还是要先更新为None 再进行去除
        self.extracted_args.update(processed_extracted_key_fields)
        self.extracted_args = {
            k: v for k, v in self.extracted_args.items() if v is not None
        }

        return dict(args=self.extracted_args, llm_info=llm_info)

    async def ainvoke(self, **kwargs):
        """
        异步调用
        :param kwargs:
        :return: args: 本轮提取的参数及之前提取的参数合并后的结果
                llm_info: 大模型请求输入输出信息,用于insight
        """
        # 首先判断是否需要进行时间增强 如果有时间格式校验要求 则进行时间增强
        prompt_template_input = self.create_prompt_template_input(self.extracted_args)
        prompt_template = [
            ChatContent(
                role=p.get(QuestionerKeyword.ROLE),
                content=p.get(QuestionerKeyword.CONTENT),
            )
            for p in self.prompt
        ]
        prompt = self.prompt_engine.format(
            keywords=prompt_template_input, prompt_template=prompt_template
        )
        if self.args_rules.get("date_time_format"):
            date_resp = await self.async_add_time_info_in_query()
            content = prompt[0].content + date_resp
            prompt[0].content = content

        # 获取大模型参数提取结果
        llm_inputs = LanguageModelInput(
            messages=ModelUtil.switch_message(prompt), tools=[]
        )
        try:
            result = await self.model.ainvoke(llm_inputs)
            response = result.content
        except Exception as modelexception:
            raise JiuWenBaseException(
                message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                error_code=StatusCode.INVOKE_LLM_FAILED.code,
            ) from modelexception

        llm_info = {"llm_info": {"llm_inputs": llm_inputs, "llm_outputs": response}}

        try:
            malformed_json = response.replace("None", "null").replace('"None"', "null")
            repaired_json = json_repair.repair_json(malformed_json)
            cur_extracted_key_fields = {
                k: v
                for k, v in json.loads(repaired_json).items()
                if v is not None and str(v)
            }
        except (SyntaxError, AttributeError, ValueError):
            logger.error(QuestionerKeyword.LLM_PARSE_FAIL_MESSAGE)
            return dict(args={}, llm_info=llm_info)
        except Exception as questioner_exception:
            logger.error(QuestionerKeyword.LLM_UNKNOWN_ERROR)
            logger.error(questioner_exception)
            return dict(args={}, llm_info=llm_info)

        extracted_key_fields_before_check = {
            k: v for k, v in cur_extracted_key_fields.items() if v is not None
        }

        cur_extracted_key_fields = self.check_type(cur_extracted_key_fields)

        # processing of custom rails
        processed_extracted_key_fields = self.process_rails(cur_extracted_key_fields)

        extracted_key_fields_after_check = {
            k: v for k, v in processed_extracted_key_fields.items() if v is not None
        }
        self.fields_check_failed = (
            extracted_key_fields_before_check.keys()
            - extracted_key_fields_after_check.keys()
        )

        if self.mode == "effect":
            # reflection
            processed_extracted_key_fields = await self.async_reflection(
                extracted_key_fields_before_check
            )

            processed_extracted_key_fields = self.check_type(
                processed_extracted_key_fields
            )

            # 反思后再校验一次
            processed_extracted_key_fields = self.process_rails(
                processed_extracted_key_fields
            )

        # 如果有些值 之前已经取到 但是本轮提取置空了 可能是参数本轮有修改意图  但是未提供新取值  还是要先更新为None 再进行去除
        self.extracted_args.update(processed_extracted_key_fields)
        self.extracted_args = {
            k: v for k, v in self.extracted_args.items() if v is not None
        }

        return dict(args=self.extracted_args, llm_info=llm_info)

    async def async_reflection(self, cur_extracted_key_fields):
        """
        reflection at primary parameter extraction level and improve the result
        """
        try:
            prompt_template_input = self.create_prompt_template_input(
                cur_extracted_key_fields
            )

            prompt_template = [
                ChatContent(
                    role=p.get(QuestionerKeyword.ROLE),
                    content=p.get(QuestionerKeyword.CONTENT),
                )
                for p in self.prompt_reflection
            ]
            prompt = self.prompt_engine.format(
                keywords=prompt_template_input, prompt_template=prompt_template
            )
            llm_inputs = LanguageModelInput(
                messages=ModelUtil.switch_message(prompt), tools=[]
            )
            try:
                result = await self.model.ainvoke(llm_inputs)
                new_response = result.content
            except Exception as e:
                raise JiuWenBaseException(
                    message=StatusCode.INVOKE_LLM_FAILED.errmsg,
                    error_code=StatusCode.INVOKE_LLM_FAILED.code,
                ) from e
            malformed_json = new_response.replace("None", "null").replace(
                '"None"', "null"
            )
            repaired_json = json_repair.repair_json(malformed_json)
            new_extracted_key_fields = {
                k: v
                for k, v in json.loads(repaired_json).items()
                if v is not None and str(v)
            }
            cur_extracted_key_fields.update(new_extracted_key_fields)
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_QUESTIONER_REFLECTION_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_QUESTIONER_REFLECTION_ERROR.code,
            ) from e
        return cur_extracted_key_fields
