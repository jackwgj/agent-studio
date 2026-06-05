#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
import secrets


class Span:
    def __init__(self, parent_id=None, trace_id="", span_id=""):
        self._span_id = span_id or str(secrets.token_hex(24))
        self._parent_id = parent_id
        self._trace_id = trace_id

    @property
    def span_id(self):
        """获取span_id"""
        return self._span_id

    @property
    def parent_id(self):
        """获取parent_id"""
        return self._parent_id

    @property
    def trace_id(self):
        """获取trace_id"""
        return self._trace_id
