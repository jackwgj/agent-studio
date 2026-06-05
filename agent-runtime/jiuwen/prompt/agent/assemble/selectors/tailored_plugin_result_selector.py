#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""handles the conversion between message list and template."""

import json
from typing import List

from jiuwen.executor_sdk.operator.impl.common.utils import executor_logger

FUNCTION_RESULT_TRUNCATE_LENGTH = 1500


class TailoredPluginResultSelector:
    """plugin res selector tailor for two function."""

    def __init__(
        self,
        refiner_config: dict = None,
        function_result_truncate_length: int = FUNCTION_RESULT_TRUNCATE_LENGTH,
    ):
        self.refiner_config = refiner_config
        self.function_result_truncate_length = function_result_truncate_length
        if self.refiner_config is not None:
            self.tool_result_selection_pattern = self.refiner_config.get(
                "toolResultSelection", None
            )
        else:
            self.tool_result_selection_pattern = None
        self.mode = "null"
        if self.tool_result_selection_pattern is not None:
            self.mode = (
                self.tool_result_selection_pattern.get("mode")
                if self.tool_result_selection_pattern.get("mode")
                else "null"
            )
            self.rule = (
                self.tool_result_selection_pattern.get("rule", {})
                if self.tool_result_selection_pattern.get("rule", {})
                else {}
            )

    def select_function_results(self, history: List[dict]) -> List[dict]:
        """select_function_results"""
        current_query = ""
        for message in history:
            content = message["content"]
            if message["role"] == "user":
                current_query = content
            function_call = message.get("tool_calls", [None])[0]
            if function_call:
                function = function_call.get("function", {})
                function_name = function.get("name")
                if function_name == "KnowledgeRetrieve":
                    current_query = json.loads(function.get("arguments", "{}")).get(
                        "query", current_query
                    )
                if function_name == "PoiRetrieve":
                    current_query = json.loads(function.get("arguments", "{}")).get(
                        "type", current_query
                    )
            if message["role"] == "tool":
                func_name = message["name"]
                if self.mode in ("null", "model"):
                    if func_name == "KnowledgeRetrieve":
                        selected_content = self.parse_knowledge_retrieve(
                            content=content, current_query=current_query
                        )
                    elif func_name == "PoiRetrieve":
                        selected_content = self.parse_poi_retrieve(
                            content=content, current_query=current_query
                        )
                    else:
                        selected_content = content[
                            : self.function_result_truncate_length
                        ]
                elif self.mode == "rule":
                    if func_name in self.rule:
                        try:
                            plugin_res = json.loads(content)
                            for key_name in self.rule.get(func_name, []):
                                plugin_res.pop(key_name, "")
                            selected_content = json.dumps(
                                plugin_res, ensure_ascii=False
                            )
                        except Exception as error:
                            executor_logger().error(str(error))
                            selected_content = content[
                                : self.function_result_truncate_length
                            ]
                    else:
                        selected_content = content[
                            : self.function_result_truncate_length
                        ]
                else:
                    selected_content = content[: self.function_result_truncate_length]

                message["content"] = selected_content
        return history

    def parse_knowledge_retrieve(self, content, current_query):
        """parse_knowledge_retrieve"""
        executor_logger().info(
            "Prompt assembler get knowledge retrieve function content"
        )
        try:
            res_list = (
                json.loads(content)
                .get("result")
                .get("abilityInfos")[0]
                .get("actionExecutorResult")
                .get("reply")
            )
            if not res_list:
                selected_content = "未能搜索到相关结果"
            else:
                answer_list = [content.get("answer") for content in res_list]
                func_result = "\n".join(
                    [f"第{idx + 1}条结果：{a}" for idx, a in enumerate(answer_list)]
                )
                selected_content = (
                    "以下是多条搜索结果：\n"
                    + func_result[: self.function_result_truncate_length]
                    + f"\n\n请根据以上搜索结果回答用户问题。\n\n目标：\n1. 根据用户问题分析搜索结果，"
                    f"选择与问题相关的搜索结果进行回答。\n2. 如果搜索结果全部不相关，请拒绝回答。"
                    f"\n3.根据搜索结果进行回答，不要编造答案\n4. 答案要保证概括性、准确性并尽可能是"
                    f"最新的信息。\n\n用户的问题是：{current_query}"
                )
        except Exception:
            executor_logger().error(
                "Prompt assembler failed to load KnowledgeRetrieve plugin result"
            )
            selected_content = content[: self.function_result_truncate_length]
        return selected_content

    def parse_poi_retrieve(self, content, current_query):
        """parse_poi_retrieve"""
        executor_logger().info(
            f"Prompt assembler get poi retrieve function content: {content}"
        )
        try:
            ability_infos = json.loads(content).get("result").get("abilityInfos")
            if ability_infos:
                dest_list = []
                for des in ability_infos[0].get("actionExecutorResult").get("reply"):
                    dest_list.append(des.get("destName"))
                selected_content = (
                    f"距离您最近的几个{current_query}如下：\n"
                    + "\n".join([f"[{idx + 1}]{a}" for idx, a in enumerate(dest_list)])
                )
                selected_content = selected_content[
                    : self.function_result_truncate_length
                ]
            else:
                selected_content = "未能查询到相关地点"
        except Exception:
            executor_logger().error(
                "Prompt assembler failed to load PoiRetrieve plugin result"
            )
            selected_content = content[: self.function_result_truncate_length]
        return selected_content
