#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
场景匹配模块

职责：
- 根据用户查询和对话历史，从可用场景列表中匹配最合适的场景
- 如果没有匹配的场景，返回 None
"""

from typing import List, Tuple, Optional, Dict

from jiuwen.common.llm_service.messages import HumanMessage, SystemMessage
from jiuwen.common.log.base import logger
from jiuwen.controller.common.config import SceneConfig
from jiuwen.prompt.template.template_manager import TemplateManager


class SceneMatchModule:
    """
    场景匹配模块

    使用 LLM 根据用户查询匹配最合适的场景
    """

    def __init__(self, llm, context_manager):
        """
        初始化场景匹配模块

        Args:
            llm: 语言模型实例
            context_manager: 上下文管理器
        """
        self.llm = llm
        self.context_manager = context_manager
        self.template_manager = TemplateManager()

    async def match(
        self,
        query: str,
        chat_history: List[Dict],
        scenes: List[SceneConfig],
        task_id: str = "",
    ) -> Tuple[Optional[SceneConfig], float]:
        """
        匹配场景

        Args:
            query: 用户查询
            chat_history: 对话历史
            scenes: 可用场景列表
            task_id: 任务ID，用于日志追踪

        Returns:
            (matched_scene, confidence): 匹配的场景和置信度
        """
        if not scenes:
            logger.info(
                f"task_id: {task_id}| No scenes available for matching",
                simple_log=f"task_id: {task_id}| No scenes available for matching",
            )
            return None, 0.0

        # 构建提示词
        system_prompt = self._build_system_prompt()
        user_prompt = self._build_user_prompt(query, chat_history, scenes)

        # 调用 LLM
        messages = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=user_prompt),
        ]

        # 构建 LLM 输入日志
        llm_input = {
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ]
        }
        logger.info(
            f"task_id: {task_id}| [SceneMatch] <LLM Input> {llm_input}",
            simple_log=f"task_id: {task_id}| [SceneMatch] <LLM Input>",
        )

        try:
            # 调用 LLM (使用 ainvoke 异步调用)
            result = await self.llm.ainvoke(messages)

            # 打印 LLM 输出日志
            logger.info(
                f"task_id: {task_id}| [SceneMatch] <LLM Output> {result.content}",
                simple_log=f"task_id: {task_id}| [SceneMatch] <LLM Output>",
            )

            # 解析结果
            scene_id = result.content.strip()
            logger.info(
                f"task_id: {task_id}| Scene matching result: {scene_id}",
                simple_log=f"task_id: {task_id}| Scene matching result",
            )

            # 查找匹配的场景
            if scene_id.lower() == "none":
                return None, 0.0

            for scene in scenes:
                if scene.id == scene_id:
                    logger.info(
                        f"task_id: {task_id}| Matched scene: {scene.name} (id={scene.id})",
                        simple_log=f"task_id: {task_id}| Matched scene: {scene.name}",
                    )
                    return scene, 1.0

            # 如果没有找到精确匹配，尝试模糊匹配
            logger.warning(
                f"task_id: {task_id}| Scene ID {scene_id} not found in available scenes",
                simple_log=f"task_id: {task_id}| Scene ID not found",
            )
            return None, 0.0

        except Exception as e:
            logger.error(
                f"task_id: {task_id}| Error during scene matching: {e}",
                simple_log=f"task_id: {task_id}| Error during scene matching",
            )
            raise

    def _build_system_prompt(self) -> str:
        """
        构建系统提示词

        Returns:
            系统提示词字符串
        """
        template = self.template_manager.get("plan_execute_scene_match")
        # 获取 system 角色的内容（第一个元素）
        for msg in template.content:
            if msg.get("role") == "system":
                return msg.get("content", "")
        return ""

    def _build_user_prompt(
        self, query: str, chat_history: List[Dict], scenes: List[SceneConfig]
    ) -> str:
        """
        构建用户提示词

        Args:
            query: 用户查询
            chat_history: 对话历史
            scenes: 可用场景列表

        Returns:
            用户提示词字符串
        """
        # 构建场景列表
        scenes_text = "## 可用场景列表\n\n"
        for scene in scenes:
            scenes_text += f"- 场景ID: {scene.id}\n"
            scenes_text += f"  场景名称: {scene.name}\n"
            scenes_text += f"  场景描述: {scene.description}\n\n"

        # 构建对话历史
        history_text = ""
        if chat_history:
            history_text = "## 对话历史\n\n"
            for msg in chat_history[-5:]:  # 只取最近5轮
                role = msg.get("role", "user")
                content = msg.get("content", "")
                history_text += f"{role}: {content}\n"
            history_text += "\n"

        # 使用模板加载用户提示词
        template = self.template_manager.get("plan_execute_scene_match")
        user_prompt_template = ""
        # 获取 user 角色的内容
        for msg in template.content:
            if msg.get("role") == "user":
                user_prompt_template = msg.get("content", "")
                break

        # 替换模板变量
        user_prompt = user_prompt_template.replace("{{scenes_text}}", scenes_text)
        user_prompt = user_prompt.replace("{{history_text}}", history_text)
        user_prompt = user_prompt.replace("{{query}}", query)

        return user_prompt
