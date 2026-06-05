#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
"""
runtime context
"""


class RuntimeContext:
    """
    Context manager during workflow execution
    """

    def __init__(self):
        self._context = {}

    def __str__(self):
        return str(self._context)

    def set(self, key, value):
        """
        设置上下文信息
        :param key: 上下文信息的键
        :param value: 与键对应的值
        """
        self._context[key] = value

    def get(self, key, default=None):
        """
        获取上下文信息
        :param key: 要获取的上下文信息的键
        :param default: 如果键不存在时返回的默认值
        :return: 与键对应的值，如果键不存在，则返回默认值
        """
        return self._context.get(key, default)

    def update(self, updates):
        """
        批量更新上下文信息
        :param updates: 一个包含多个键值对的字典
        """
        self._context.update(updates)

    def remove(self, key):
        """
        移除上下文中指定的键（如果存在）
        :param key: 要移除的键
        """
        if key in self._context:
            del self._context[key]

    def clear(self):
        """
        清除所有上下文信息
        """
        self._context.clear()
