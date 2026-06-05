#!/usr/bin/env python
# coding=utf-8
#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import List, Dict

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.controller.utils.utils import WorkspaceUtils
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.common.exception import TaskPluginException
from jiuwen.orchestration.flow.enum import NodeType
from jiuwen.orchestration.flow.workflow import sync_build_workflow, build_workflow
from jiuwen.planner.common.enum_class import ProviderType
from jiuwen.plugin import PluginManager
from jiuwen.plugin.common.exception import ErrorCode as plugin_ErrCode
from jiuwen.plugin.models.mcpapi import McpAPI
from jiuwen.plugin.models.plugin import BasePlugin
from jiuwen.serve.common.context import request, g, request_json
from jiuwen.serve.controllers.execution.open_utils import async_ir_load
from rag.models.tool import Tool

WORKFLOW_NAME = "workflowName"
DESCRIPTION = "description"
WORKFLOW_PROMPT = "无需提供properties以外的参数信息"


class WorkSpace:
    def __init__(
        self, workflows=None, plugins=None, plugin_retrieve_enable=False, top_k=3
    ):
        self.workflows = workflows
        self.plugin_manager = PluginManager()
        self.workflow_map = dict()  # 工作流实例字典
        self.plugins = self.init_plugins(plugins)  # 插件实例字典
        self.mcps = []
        self.rec_plugin = plugin_retrieve_enable
        self.top_k_functions = top_k

    def init_plugins(self, plugins):
        """初始化plugins"""
        if plugins:
            # plugin names in dict
            if isinstance(plugins, dict):
                functions = self._get_plugin_from_dict(plugins)
            # plugin name
            elif isinstance(plugins, str):
                functions = self._get_plugin_by_name(plugins)
            # plugin instance list
            elif isinstance(plugins, (BasePlugin, Invokable)):
                functions = [plugins]
            elif isinstance(plugins, list):
                functions = plugins
            else:
                raise ValueError("Unsupported type for plugin functions")
        else:
            functions = []
        functions = WorkspaceUtils.get_plugin_from_list(functions)
        return functions

    async def gen_tools(self, input_content: str) -> List[Tool]:
        """生成function_call tool候选list"""
        tools = []
        output_plugins = self._get_all_plugins(input_content)
        for plugin in output_plugins:
            if not isinstance(plugin, Invokable):
                logger.error("plugin type error while wrap plugin into Tool")
                continue
            tools.append(
                Tool(
                    tool_id=plugin.name,
                    provider_type=ProviderType.PLUGIN,
                    invokable=plugin,
                )
            )
        # 获取mcp apis
        mcp_apis = await self._get_all_mcp_apis()

        for mcp_api in mcp_apis:
            tools.append(
                Tool(
                    tool_id=mcp_api.name,
                    provider_type=ProviderType.MCP,
                    invokable=mcp_api,
                    parameters=mcp_api.parameters,
                )
            )
        current_ids = list()
        if self.workflows:
            for workflow in self.workflows:
                if isinstance(workflow, dict):
                    # 适配平台ir传入的逻辑
                    workflow_name = workflow.get("workflowName")
                    params = WorkspaceUtils.get_workflow_params(workflow)
                    workflow_instance = sync_build_workflow(
                        workflow, global_variables={}, is_workflow_mode=False
                    )
                    workflow = Tool(
                        tool_id=workflow_name,
                        provider_type=ProviderType.WORKFLOW,
                        invokable=workflow_instance,
                        parameters=params,
                    )
                current_ids.append(workflow.tool_id)
                if workflow.tool_id not in self.workflow_map:
                    self.workflow_map.update({workflow.tool_id: workflow})
        if isinstance(self.workflow_map, dict):
            self.workflow_map = {
                key: self.workflow_map.get(key)
                for key in self.workflow_map
                if key in current_ids
            }
            tools.extend(list(self.workflow_map.values()))
        return tools

    def _get_plugin_from_dict(
        self, plugin_dict: Dict[str, List[str]]
    ) -> List[Invokable]:
        """plugin_dict is a map from plugin name to a list of function names."""
        function_or_api_list = list()
        for plugin_name, functions_or_rest_apis in plugin_dict.items():
            if not isinstance(functions_or_rest_apis, (list, tuple, set)):
                msg = (
                    f"The functions of plugin {plugin_name} should be a list/tuple/set of str"
                    f" but got {type(functions_or_rest_apis).__name__}"
                )
                logger.error(msg)
                raise TaskPluginException(
                    StatusCode.TASK_PLUGIN_TYPE_ERROR.code,
                    StatusCode.TASK_PLUGIN_TYPE_ERROR.errmsg.format(
                        plugin_name, type(functions_or_rest_apis).__name__
                    ),
                )
            for func_name in functions_or_rest_apis:
                # Plugin name and function name are checked by plugin_manager.
                function_or_api_list.extend(
                    WorkspaceUtils.plugin_operate(
                        self.plugin_manager.get(
                            plugin_name=plugin_name, func_name=func_name
                        ),
                        f"Get plugin by {plugin_name=} and {func_name=}",
                        plugin_ErrCode,
                    )
                )
        return function_or_api_list

    def _get_plugin_by_name(self, plugin_name: str) -> List[Invokable]:
        """
        get plugin by name
        Args:
            plugin_name:

        Returns:

        """
        return WorkspaceUtils.plugin_operate(
            self.plugin_manager.get(plugin_name=plugin_name),
            f"Get plugin by {plugin_name=}",
            plugin_ErrCode,
        )

    def _get_all_plugins(self, instruction) -> list:
        """Return a union of user input and recommend plugins for each instruction"""
        output_plugins = list()
        if self.rec_plugin:
            recommend_plugins = self.plugin_manager.retrieve(
                query=instruction, top_k=self.top_k_functions
            )
            redundant_plugins = recommend_plugins
            if self.plugins and isinstance(self.plugins, list):
                redundant_plugins += self.plugins
            for plugin in redundant_plugins:
                if plugin in output_plugins:
                    continue
                output_plugins.append(plugin)
        else:
            output_plugins = self.plugins
        return output_plugins

    async def _get_all_mcp_apis(self) -> list:
        """
        get all mcp apis of server
        :return: mcp apis
        """
        mcp_apis = []
        for mcp in self.mcps:
            mcp_tools = mcp.ainvoke(inputs={})
            if mcp_tools:
                # 等待异步方法完成
                mcp_tools_result = await mcp_tools
                if mcp_tools_result and hasattr(mcp_tools_result, "tools"):
                    for mcp_tool in mcp_tools_result.tools:
                        mcp_apis.append(
                            McpAPI(
                                server_url=mcp.server_url,
                                name=mcp_tool.name,
                                parameters=mcp_tool.inputSchema,
                                headers=mcp.headers,
                                description=mcp_tool.description,
                                server_name=mcp.name,
                                transport_type=mcp.transport_type,
                                auth=mcp.auth,
                            )
                        )
        return mcp_apis

    @staticmethod
    def get_workflow_params(workflow_ir: dict) -> dict:
        """格式化workflow Panel接口"""
        return WorkspaceUtils.get_workflow_params(workflow_ir)

    @staticmethod
    async def create_workflow_instance(workflow_ir):
        """创建工作流实例"""
        headers = dict(request.get().headers)
        project_id = g.get().get("project_id", "")
        conversation_id = request_json.get().get("conversationId", "")
        user_id = request_json.get().get("userId", "")
        wf = await build_workflow(
            workflow_ir,
            project_id=project_id,
            x_auth_token=headers.get("X-Auth-Token"),
            cust_headers=headers,
            conversation_id=conversation_id,
            is_workflow_mode=False,
            user_id=user_id,
        )
        return wf

    @staticmethod
    async def get_workflow_instance(ir_data: dict):
        """get workflow instance"""
        workflow_ir_paths = ir_data.get("configs", {}).get("workflows", [])
        workflows = []

        headers = dict(request.get().headers)
        project_id = g.get().get("project_id", "")
        conversation_id = request_json.get().get("conversationId", "")
        user_id = request_json.get().get("userId", "")

        for workflow_info in workflow_ir_paths:
            workflow_ir = await async_ir_load(workflow_info.get("ir_path"))
            component_list = workflow_ir.get("components", [])
            # 排包含有task组件的workflow 防止无穷嵌套
            for component in component_list:
                type_value = component.get("type")
                if not type_value or type_value == NodeType.TASK.value:
                    raise JiuWenBaseException(
                        error_code=StatusCode.IR_DATA_VALIDATION_ERROR.code,
                        message=f"workflow ir contains task components or None, value is {type_value}",
                    )
            workflow_params = WorkSpace.get_workflow_params(workflow_ir)
            wf = await build_workflow(
                workflow_ir,
                project_id=project_id,
                x_auth_token=headers.get("X-Auth-Token"),
                cust_headers=headers,
                conversation_id=conversation_id,
                is_workflow_mode=False,
                user_id=user_id,
            )
            workflow_tool = Tool(
                tool_id=workflow_ir.get("workflowName"),
                provider_type=ProviderType.WORKFLOW,
                invokable=wf,
                parameters=workflow_params,
            )
            workflows.append(workflow_tool)
        return workflows
