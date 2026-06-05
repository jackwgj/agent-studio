#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import List


class MsgContext:
    """
    planning assistance engine state

    Args:
        query_id: query id (removed in list-based version)
        query: query
        accessible_tools: accessible tools (renamed to functions for consistency)
    """

    def __init__(self, query: str = None, functions=None):
        if functions is None:
            functions = {}
        self._query = query
        self._functions = functions
        self._last_func = dict()
        self._intent_function_name = ""
        self._msgs: List[dict] = []
        self._tool_msg = ""
        self._is_finished = False
        self._is_terminated = False
        self._report_message = None

    def __str__(self):
        return (
            f"query: {self.query}\n"
            f"accessible tools: {self.functions}\n"
            f"message list: {self._msgs}"
        )

    @property
    def query(self):
        """get query"""
        return self._query

    @query.setter
    def query(self, q):
        self._query = q

    @property
    def dealing_node(self):
        """get steps"""
        return self._msgs[-1]

    @property
    def functions(self):
        """get functions (accessible tools)"""
        return self._functions

    @functions.setter
    def functions(self, tools):
        self._functions = tools

    @property
    def intent_function_name(self):
        """get intent function name"""
        return self._intent_function_name

    @intent_function_name.setter
    def intent_function_name(self, intent_function_name):
        self._intent_function_name = intent_function_name

    @property
    def last_func(self):
        """get last function call"""
        return self._last_func

    @last_func.setter
    def last_func(self, function_call):
        self._last_func = function_call

    @property
    def is_finished(self):
        """get if planning state is finished"""
        return self._is_finished

    @is_finished.setter
    def is_finished(self, is_finished):
        self._is_finished = is_finished

    @property
    def is_terminated(self):
        """get if planning state is finished"""
        return self._is_terminated

    @is_terminated.setter
    def is_terminated(self, is_terminated):
        self._is_terminated = is_terminated

    @property
    def report_message(self):
        """get report message"""
        return self._report_message

    @report_message.setter
    def report_message(self, report_message):
        self._report_message = report_message

    @property
    def tool_msg(self):
        """get tool message"""
        return self._tool_msg

    @tool_msg.setter
    def tool_msg(self, msg):
        self._tool_msg = msg

    @property
    def context(self):
        """
        Current status context information

        Returns:
            cur_context, Current status context information
        """
        cur_context = {
            "query": self._query,
            "functions": self._functions,
            "steps": self._msgs,
        }
        return cur_context

    def init_history(self, msgs):
        """init msgs with chat history"""
        self._msgs = msgs

    def clear(self):
        """
        clear planning state
        """
        self._query = None
        self._functions = None
        self._msgs = []  # clear steps list
        self._is_finished = False
        self._is_terminated = False

    def add(self, msg):
        """
        add a step to the plan
        """
        self._msgs.append(msg)  # append step to the list

    def delete_dealing_node(self):
        """eliminate dealing node"""
        self._msgs.pop()
