#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
计划跟踪模块

包含：
- PlanStep: 计划步骤数据结构
- ExecutionPlan: 执行计划数据结构
- StepStatus: 步骤状态枚举
- PlanTracker: 计划跟踪器，管理执行计划状态
- HintBuilder: 引导消息构建器，构建四种类型的 hint 消息
"""

from dataclasses import dataclass
from enum import Enum
from typing import List, Optional

from jiuwen.controller.common.config import SceneConfig
from jiuwen.prompt import TemplateManager


class StepStatus(str, Enum):
    """步骤状态枚举"""

    PENDING = "pending"  # 待执行
    IN_PROGRESS = "in_progress"  # 执行中
    COMPLETED = "completed"  # 已完成
    SKIPPED = "skipped"  # 已跳过
    FAILED = "failed"  # 失败


@dataclass
class PlanStep:
    """计划步骤"""

    step_idx: int
    step_name: str
    description: str
    status: StepStatus = StepStatus.PENDING
    result: Optional[str] = None  # 步骤执行结果（来自 LLM 标记完成时的总结）
    interrupted_workflow_name: Optional[str] = (
        None  # 被中断的工作流名称，用于恢复时直接注入 tool call
    )


@dataclass
class ExecutionPlan:
    """执行计划"""

    plan_id: str
    steps: List[PlanStep]
    current_step_idx: int = 1
    matched_scene: Optional[SceneConfig] = None  # 保存匹配的场景信息


class PlanTracker:
    """
    计划跟踪器

    职责：
    - 管理执行计划状态
    - 跟踪当前步骤、已完成步骤及其结果
    - 管理临时消息的添加和清理
    - 判断任务是否完成
    """

    def __init__(self):
        self.plan: Optional[ExecutionPlan] = None
        self.current_step_idx: int = 1

    def init_plan(self, plan: ExecutionPlan):
        """
        初始化执行计划

        Args:
            plan: 执行计划对象
        """
        self.plan = plan
        self.current_step_idx = plan.current_step_idx

    def get_current_step(self) -> Optional[PlanStep]:
        """
        获取当前步骤

        Returns:
            当前步骤对象，如果已完成则返回 None
        """
        if not self.plan or self.current_step_idx > len(self.plan.steps):
            return None
        return self.plan.steps[self.current_step_idx - 1]

    def mark_step_completed(self, step_idx: int, result: str = ""):
        """
        标记步骤完成

        Args:
            step_idx: 步骤序号（从1开始）
            result: 步骤执行结果
        """
        if not self.plan or step_idx < 1 or step_idx > len(self.plan.steps):
            return
        step = self.plan.steps[step_idx - 1]
        step.status = StepStatus.COMPLETED
        step.result = result

        # 更新当前步骤索引
        if step_idx == self.current_step_idx:
            self.current_step_idx += 1
            self.plan.current_step_idx = self.current_step_idx

    def mark_step_skipped(self, step_idx: int):
        """
        标记步骤跳过

        Args:
            step_idx: 步骤序号（从1开始）
        """
        if not self.plan or step_idx < 1 or step_idx > len(self.plan.steps):
            return

        step = self.plan.steps[step_idx - 1]
        step.status = StepStatus.SKIPPED

        # 更新当前步骤索引
        if step_idx == self.current_step_idx:
            self.current_step_idx += 1
            self.plan.current_step_idx = self.current_step_idx

    def mark_step_failed(self, step_idx: int, error: str = ""):
        """
        标记步骤失败

        Args:
            step_idx: 步骤序号（从1开始）
            error: 错误信息
        """
        if not self.plan or step_idx < 1 or step_idx > len(self.plan.steps):
            return

        step = self.plan.steps[step_idx - 1]
        step.status = StepStatus.FAILED
        step.result = error

    def is_completed(self) -> bool:
        """
        判断任务是否完成

        Returns:
            任务是否完成
        """
        if not self.plan:
            return False

        # 当前步骤索引超过总步骤数，说明任务完成
        return self.current_step_idx > len(self.plan.steps)

    def get_completed_steps(self) -> List[PlanStep]:
        """
        获取已完成的步骤列表

        Returns:
            已完成步骤列表
        """
        if not self.plan:
            return []

        return [step for step in self.plan.steps if step.status == StepStatus.COMPLETED]


class HintBuilder:
    """
    引导消息构建器

    职责：构建 <system-hint> 格式的引导消息，包含四种类型：
    1. 开始执行步骤
    2. 步骤执行中
    3. 步骤完成，进入下一步
    4. 所有步骤完成
    """

    def __init__(self):
        self.template_manager = TemplateManager()
        # 加载统一的 hint 模板
        self.hint_template = self.template_manager.get("plan_execute_hint")

    @staticmethod
    def _get_step_state_symbol(status: StepStatus) -> str:
        """
        获取步骤状态符号

        Args:
            status: 步骤状态

        Returns:
            状态符号字符串
        """
        if status == StepStatus.COMPLETED:
            return "x"
        if status == StepStatus.SKIPPED:
            return "-"
        if status == StepStatus.FAILED:
            return "!"
        return " "

    def build_hint_start_step(self, plan: ExecutionPlan) -> str:
        """
        构建开始执行步骤的 hint

        Args:
            plan: 执行计划

        Returns:
            hint 消息字符串
        """
        steps_text = self._build_steps_text(plan)
        current_step = plan.steps[plan.current_step_idx - 1]

        # 准备模板参数
        inputs = {
            "steps_text": steps_text,
            "current_step_info": f"当前正在执行第{plan.current_step_idx}步：{current_step.step_name}，{current_step.description}",
            "actions": "调用相关工具完成当前步骤，完成后总结并在回复末尾输出 [STEP_DONE]",
        }

        # 使用 Prompt 和 Template 替换变量
        return self._render_template(inputs)

    def build_hint_step_in_progress(self, plan: ExecutionPlan) -> str:
        """
        构建步骤执行中的 hint

        Args:
            plan: 执行计划

        Returns:
            hint 消息字符串
        """
        steps_text = self._build_steps_text(plan, mark_in_progress=True)
        current_step = plan.steps[plan.current_step_idx - 1]

        # 准备模板参数
        inputs = {
            "steps_text": steps_text,
            "current_step_info": f"当前正在执行第{plan.current_step_idx}步：{current_step.step_name}，{current_step.description}",
            "actions": f"1. 继续完成第{plan.current_step_idx}步\n2. 步骤完成后总结当前步骤结果并在回复末尾输出 [STEP_DONE]",
        }

        # 使用 Prompt 和 Template 替换变量
        return self._render_template(inputs)

    def build_hint_step_completed(self, plan: ExecutionPlan) -> str:
        """
        构建步骤完成、进入下一步的 hint

        Args:
            plan: 执行计划

        Returns:
            hint 消息字符串
        """
        steps_text = self._build_steps_text(plan)
        current_step = plan.steps[plan.current_step_idx - 1]

        # 准备模板参数
        inputs = {
            "steps_text": steps_text,
            "current_step_info": f"当前正在执行第{plan.current_step_idx}步：{current_step.step_name}，{current_step.description}",
            "actions": "调用相关工具完成当前步骤，完成后总结当前步骤结果并在回复末尾输出 [STEP_DONE]",
        }

        # 使用 Prompt 和 Template 替换变量
        return self._render_template(inputs)

    def build_hint_all_completed(self, plan: ExecutionPlan) -> str:
        """
        构建所有步骤完成的 hint

        Args:
            plan: 执行计划

        Returns:
            hint 消息字符串
        """
        steps_text = self._build_steps_text(plan)

        # 准备模板参数
        inputs = {
            "steps_text": steps_text,
            "current_step_info": "所有步骤已完成。",
            "actions": "1. 任务成功，综合所有步骤的执行结果，输出完整的任务完成结果（必须包含关键代码、计算结果、重要输出等实质性内容，不要仅做概括性描述）并结束\n2. 任务失败，输出任务失败原因",
        }

        # 使用 Prompt 和 Template 替换变量
        return self._render_template(inputs)

    def _render_template(self, inputs: dict) -> str:
        """
        渲染模板，替换变量

        Args:
            inputs: 模板变量字典

        Returns:
            渲染后的字符串
        """
        # 获取模板内容
        template_content = self.hint_template.content

        # 提取模板字符串
        if isinstance(template_content, list) and len(template_content) > 0:
            # 如果是列表，取第一个元素的content字段
            prompt_message = template_content[0]
            if isinstance(prompt_message, dict):
                template_str = prompt_message.get("content", "")
            else:
                template_str = str(prompt_message)
        else:
            template_str = str(template_content)

        # 使用字符串替换填充变量
        result = template_str
        for key, value in inputs.items():
            result = result.replace(f"{{{{{key}}}}}", str(value))

        return result

    def _build_steps_text(
        self, plan: ExecutionPlan, mark_in_progress: bool = False
    ) -> str:
        """
        构建步骤列表文本

        Args:
            plan: 执行计划
            mark_in_progress: 是否标记执行中的步骤

        Returns:
            步骤列表文本
        """
        steps_text = ""
        for step in plan.steps:
            state = self._get_step_state_symbol(step.status)
            in_progress_mark = (
                "（执行中）"
                if mark_in_progress and step.status == StepStatus.IN_PROGRESS
                else ""
            )
            result_text = f": {step.result}" if step.result else ""
            steps_text += f"- [{state}] {step.step_name}：{step.description}{in_progress_mark}{result_text}\n"
        return steps_text
