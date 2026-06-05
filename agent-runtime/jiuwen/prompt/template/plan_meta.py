#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
plan meta template
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


class PlanMeta(ModularMetaTemplate):
    """
    plan meta implement
    """

    def build_prompt(self, instruction: str = "", tools: List[dict] = None):
        """build prompt interface"""

        def build_plan_meta(output_queue_pre, output_queue_suf):
            plan_meta_generated = ""
            try:
                for token in plan_meta_miner.build_prompt(
                    instruction=instruction, tools=tools
                ):
                    if token.get("code") != 0:
                        output_queue_pre.put(token)
                    for t in token.get("data", ""):
                        if plan_meta_keyword_pre[1:] not in plan_meta_generated:
                            output_queue_pre.put(t)
                        else:
                            output_queue_pre.put(StopIteration)
                        if plan_meta_keyword_suf[1:] in plan_meta_generated:
                            output_queue_suf.put(t)
                        plan_meta_generated += t
            except JiuWenBaseException as error:
                output_queue_pre.put(
                    dict(code=error.error_code, message=error.message, data="")
                )
            except Exception as _:
                output_queue_pre.put(
                    dict(
                        code=StatusCode.PROMPT_UNEXPECT_ERROR.code,
                        message=StatusCode.PROMPT_UNEXPECT_ERROR.errmsg.format(
                            error_msg="Plan meta error"
                        ),
                        data="",
                    )
                )
            finally:
                output_queue_pre.put(StopIteration)
                output_queue_suf.put(StopIteration)

        plan_meta_miner = MetaTemplate.create(
            name=MetaTemplateType.PLAN_META_EPO.value,
            stream=True,
            model_info=self.model_info,
        )
        if not plan_meta_miner:
            raise ValueError("plan meta miner is empty!")
        plan_meta_keyword_pre = "## 工具间约束\n- **依赖与偏好**："
        plan_meta_keyword_suf = "## 风格与个性"
        plan_meta_generator_pre = Queue()
        plan_meta_generator_suf = Queue()
        threading.Thread(
            target=build_plan_meta,
            args=(plan_meta_generator_pre, plan_meta_generator_suf),
        ).start()

        def build_role_meta(output_queue):
            role_meta_generated = ""
            try:
                for token in role_meta_miner.build_prompt(
                    instruction=instruction, tools=tools
                ):
                    if token.get("code") != 0:
                        output_queue.put(token)
                    for t in token.get("data", ""):
                        if role_meta_keyword_pre[1:] in role_meta_generated:
                            output_queue.put(t)
                        role_meta_generated += t
            except JiuWenBaseException as error:
                output_queue.put(
                    dict(code=error.error_code, message=error.message, data="")
                )
            except Exception as _:
                output_queue.put(
                    dict(
                        code=StatusCode.PROMPT_UNEXPECT_ERROR.code,
                        message=StatusCode.PROMPT_UNEXPECT_ERROR.errmsg.format(
                            error_msg="Plan meta error"
                        ),
                        data="",
                    )
                )
            finally:
                output_queue.put(StopIteration)

        role_meta_miner = MetaTemplate.create(
            name=MetaTemplateType.ROLE_TOOL_META.value,
            stream=True,
            model_info=self.model_info,
        )
        if not role_meta_miner:
            raise ValueError("role meta miner is empty!")

        role_meta_keyword_pre = "## 规则"
        role_meta_generator = Queue()
        threading.Thread(target=build_role_meta, args=(role_meta_generator,)).start()

        while True:
            token = plan_meta_generator_pre.get()
            if token is StopIteration:
                break
            if isinstance(token, dict):
                yield token
            yield dict(code=0, message=self.success_msg, data=token)

        while True:
            token = role_meta_generator.get()
            if token is StopIteration or token == "#":
                break
            if isinstance(token, dict):
                yield token
            yield dict(code=0, message=self.success_msg, data=token)

        for token in plan_meta_keyword_suf:
            yield dict(code=0, message=self.success_msg, data=token)

        while True:
            token = plan_meta_generator_suf.get()
            if token is StopIteration or token == "#":
                break
            yield dict(code=0, message=self.success_msg, data=token)
