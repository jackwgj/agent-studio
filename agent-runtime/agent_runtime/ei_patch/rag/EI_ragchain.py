#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""RAF Chain
支持文档检索/网页检索混合模型，Rag_chain_config输出模型调用先后顺序
## RAG模式不与用户进行交互
## 知识检索模式：1、融合检索模式。两个库并行查询并对比总结；2、知识库优先模式；3、网页搜索优先模型；
## 知识库检索策略暂时不加 待后续与KooSearch联调后处理
## 表格问答单独增加一列暂时不加，1230需求增加文档检索与表格检索混合检索模式
## 检索模式各种参数本次暂时不对外开放用户修改，以默认修改模式处理
## step1: 前置改写，1、上下文信息补充；2、时间信息补充；3、检索规划，复杂问题拆分 -- 这个挪到ReAct模式里面
## 前置改写增加动态样例补充能力，预制部分拆解数据库
## step2: 分步骤按着要求查询数据库。1、混合检索直接拼接结果生成；2、知识库优先检索，若没生成结果则进入网页搜索优先模型；3、网页搜索优先模型检索，若没有搜到则进入知识库优先模型
## 网页检索结果总结时基于结果，若结果没有检索，动态生成三个相似检索词再次检索(该方案选配)
## 最终进行答案总结。总结答案作为本段工具输出
"""

import json
import os
import re
import time

import requests
from jiuwen.common.log.base import logger
from jiuwen.controller.common.enum import RetCode, OutputResultType
from jiuwen.planner.common.plan_constants import TimerInfo
from jiuwen.planner.common.plan_runtime_context import PlanRuntimeContext
from jiuwen.planner.common.result_output_callback import ResultOutputStreamCallback
from jiuwen.prompt import Template, Prompt
from rag.rag import rag_config
from rag.stream_post_utils import stream_function_call

HTTP_TIMEOUT = os.getenv("HTTP_TIMEOUT", "15")

functions = []
search_plan_prompts = rag_config["search_plan_prompts"].replace("\\n", "\n")
search_summary_prompts = rag_config["search_summary_prompts"].replace("\\n", "\n")
finall_summary = rag_config["finall_summary"].replace("\\n", "\n")
searchexample = rag_config["searchexample"].replace("\\n", "\n")
NON_EXIST_FUNC = "大模型工具调用失败"
ROLE = "role"
USER = "user"
NAME = "name"
FUNCTION = "function"
CONTENT = "content"
SEARCHTYPE = "db_first"
SEARCHQUERYLIST = "query"

TAG = " === EI_ragchain === "


def refix_llm_output(input_str):
    res = input_str
    json_pattern = r"\{.*\}"
    match = re.search(json_pattern, input_str, re.DOTALL)
    if match:
        res = match.group(0)
        res = (
            res.replace("false", "False")
            .replace("true", "True")
            .replace("null", "None")
        )
    else:
        return input_str
    if "</cot>" in res:
        tmp = res.split("</cot>")
        res = tmp[-1]
    return res


def start_rag_line(ir_data):
    try:
        configs_info = ir_data["configs"]
        if configs_info["mode"] != "RAG":
            return False
        elif configs_info.get("workflows"):
            return False
        elif configs_info.get("mcps"):
            return False
        if not configs_info.get("plugins"):
            return False
        for pluginsitem in configs_info["plugins"]:
            if (
                "search" not in pluginsitem["name"]
                and "retrieval" not in pluginsitem["name"]
            ):
                return False
            for args in pluginsitem["arguments"]:
                if args["visible"] is False:
                    continue
                if args["required"] and args["name"] != SEARCHQUERYLIST:
                    return False
        return True
    except Exception as e:
        logger.error("rag error", e)
        return False


def get_search_type(search_type, webfun, dbfun):
    if dbfun is None and webfun is None:
        return ""
    elif dbfun is not None and webfun is None:
        return "db_first"
    elif dbfun is None and webfun is not None:
        return "web_first"
    elif search_type not in rag_config["search_typelist"]:
        return ""
    else:
        return search_type


def get_fun_info(funlist, fun_name):
    funinfo = None
    for tmpfuninfo in funlist:
        if tmpfuninfo.tool_id == fun_name:
            funinfo = tmpfuninfo
    return funinfo


class EIRagChain:
    def __init__(self, llm_model, rag_chain_config, inputfunctions):
        # 这里的检索配置
        self.llm_model = llm_model
        self.webapi = get_fun_info(inputfunctions, "websearch")
        self.dbapi = get_fun_info(inputfunctions, "retrieval")
        # 这里涉及3种模式，db_first, web_first,parallel
        self.search_type = rag_chain_config.get(
            "search_type", rag_config["Rag_chain_config"]["search_type"]
        )
        self.search_type = get_search_type(self.search_type, self.webapi, self.dbapi)
        self.dynamic_example_config = rag_chain_config.get(
            "dy_example_config", rag_config["Rag_chain_config"]["dy_example_config"]
        )
        self.result_output_callback = ResultOutputStreamCallback()

    async def excuter_fun(self, tool_arguments, funobj, planning_state, **kwargs):
        runtime_context: PlanRuntimeContext = (
            kwargs.get("runtime_context") or PlanRuntimeContext()
        )
        try:
            start_time = time.time()
            function_exec_res = {}
            if funobj is not None:
                function_exec_res = await funobj.ainvoke(
                    inputs={
                        "tool_name": funobj.tool_id,
                        "tool_arguments": tool_arguments,
                    },
                    runtime_auth={
                        "headers": runtime_context.api_config.get("headers", {})
                    },
                    **kwargs,
                )
            exec_latency = round(time.time() - start_time, 2)
        except Exception as e:
            logger.warning("fun invoke Exception: %s", e)
            function_exec_res, exec_latency = (
                dict(errCode=-1, errMessage=NON_EXIST_FUNC),
                0.0,
            )
        return function_exec_res, exec_latency

    async def rag_search(self, query, planning_state, **kwargs):
        tool_arguments = json.dumps({"query": query})
        data = await self.excuter_fun(
            tool_arguments, self.dbapi, planning_state, **kwargs
        )
        top_k = rag_config["DB_search_info"]["config"]["top_k"]
        score_line = rag_config["DB_search_info"]["config"]["score_line"]
        res_text = ""
        try:
            doc_list = json.loads(data)["docList"]
            for doc in doc_list[:top_k]:
                page_content = doc["content"]
                if "score" in doc and score_line != 0:
                    if doc["score"] > score_line and page_content not in res_text:
                        res_text += page_content + "\n"
                else:
                    res_text += page_content + "\n"
        except Exception:
            res_text += str(data)
            logger.warning("rag search KeyError, Unknown Reasons.")
        return res_text

    async def web_search(self, query, planning_state, **kwargs):
        tool_arguments = json.dumps({"query": query})
        data = await self.excuter_fun(
            tool_arguments, self.webapi, planning_state, **kwargs
        )
        # 根据query获得检索结果，top_k为取前几个结果
        top_k = rag_config["DB_search_info"]["config"]["top_k"]
        res_text = ""
        i = 1
        for doc in data[0]["data"]["web_pages"][:top_k]:
            if not isinstance(doc, dict):
                continue
            if "content" not in doc:
                continue
            page_content = doc["content"]
            res_text += "检索结果" + str(i) + ":" + page_content + "\n"
            i += 1
        return res_text

    def dy_example(self, query: str, dy_config):
        rag_url = dy_config["dy_url"]
        response = requests.post(
            url=rag_url,
            json={
                "query": query,
            },
            stream=True,
            timeout=HTTP_TIMEOUT,
        )
        data = response.json().get("data")
        return data

    def get_llm_inputs(self, sys2, inputstr):
        msg_list = [
            {"role": "system", "content": sys2},
            {"role": "user", "content": inputstr},
        ]
        return msg_list

    @staticmethod
    def _format_yield_from_llm_output(result, llm_output: dict):
        for item in result:
            if (
                isinstance(item, dict) and "llm_output" in item
            ):  # 大模型的最终输出，不需要流式输出
                llm_output.update(item.get("llm_output"))
            elif isinstance(item, dict) and "time_consumption" in item:
                pass
            else:
                yield item

    @staticmethod
    def _format_yield_from_llm_output_not_yield(result, llm_output: dict):
        for item in result:
            if (
                isinstance(item, dict) and "llm_output" in item
            ):  # 大模型的最终输出，不需要流式输出
                llm_output.update(item.get("llm_output"))

    def get_historyinfo(self, inputstr, planning_state):
        res = ""
        prompt_ex = Prompt(template=Template(name="search_ex", content=searchexample))
        current_timestamp = time.time()
        local_time = time.localtime(current_timestamp)
        curr_time = time.strftime("%Y年%m月%d日", local_time)
        historyinfo = planning_state.context["steps"][:-1]
        userinfo = {"role": "user", "content": inputstr}
        try:
            res = prompt_ex.invoke(
                inputs={
                    "curr_time": curr_time,
                    "historyinfo": str(historyinfo),
                    "userinfo": str(userinfo),
                }
            )
        except AttributeError:
            res = inputstr
        return res

    async def sub_search(self, search_type, sub_query, planning_state, **kwargs):
        toolname = "retrieval"
        if search_type == "web_first":
            toolname = "websearch"
        function_call = dict(
            name=toolname,
            arguments=json.dumps(dict(query=sub_query), ensure_ascii=False),
        )
        for item in ResultOutputStreamCallback().on_function_call_generated(
            func_call=function_call, time_info=TimerInfo()
        ):
            yield item
        if_something_searched = False
        if search_type == "db_first":
            sub_answer = await self.rag_search(sub_query, planning_state, **kwargs)
            if_something_searched = bool(sub_answer.strip())
        elif search_type == "web_first":
            sub_answer = await self.web_search(sub_query, planning_state, **kwargs)
            if_something_searched = bool(sub_answer.strip())
        else:
            sub_answer1 = await self.rag_search(sub_query, planning_state, **kwargs)
            sub_answer2 = await self.web_search(sub_query, planning_state, **kwargs)
            if_something_searched = bool(sub_answer1.strip() + sub_answer2.strip())
            sub_answer = (
                "<数据库检索结果>" + sub_answer1 + "\n<网页检索结果>" + sub_answer2
            )

        for item in ResultOutputStreamCallback().on_tool_executed(
            toolname, toolname, sub_answer, True, TimerInfo()
        ):
            yield item
        if if_something_searched:
            prompt_summary = Prompt(
                template=Template(name="sub_summary", content=search_summary_prompts)
            )
            prompt_summary_sys = prompt_summary.invoke(inputs={"quotes": sub_answer})
            prompt_summary_llm = self.get_llm_inputs(prompt_summary_sys, sub_query)
            llm_output_dict = dict()
            sub_query_res = stream_function_call(
                prompt_summary_llm, functions, self.llm_model
            )
            self._format_yield_from_llm_output_not_yield(sub_query_res, llm_output_dict)
        else:  # 若检索结果为空，则跳过模型总结
            llm_output_dict = {
                "role": "assistant",
                "content": json.dumps(
                    {"检索结果是否相关": "否", "搜索结果总结": "未检索到相关内容\n"},
                    ensure_ascii=False,
                ),
            }
        yield llm_output_dict, "final_return"

    async def stream(self, inputstr, planning_state, **kwargs):
        # 检索规划模块
        searchinput = self.get_historyinfo(inputstr, planning_state)
        search_plan_query = self.get_llm_inputs(search_plan_prompts, searchinput)
        llm_output_dict = dict()
        res = stream_function_call(search_plan_query, functions, self.llm_model)
        self._format_yield_from_llm_output_not_yield(res, llm_output_dict)
        content = refix_llm_output(llm_output_dict.get("content", "{}"))
        sub_queries = json.loads(content).get("查询子句", "")
        change_query = json.loads(content).get("改写问题", inputstr)
        sub_queries = sub_queries or [inputstr]
        # 分步检索
        res_all = ""
        for sub_query in sub_queries:
            try:
                async for item in self.sub_search(
                    self.search_type, sub_query, planning_state, **kwargs
                ):
                    if not isinstance(item, tuple):
                        yield item
                        continue

                    llm_output_dict, _ = item
                llm_output_dict = (
                    refix_llm_output(llm_output_dict)
                    if isinstance(llm_output_dict, str)
                    else llm_output_dict
                )
                llm_output_dict = self.is_json(llm_output_dict)
                llm_content = llm_output_dict.get("content", "{}")
                # 去除多余的标记
                llm_content = refix_llm_output(llm_content)
                llm_content = (
                    llm_content.replace("```json", "").replace("```", "").strip()
                )
                if_relate = json.loads(llm_content).get("检索结果是否相关", "是")
                if if_relate == "否":
                    if self.search_type == "db_first" and self.webapi is not None:
                        async for item in self.sub_search(
                            "web_first", sub_query, planning_state, **kwargs
                        ):
                            if not isinstance(item, tuple):
                                yield item
                                continue
                            llm_output_dict, _ = item
                    elif self.search_type == "web_first" and self.dbapi is not None:
                        async for item in self.sub_search(
                            "db_first", sub_query, planning_state, **kwargs
                        ):
                            if not isinstance(item, tuple):
                                yield item
                                continue
                            llm_output_dict, _ = item
                try:
                    llm_output_dict = self.is_json(llm_output_dict)
                    sub_answer_json = llm_output_dict.get("content", "{}")
                    sub_answer_json = refix_llm_output(sub_answer_json)
                    sub_answer_json = (
                        sub_answer_json.replace("```json", "")
                        .replace("```", "")
                        .strip()
                    )
                    sub_answer_json = self.is_json(sub_answer_json)
                    sub_answer = sub_answer_json.get("搜索结果总结", "")
                except KeyError:
                    sub_answer = llm_output_dict.get("content", "{}")
            except KeyError:
                sub_answer = "未检测到相关内容"
            quotes = sub_query + "：" + sub_answer
            res_all += quotes + "\n"
        prompt_summary = Prompt(
            template=Template(name="final_summary", content=finall_summary)
        )
        prompt_summary_sys = prompt_summary.invoke(inputs={"quotes": res_all})
        prompt_summary_llm = self.get_llm_inputs(prompt_summary_sys, change_query)
        for item in ResultOutputStreamCallback().on_llm_output_stream_started():
            yield item
        llm_output_dict3 = dict()
        outputs = stream_function_call(prompt_summary_llm, functions, self.llm_model)
        for item in self._format_yield_from_llm_output(outputs, llm_output_dict3):
            yield item
        for item in ResultOutputStreamCallback().on_llm_output_stream_end(
            OutputResultType.LLM_FUNCTION_CALL
        ):
            yield item
        outputs = llm_output_dict3.get("content", "{}")
        planning_state.report_message = dict(
            result=f"{outputs}#*#*#{json.dumps(dict(), ensure_ascii=False)}"
        )
        yield RetCode.SUCCESS

    def is_json(self, content_str):
        res_json = {}
        if isinstance(content_str, str):
            try:
                # 尝试解析字符串为 JSON
                content_json = json.loads(content_str)
                res_json = content_json
            except json.JSONDecodeError as e:
                logger.error("is_json() func err，not JSON str：", e)
        if isinstance(content_str, dict):
            res_json = content_str
        return res_json
