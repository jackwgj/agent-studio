#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
@Copyright: Copyright (C) 2023-2023 Huawei Inc
@Project:
@Author: jiuwen planner
@Date: 2023-10-18
@LastEditTime: 2023-10-18 17:03:17
@Description: propose next
"""

from abc import ABC

from jiuwen.planner.common import OpType
from jiuwen.planner.planning_modules.base import LlmPlanningModule, PromptPlanningModule

ROLE = "role"
CONTENT = "content"


class ProposeNext(LlmPlanningModule, ABC):
    """
    LLM operator of proposing next step
    """

    def __init__(self, plan_config, model):
        super(ProposeNext, self).__init__(
            op_type=OpType.ProposeNext, plan_config=plan_config, model=model
        )

    def create_llm_input(self, **kwargs):
        prompt_info = kwargs.get("prompt_info")
        prompt_engine = PromptPlanningModule()
        prompt = prompt_engine.format(
            template_name=self.op_config.prompt_template_name, keywords=prompt_info
        )

        if self.op_config.is_chat:
            planning_state = kwargs.get("planning_state")
            steps = planning_state.context.get("steps")
            steps = steps[:1] + steps[1:][-self.op_config.reserved_max_chat_rounds :]
            for step in steps:
                prompt.append(step)
        return prompt

    def update_planning_state(self, **kwargs):
        llm_output = kwargs.get("llm_output")
        role = llm_output.get(ROLE)
        content = llm_output.get(CONTENT)
        function_call = llm_output.get("function_call")

        planning_state = kwargs.get("planning_state")
        generated_functions: dict = planning_state.functions
        if function_call and function_call.get("name"):
            # 留作workflow场景判断
            generated_functions[function_call.get("name")] = function_call
            planning_state.functions = generated_functions
            planning_state.last_func = function_call
            cur_thought = {ROLE: role, CONTENT: content, "function_call": function_call}
        else:
            cur_thought = {ROLE: role, CONTENT: content}
        ## 将大模型生成的工具调用消息加入消息队列
        planning_state.tool_msg = cur_thought
        planning_state.add(cur_thought)
