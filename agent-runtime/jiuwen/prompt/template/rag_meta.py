#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
rag meta template interface
"""

import threading
from queue import Queue
from typing import List

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.prompt.mining.template.meta_template.meta_template_type import (
    MetaTemplateType,
)
from jiuwen.prompt.template.base import MetaTemplate, ModularMetaTemplate


class RagMeta(ModularMetaTemplate):
    """RagMeta"""

    def build_prompt(self, instruction: str = "", tools: List[dict] = None):
        """build prompt interface"""

        def build_plan_meta(pre_output_queue, suf_output_queue):
            """rag meta build plan meta"""
            plan_meta_generated = ""
            try:
                for token in plan_meta_miner.build_prompt(
                    instruction=instruction, tools=tools
                ):
                    if token.get("code") != 0:
                        pre_output_queue.put(token)
                    for t in token.get("data", ""):
                        if plan_meta_keyword_pre[1:] not in plan_meta_generated:
                            pre_output_queue.put(t)
                        else:
                            pre_output_queue.put(StopIteration)
                        if plan_meta_keyword_suf[1:] in plan_meta_generated:
                            suf_output_queue.put(t)
                        plan_meta_generated += t
            except JiuWenBaseException as error:
                pre_output_queue.put(
                    dict(code=error.error_code, message=error.message, data="")
                )
            except Exception as _:
                pre_output_queue.put(
                    dict(
                        code=StatusCode.PROMPT_UNEXPECT_ERROR.code,
                        message=StatusCode.PROMPT_UNEXPECT_ERROR.errmsg.format(
                            error_msg="Rag meta error"
                        ),
                        data="",
                    )
                )
            finally:
                pre_output_queue.put(StopIteration)
                suf_output_queue.put(StopIteration)

        def get_tools_priority_description(tools_list):
            description_list = []
            general_search_tool_priority_dict = {
                "retrieval": 3,
                "duckduckgo_search": 2,
                "search_wikipedia": 1,
            }
            tools_dict = {
                "retrieval,duckduckgo_search,search_wikipedia": "如果用户的问题可以通过本地数据库查询得到答案，优先使用retrieval工具。\n"
                "如果retrieval工具查询本地数据库无法得到答案，则使用duckduckgo_search工具进行搜索。\n"
                "如果使用duckduckgo_search工具进行搜索无法得到答案，则使用search_wikipedia工具进行查询，请按如下顺序执行：\n"
                "retrieval > duckduckgo_search > search_wikipedia",
                "retrieval,search_wikipedia": "如果用户的问题可以通过本地数据库查询得到答案，优先使用retrieval工具。\n"
                "如果retrieval工具查询本地数据库无法得到答案，则使用search_wikipedia工具进行查询，请按如下顺序执行：\n"
                "retrieval > search_wikipedia",
                "retrieval,duckduckgo_search": "如果用户的问题可以通过本地数据库查询得到答案，优先使用retrieval工具。\n"
                "如果retrieval工具查询本地数据库无法得到答案，则使用duckduckgo_search工具进行搜索，请按如下顺序执行：\n"
                "retrieval > duckduckgo_search",
                "retrieval": "如果用户的问题可以通过本地数据库查询得到答案，优先使用retrieval工具，请按如下顺序执行：\n"
                "retrieval",
                "duckduckgo_search,search_wikipedia": "如果用户的问题可以通过duckduckgo_search工具查询得到答案，优先使用duckduckgo_search工具进行搜索。\n"
                "如果使用duckduckgo_search工具进行搜索无法得到答案，则使用search_wikipedia工具进行查询，请按如下顺序执行：\n"
                "duckduckgo_search > search_wikipedia",
                "duckduckgo_search": "如果用户询问的是通用问题，则使用duckduckgo_search工具进行搜索，请按如下顺序执行：\n"
                "duckduckgo_search",
                "search_wikipedia": "如果用户询问的是一般知识性问题，则使用search_wikipedia工具进行搜索，请按如下顺序执行：\n"
                "search_wikipedia",
                "search_current_weather": "如果用户询问的是天气信息，使用search_current_weather工具进行查询，请按如下顺序执行：\n"
                "search_current_weather",
                "search_arxiv": "如果用户询问的是学术论文信息，使用search_arxiv工具进行查询，请按如下顺序执行：\n"
                "search_arxiv",
            }
            general_search_tool_list = []
            special_search_tool_list = []

            for _, tool in enumerate(tools_list):
                tool_name = tool.get("name", "")
                if not tool_name:
                    continue
                if tool_name in general_search_tool_priority_dict:
                    general_search_tool_list.append(tool_name)
                else:
                    special_search_tool_list.append(tool_name)

            if general_search_tool_list:
                general_search_tool_list = sorted(
                    general_search_tool_list,
                    key=lambda x: -general_search_tool_priority_dict.get(x),
                )
                general_key = ",".join(general_search_tool_list)
                description_list.append(tools_dict.get(general_key))

            for tool in special_search_tool_list:
                if tool in tools_dict:
                    description_list.append(tools_dict[tool])

            tool_description = ""
            for idx, content in enumerate(description_list):
                if idx < len(description_list) - 1:
                    tool_description += f"{idx + 1}. {content}\n"
                else:
                    tool_description += f"{idx + 1}. {content}"

            return tool_description

        plan_meta_miner = MetaTemplate.create(
            name=MetaTemplateType.PLAN_META_EPO.value,
            stream=True,
            model_info=self.model_info,
        )
        if not plan_meta_miner:
            raise ValueError("rag plan meta miner is empty!")
        plan_meta_keyword_pre = "## 工具间约束\n- **依赖与偏好**："
        plan_meta_keyword_suf = "## 风格与个性"
        tools_priority_description = get_tools_priority_description(tools)
        tool_description = "\n" + tools_priority_description + "\n\n"
        plan_meta_generator_pre = Queue()
        plan_meta_generator_suf = Queue()
        thread_build = threading.Thread(
            target=build_plan_meta,
            args=(plan_meta_generator_pre, plan_meta_generator_suf),
        )
        thread_build.start()

        while True:
            token = plan_meta_generator_pre.get()
            if token is StopIteration:
                break
            if isinstance(token, dict):
                yield token
            yield dict(code=0, message=self.success_msg, data=token)

        if tools_priority_description:
            for token in tool_description:
                yield dict(code=0, message=self.success_msg, data=token)

        if plan_meta_generator_suf.qsize() > 1:
            for token in plan_meta_keyword_suf:
                yield dict(code=0, message=self.success_msg, data=token)

        while True:
            token = plan_meta_generator_suf.get()
            if token is StopIteration or token == "#":
                break
            yield dict(code=0, message=self.success_msg, data=token)
