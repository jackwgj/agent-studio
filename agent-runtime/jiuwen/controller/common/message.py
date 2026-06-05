#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import uuid
from dataclasses import dataclass, field
from typing import Dict, Any, Optional, Union

from jiuwen.controller.common.message_type import MessageType, MessageSubType


@dataclass
class Message:
    """消息类，用于系统内部通信"""

    message_type: MessageType
    content: Union[str, dict]
    runtime_data: Dict[str, Any] = field(default_factory=dict)
    sub_type: Optional[MessageSubType] = None
    message_id: str = field(default_factory=lambda: str(uuid.uuid4()))

    def __init__(
        self,
        message_type: MessageType,
        content: Union[str, dict],
        runtime_data: Optional[Dict[str, Any]] = None,
        sub_type: Optional[MessageSubType] = None,
    ):
        """初始化消息对象

        Args:
            message_type: 消息类型
            content: 消息内容
            runtime_data: 元数据字典
            sub_type: 消息子类型
        """
        self.message_type = message_type
        self.content = content
        self.runtime_data = runtime_data or {}
        self.sub_type = sub_type
        self.message_id = str(uuid.uuid4())

    @property
    def is_workflow_interrupt(self) -> bool:
        """是否为工作流中断消息"""
        return self.message_type == MessageType.WORKFLOW_INTERRUPT

    @property
    def is_workflow_message(self) -> bool:
        """是否为工作流中断消息"""
        return self.message_type == MessageType.WORKFLOW_MESSAGE

    @property
    def is_workflow_completion(self) -> bool:
        """是否为工作流完成消息"""
        return self.message_type == MessageType.WORKFLOW_COMPLETION

    @property
    def is_user_input(self) -> bool:
        """是否为工作流完成消息"""
        return self.message_type == MessageType.USER_INPUT

    @property
    def is_global_intention(self) -> bool:
        """是否为工作流完成消息"""
        return (
            self.message_type == MessageType.WORKFLOW_INTERRUPT
            and self.sub_type == MessageSubType.GLOBAL_INTENT
        )

    @property
    def is_plugin_param_miss(self) -> bool:
        """是否为插件参数缺失消息"""
        return (
            self.message_type == MessageType.WORKFLOW_INTERRUPT
            and self.sub_type == MessageSubType.PLUGIN_PARAM_MISS
        )

    @property
    def is_global_intent(self) -> bool:
        """是否为插件参数缺失消息"""
        return (
            self.message_type == MessageType.WORKFLOW_INTERRUPT
            and self.sub_type == MessageSubType.GLOBAL_INTENT
        )

    @property
    def is_plugin_call_confirm(self) -> bool:
        """是否为插件调用确认消息"""
        return (
            self.message_type == MessageType.WORKFLOW_INTERRUPT
            and self.sub_type == MessageSubType.PLUGIN_CALL_CONFIRM
        )

    @property
    def workflow_id(self) -> Optional[str]:
        """获取关联的工作流ID"""
        return self.runtime_data.get("workflow_id")

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "Message":
        """从字典创建消息

        Args:
            data: 消息的字典表示

        Returns:
            创建的消息对象
        """
        message_type = (
            MessageType(data["type"]) if data.get("type") else MessageType.USER_INPUT
        )

        sub_type = None
        if data.get("sub_type"):
            try:
                sub_type = MessageSubType(data["sub_type"])
            except ValueError:
                # 子类型不存在，忽略
                pass

        msg = cls(
            message_type=message_type,
            content=data.get("content", ""),
            runtime_data=data.get("metadata", {}),
            sub_type=sub_type,
        )

        if "message_id" in data:
            msg.message_id = data["message_id"]

        return msg

    def to_dict(self) -> Dict[str, Any]:
        """将消息转换为字典

        Returns:
            消息的字典表示
        """
        result = {
            "type": self.message_type.value
            if isinstance(self.message_type, MessageType)
            else str(self.message_type),
            "content": self.content,
            "message_id": self.message_id,
            "metadata": self.runtime_data,
        }

        if self.sub_type:
            result["sub_type"] = (
                self.sub_type.value
                if isinstance(self.sub_type, MessageSubType)
                else str(self.sub_type)
            )

        return result
