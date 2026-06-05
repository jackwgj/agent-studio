#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import json

import requests

from ..messages.messages import BaseMessage


class LLMAPIClient:
    """API大模型访问客户端"""

    def __init__(
        self,
        model_name: str,
        api_key: str,
        api_base: str,
        temperature: float,
        top_p: float,
        timeout: float,
    ):
        """初始化客户端"""
        self.model_name = model_name
        self.api_key = api_key
        self.api_base = api_base
        self.temperature = temperature
        self.top_p = top_p
        self.timeout = timeout
        self.headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.api_key}",
        }

    def invoke(self, input_prompt: list) -> BaseMessage:
        """
        调用模型生成响应

        参数:
            input_prompt: 输入的提示文本

        返回:
            模型生成的响应文本
        """
        # 构造请求体，根据实际API文档调整参数
        payload = {
            "model": self.model_name,
            "messages": input_prompt,
            "temperature": self.temperature,
        }

        # 发送POST请求
        response = requests.post(
            url=self.api_base,
            headers=self.headers,
            data=json.dumps(payload),
            timeout=self.timeout,
        )

        # 检查响应状态
        response.raise_for_status()

        # 解析响应，根据实际API返回格式调整
        result = response.json()
        return BaseMessage(
            type="system", content=str(result["choices"][0]["message"]["content"])
        )
