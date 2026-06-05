import copy
import json
import os
import re
import time

import jionlp as jio
import nltk
import requests
from dateutil import parser
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.language_model.poc_agent_builder import ChatAgentBuilder
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.orchestration.flow.constant import QuestionerKeyword

HTTP_TIMEOUT = os.getenv("HTTP_TIMEOUT", "15")
KEY_VALUE_EXTRACTED = "val_extracted"
KEY_ARG_INFO_TO_EXTRACT = "arg_info_to_extract"
KEY_ARG_DESC = "desc"

# 定义错误码
SUCC_CODE = 1
FAIL_CODE = 0

# 定义默认编辑距离
EDIT_DISTANCE = 1
DIFF_SCORE = 0.6

enum_config = {
    "model": {
        "type": "agentBuilder_llm",
        "model": "poc_agentBuilder",
        "url": "",
        "api_key": "",
    },
    "prompt": [
        {
            "role": "system",
            "content": '你是一个语义匹配系统，请从枚举值里提出语义最相似的一个。增加一个标识代表是否找到匹配语义词。如果十分确定，请返回，除此之外，请在最开始返回不确定。请遵从以下规则：1.输出格式必须是:["枚举值"，"是否匹配"]。2.如果提取出的语义十分确定，"是否匹配"的值为1，不确定则为0。1.文本：请从枚举值如下：["香蕉","苹果","菠萝","草莓"]中寻找与"草莓的"语义最接近的词。输出： ["草莓", 1 ]。\n2.文本：请从枚举值如下：["香蕉","苹果","菠萝","草莓"]中寻找与"男的"语义最接近的词。输出： ["没有找到相近枚举值", 0 ]。',
        }
    ],
    "action_extra_args": {"enum_pattern": r"枚举值为[(.*?)]"},
}

functionmappinglist = {
    "key1": [
        "office_knowledge",
        "meeting_room_reserve",
        "translate",
        "stuff_query",
        "traveltool",
    ]
}

function_st = [
    "office_knowledge",
    "meeting_room_reserve",
    "translate",
    "stuff_query",
    "traveltool",
]


def get_fun_info(functions, fun_name):
    for item in functions:
        if item.name == fun_name:
            return item
    return []


def get_action_config(llm_info):
    res = copy.deepcopy(enum_config)
    res["model"]["url"] = llm_info.api_base
    return res


map_zhengze = {"time": r"时间格式提取为<([^>]+)>", "enum": r"枚举值为\[(.*?)\]"}

enum_spilt_word = [",", "，"]


def extract_enum_from_text(text):
    pattern = r"枚举值为\[(.*?)\]"
    match = re.search(pattern, text)
    res = text
    find = 0
    if match:
        res = match.group(1)
        find = 1
        # 替换中文逗号为英文逗号
        res = res.replace("，", ",")
        # 去除引号和空格
        enum_part = res.replace("“", "").replace("”", "").replace(" ", "")

        # 处理范围的情况，例如："1-5"
        parts = enum_part.split(",")
        result = []
        for part in parts:
            if "-" in part:
                # 默认一定是数字 1-5
                start, end = map(int, part.split("-"))
                result.extend(range(start, end + 1))
            else:
                if part.isdigit():
                    result.append(int(part))
                else:
                    result.append(part)

        # 将结果排序并转换为字符串形式的列表
        result_str = "[" + ", ".join(map(str, sorted(result))) + "]"

        # 替换原始文本中枚举部分
        res = result  # re.sub(pattern, "枚举值为{}".format(result_str), text)

    else:
        pass

    return [res, find]


def _model_find_enum_values(text, enum_list, enum_config):
    # 拼接prompt
    query = f"请从{enum_list}中寻找与{text}语义最接近的词"
    prompt = copy.deepcopy(enum_config["prompt"])
    prompt.append(dict(role="user", content=query))

    inputs = LanguageModelInput(messages=ModelUtil.switch_message(prompt), tools=[])
    model_service = ChatAgentBuilder(
        api_key=enum_config["model"]["api_key"],
        api_base=enum_config["model"]["url"],
        model=enum_config["model"]["model"],
        stream=False,
        top_p=0.0,
    )
    try:
        model_response = model_service.invoke(inputs=inputs).content
        model_res_list = json.loads(model_response)
    except AttributeError:
        model_res_list = []
    return model_res_list


def calculate_edit_distance(word1, word2):
    edit_distance = nltk.edit_distance(word1, word2)
    return edit_distance


def enum_revise(word, enum_list, enum_config):
    res = word
    error_code = FAIL_CODE
    # 如果通过编辑距离找到枚举值
    for enum_value in enum_list:
        if str(word) == enum_value:
            res = enum_value
            error_code = SUCC_CODE
    if error_code:
        return [res, error_code]

        # 调用模型寻找语义相似的枚举值
    model_response = _model_find_enum_values(word, enum_list, enum_config)
    if model_response[-1]:
        res = model_response[0]
        error_code = SUCC_CODE

    return [res, error_code]


def enum_repair(val_extracted, arg_info_for_extract, enum_config):
    """单个枚举型参数的修复"""
    arg_desc = arg_info_for_extract.get(KEY_ARG_DESC)
    if not (arg_info_for_extract and val_extracted and arg_desc):
        return val_extracted

    res = extract_enum_from_text(arg_desc)
    # 如果在description中找到枚举值为[]，匹配
    if not res[1]:
        return val_extracted
    enum_des = res[0]
    revised_arg = enum_revise(val_extracted, enum_des, enum_config)[0]
    revised_arg = revised_arg.strip()
    # 如果出差事由不在枚举值列表中，默认返回其他
    if revised_arg not in enum_des:
        revised_arg = "其他"
    return revised_arg


class EnhancesReact:
    def __init__(self, functions, planning_state, llm_info, query):
        self.funcitions = functions
        self.open_enhances = self.if_start_enhances()
        self.flag_start = self.check_start_flag()
        self.flag_end = self.check_end_flag()
        self.contextlist = planning_state.context["steps"]
        self.llm_info = llm_info
        self.query = query

    def if_start_enhances(self):
        for key in self.funcitions:
            if key.name in function_st:
                return True
        for key in functionmappinglist:
            if len(self.funcitions) == len(functionmappinglist[key]):
                count = 0
                for item in self.funcitions:
                    if item.name in functionmappinglist[key]:
                        count += 1
                if count == len(self.funcitions):
                    return True
        return False

    def check_start_flag(self):
        if self.open_enhances:
            return ["time"]
        else:
            return []

    def check_end_flag(self):
        if self.open_enhances:
            return ["time", "enum"]
        else:
            return []

    def format_target_date(self, chat_history):
        """
        convert the chat history into target data
        """
        if (
            len(chat_history) == 1
            and chat_history[0]["thought"].get(QuestionerKeyword.ROLE)
            == QuestionerKeyword.USER
        ):
            return "用户输入：{user_res}".format(
                user_res=chat_history[0]["thought"].get(QuestionerKeyword.CONTENT)
            )
        if (
            len(chat_history) > 1
            and chat_history[-1]["thought"].get(QuestionerKeyword.ROLE)
            == QuestionerKeyword.USER
        ):
            history = "，".join(
                [
                    "{role}：{content}".format(
                        role=unit["thought"].get(QuestionerKeyword.ROLE),
                        content=unit["thought"].get(QuestionerKeyword.CONTENT),
                    )
                    for unit in chat_history[:-1]
                ]
            )
            return "对话历史：{history}；当前用户输入：{user_res}".format(
                history=history,
                user_res=chat_history[-1]["thought"].get(QuestionerKeyword.CONTENT),
            )
        return json.dumps(chat_history, ensure_ascii=False)

    def strat_query(self):
        try:
            target_data = self.query
            for key in self.flag_start:
                if key == "time":
                    chat_history = self.contextlist
                    target_data = "目标数据：'{target_data}'".format(
                        target_data=self.format_target_date(chat_history)
                    )
                    target_data = enhance_time(target_data)
        except AttributeError:
            target_data = self.query
        return target_data

    def get_fun_key_info(self, function_info, key):
        for item in function_info.params:
            if item.name == key:
                parameters_info = item.description
                outname = item.name
        res = {"name": outname, "desc": parameters_info}
        return res

    def get_zengze_param(self, function_info, type_zengze, key):
        for item in function_info.params:
            if item.name == key:
                parameters_info = item.description
        match = re.search(type_zengze, parameters_info)
        if match:
            target = match.group(1)
        else:
            target = ""
        return target

    def end_param_correct(self, function_info, data_str):
        param_json = json.loads(data_str)
        enum_config = get_action_config(self.llm_info)
        for key in param_json:
            if "time" in self.flag_end:
                try:
                    zhengze = map_zhengze["time"]
                    time_target = self.get_zengze_param(function_info, zhengze, key)
                    if time_target != "":
                        param_json[key] = time_revise(param_json[key], time_target)
                except KeyError:
                    logger.warning("keyError")
            if "enum" in self.flag_end:
                try:
                    self.enum_end_param_correct(
                        enum_config, function_info, key, param_json
                    )
                except KeyError:
                    logger.warning("keyError")
        return json.dumps(param_json, ensure_ascii=False)

    def enum_end_param_correct(self, enum_config, function_info, key, param_json):
        test_query_str = param_json[key]
        arg_info_to_extract = self.get_fun_key_info(function_info, key)
        reskey = ""
        for spkey in enum_spilt_word:
            if spkey in test_query_str:
                testquerylist = test_query_str.split(spkey)
                for tq in testquerylist:
                    tq = tq.strip()
                    backtq = enum_repair(tq, arg_info_to_extract, enum_config)
                    if reskey != "":
                        reskey += ","
                    reskey += str(backtq)
        if reskey != "":
            param_json[key] = reskey
        else:
            param_json[key] = enum_repair(
                test_query_str, arg_info_to_extract, enum_config
            )


def enhance_time(target_data):
    additional_text = ""
    if "对话历史" in target_data:
        # 针对多轮对话的格式构造时间提示语句
        pattern = r"user：(.*?)(?=assistant)|当前用户输入[-:：](.*?)'"
        matches = re.findall(pattern, target_data, flags=re.S)
        if matches:
            user_inputs = [m[0] if m[0] else m[1] for m in matches]
            extracted_data = " ".join(user_inputs)
            additional_text = get_llm_res(extracted_data)
        result = target_data[:-1] + additional_text + target_data[-1]
        return result

    # 针对单轮对话的格式构造时间提示语句
    pattern = r"目标数据：'用户输入[-:：](.+)'"
    match = re.search(pattern, target_data)
    if match:
        extracted_data = match.group(1)
        additional_text = get_llm_res(extracted_data)

    result = target_data[:-1] + additional_text + target_data[-1]
    return result


def get_llm_res(text):
    system_prompt = [
        "你生成综合质量（包括有用性，事实性，无害性）极好的回复。",
        '你是一个日期抽取助手，请遵从以下规则：1.请从用户输入中提取时间信息并将时间信息描述统一。2.如果没有时间信息，请回复空列表，输出格式是:[]。3.如果存在时间跨度，请正确计算出结束日期时间。4.输出格式必须是：["时间信息", "时间信息"]。\n\n示例如下：\n'
        '1.文本：下下周一出差，周五回来。输出： ["下下周一", "下下周五"]。\n'
        '2.文本：我下个月1号去北京进行培训，做火车，预计15天后回来。输出： ["下个月1号", "下个月16号"]。\n'
        '3.文本：下个月的第二个星期三我将去武汉开展业务，一周后回来，我会选择租车。输出： ["下个月的第二个星期三", "下个月的第三个星期三"]。\n'
        '4.文本：明天9点开会。结束时间12点。输出： ["明天9点", "明天12点"]。\n'
        '5.文本：后天8点到9点开会。输出： ["后天8点", "后天9点"]。\n'
        '6.文本：明天上午8-9点。输出： ["明天上午8点", "明天上午9点"]。\n'
        '7.文本：明天下午8-9点我要去约会 改到后天下午。输出： ["后天下午8点", "后天下午9点"]。'
        '8.文本：本周三有个约会。输出： ["这周三"]。'
        '9.文本：本周三上午9点15开始，持续三个小时。输出：["本周三上午9:15", "本周三上午12:15"]'
        '10.文本：我要出去骑行，骑到后天回来，今天就出发。输出：["今天", "后天"]'
        '11.文本：本月3号出去游玩。输出： ["这个月3号"]。'
        '12.文本：定个今天晚上的会议室，8点10分开始，持续一个半小时。输出：["今天晚上8:10", "今天晚上9:40"]'
        "13.文本：我喜欢吃香蕉。输出：[]。\n",
    ]

    input_data = f"[unused9]系统：{system_prompt[0]}[unused10][unused9]系统：{system_prompt[1]}[unused10][unused9]用户：{text}[unused10][unused9]助手："

    url = os.environ.get("time_model_url")
    payload = json.dumps(
        {
            "data": [input_data],
            "with_prompt": True,
            "decode_strategy": {"temperature": 0.2, "top_p": 0.15},
        }
    )
    headers = {"Content-Type": "application/json"}

    response = requests.request(
        "POST", url, headers=headers, data=payload, timeout=HTTP_TIMEOUT
    ).json()
    llm_res = response["answers"][0]["content"].replace("，", ",")
    # 标准时间格式
    time_entities = jio.ner.extract_time(
        llm_res, time_base=time.time(), with_parsing=True
    )
    # 差旅助手 2024年4月18日
    time_entities = jio.ner.extract_time(
        llm_res, time_base=1713369600.0, with_parsing=True
    )

    res = ""

    if not time_entities:
        return res

    try:
        list(llm_res)
    except Exception:
        return res

    for entities in time_entities:
        time_param = entities["text"]
        time_type = entities["type"]
        if time_type == "time_span":
            start = entities["detail"]["time"][0][:-3]
            end = entities["detail"]["time"][1][:-3]
            standard_time = "{} 到 {}".format(start, end)
        elif time_type == "time_point":
            standard_time = entities["detail"]["time"][0][:-3]
        else:
            standard_time = entities["detail"]["time"][:-3]
        temp = "{} 是 {}, ".format(time_param, standard_time)
        res += temp
    result = (
        f"\n时间提示：<{res[:-2]}>"
        if len(list(llm_res)) == len(time_entities)
        else f"\n时间提示：<{res[:-2]},{llm_res}>"
    )
    return result


def time_revise(time_param, target_format):
    """
    Returns: [时间修正值，错误码]
    Example1：
    time_revise("2024-07-24 12:00:00", "%m-%d") 输出==> ['07-24', 1]
    Example2：
    time_revise("24-Jul-2024 10:30:00", "%m/%d/%Y %H:%M:%S") 输出==> ['07/24/2024 10:30:00', 1]
    Example3:
    time_revise("24-Jul-2024 25:30", "%m/%d/%Y %H:%M:%S") 输出==> ['24-Jul-2024 25:30', 0]
    """
    timechange_mapping = {
        "YYYY-MM-DD": "%Y-%m-%d",
        "YYYY-MM-DD HH:MM": "%Y-%m-%d %H:%M",
        "YYYY-MM-DD HH": "%Y-%m-%d %H",
        "YYYY-MM": "%Y-%m",
        "YYYY": "%Y",
        "MM-DD": "%m-%d",
    }
    dellist = ["<", ">"]
    for key in dellist:
        time_param = time_param.replace(key, "")
    res = time_param
    if target_format in timechange_mapping:
        target_format = timechange_mapping[target_format]
    error_code = FAIL_CODE
    # 解析日期字符串为 datetime 对象
    try:
        date_obj = parser.parse(time_param)
    except BaseException:
        return res

    # 将日期对象按目标格式进行格式化
    formatted_date = date_obj.strftime(target_format)
    res = formatted_date
    error_code = SUCC_CODE
    return res


if __name__ == "__main__":
    test_query_str = "火车"
    arg_info_to_extract = {
        "name": "traveltool",
        "desc": "交通方式，枚举值为[火车，飞机]",
    }
    res = enum_repair(test_query_str, arg_info_to_extract)
    logger.info("enum_repair result: %s", str(res))
