#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.

"""
IRWorkflowBuilder - IR 到工作流的构造器

将商用九问的 IR JSON 转换为 agent-core 的 InvokableWorkflow 实例。
"""

from dataclasses import dataclass
from typing import Any, Optional
import time as _time

from agent_runtime.common.exception.errors import AgentBuilderError, ExtensionStatusCode
from openjiuwen.core.common.logging import performance_logger
from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.workflow import Workflow as InvokableWorkflow
from openjiuwen.core.workflow import WorkflowCard

from .components import BuildContext, BranchHandler
from .interfaces import ModelConfigProvider
from .registry import get_handler


@dataclass
class IRBuilderConfig:
    """构造器配置"""

    # 是否跳过未知节点类型（记录 warning 而不是抛出异常）
    skip_unknown: bool = True

    # 是否启用流式输出
    enable_stream: bool = True


class IRBuildException(AgentBuilderError):
    """IR 构造异常"""

    def __init__(self, msg: str = "", **kwargs):
        super().__init__(ExtensionStatusCode.IR_BUILD_ERROR, msg=msg, **kwargs)


class IRWorkflowBuilder:
    """
    IR → Workflow 构造器

    使用示例：
        from agent_runtime.ir_runner import IRWorkflowBuilder, ModelConfigProvider

        # model_provider 是必填参数，需要提供 ModelConfigProvider 实现
        builder = IRWorkflowBuilder(model_provider=my_model_provider)
        workflow = builder.build(ir_json)

        # 执行
        async def run():
            session = builder.create_session()
            async for chunk in workflow.stream(inputs, session):
                print(chunk)
    """

    def __init__(
        self,
        model_provider: ModelConfigProvider,
        config: Optional[IRBuilderConfig] = None,
    ):
        """
        初始化构造器

        Args:
            model_provider: 模型配置提供者（必填）
            config: 构造器配置
        """
        self._model_provider = model_provider
        self._config = config or IRBuilderConfig()

    def build(self, ir_json: dict) -> InvokableWorkflow:
        """
        从 IR JSON 构建可执行工作流

        Args:
            ir_json: IR JSON 对象

        Returns:
            InvokableWorkflow: 可执行的工作流实例

        Raises:
            IRBuildException: 当 IR 格式错误或缺少必需配置时
        """
        # 校验 IR 格式
        if not self._validate_ir(ir_json):
            raise IRBuildException("IR JSON 格式无效，缺少必需字段")

        t_build_start = _time.perf_counter()

        # 创建工作流实例
        workflow_id = ir_json.get("workflowId", "workflow")
        workflow_name = ir_json.get("workflowName", "Workflow")
        workflow_version = ir_json.get("workflowVersion", "draft")

        card = WorkflowCard(
            id=workflow_id,
            name=workflow_name,
            version=workflow_version,
        )
        workflow = InvokableWorkflow(card=card)

        # 生成节点 ID 映射
        node_id_map = self._generate_node_id_map(ir_json.get("components", []))

        # 构建上下文
        context = BuildContext(
            ir_json=ir_json,
            workflow=workflow,
            node_id_map=node_id_map,
            branch_components={},
            model_provider=self._model_provider,
            config=self._config,
        )

        # 第一阶段：注册节点
        t_reg_start = _time.perf_counter()
        self._register_components(workflow, ir_json, context)
        performance_logger.info(f"register_components|{round((_time.perf_counter() - t_reg_start) * 1000)}")

        # 第二阶段：建立连接
        t_conn_start = _time.perf_counter()
        self._register_connections(workflow, ir_json, context)
        performance_logger.info(f"register_connections|{round((_time.perf_counter() - t_conn_start) * 1000)}")

        # 第三阶段：完成分支组件的 finalization
        t_branch_start = _time.perf_counter()
        self._finalize_branches(workflow, context)
        performance_logger.info(f"finalize_branches|{round((_time.perf_counter() - t_branch_start) * 1000)}")

        performance_logger.info(f"ir_builder_build|{round((_time.perf_counter() - t_build_start) * 1000)}")
        workflow_logger.info(f"IR workflow built successfully: {workflow_id}")

        return workflow

    def create_session(self, session_id: Optional[str] = None) -> Any:
        """
        创建工作流会话

        Args:
            session_id: 会话 ID，默认自动生成

        Returns:
            WorkflowSession: 工作流会话
        """
        from openjiuwen.core.workflow import create_workflow_session

        return create_workflow_session(session_id=session_id)

    def _validate_ir(self, ir_json: dict) -> bool:
        """校验 IR JSON 格式"""
        if not isinstance(ir_json, dict):
            return False
        if "components" not in ir_json and "nodes" not in ir_json:
            return False
        return True

    def _generate_node_id_map(self, components: list[dict]) -> dict[str, str]:
        """生成节点 ID 映射"""
        node_id_map = {}
        for comp in components:
            original_id = comp.get("id", "")
            if original_id:
                node_id_map[original_id] = original_id
        return node_id_map

    def _register_components(
        self,
        workflow: InvokableWorkflow,
        ir_json: dict,
        context: BuildContext,
    ) -> None:
        """注册所有组件到工作流"""
        components = ir_json.get("components", [])

        for ir_node in components:
            ir_type = ir_node.get("type", "")
            handler = get_handler(ir_type)

            if handler is None:
                if self._config.skip_unknown:
                    workflow_logger.warning(f"未知的节点类型: {ir_type}，已跳过")
                    continue
                else:
                    raise IRBuildException(f"未知的节点类型: {ir_type}")

            try:
                original_id = ir_node.get("id", "")
                registered_id = context.node_id_map.get(original_id, original_id)

                component = handler.create_component(ir_node, context)
                handler.register(workflow, component, registered_id, ir_node, context)

                workflow_logger.debug(f"注册组件: {registered_id} ({ir_type})")

            except NotImplementedError:
                workflow_logger.warning(f"节点类型 {ir_type} 尚未实现，已跳过")
                continue
            except Exception as e:
                workflow_logger.error(
                    f"Component registration failed: {ir_type}, {e}", exc_info=True
                )
                raise IRBuildException(f"注册组件失败: {ir_type}, {e}") from e

    def _register_connections(
        self,
        workflow: InvokableWorkflow,
        ir_json: dict,
        context: BuildContext,
    ) -> None:
        """建立组件之间的连接"""
        connections = ir_json.get("connections", [])

        for conn in connections:
            source = conn.get("source", {})
            target = conn.get("target", {})
            branch_id = source.get("branchId")

            source_id = source.get("componentId", "")
            target_id = target.get("componentId", "")

            registered_source = context.node_id_map.get(source_id, source_id)
            registered_target = context.node_id_map.get(target_id, target_id)

            if branch_id:
                self._handle_branch_connection(workflow, conn, context)
            else:
                workflow.add_connection(registered_source, registered_target)
                workflow_logger.debug(
                    f"添加连接: {registered_source} -> {registered_target}"
                )

    def _handle_branch_connection(
        self,
        workflow: InvokableWorkflow,
        conn: dict,
        context: BuildContext,
    ) -> None:
        """处理分支连接"""
        source_id = conn["source"]["componentId"]
        target_id = conn["target"]["componentId"]

        if source_id not in context.node_id_map:
            return

        registered_source_id = context.node_id_map[source_id]
        registered_target_id = context.node_id_map.get(target_id, target_id)

        if registered_source_id not in context.branch_components:
            workflow_logger.warning(f"分支节点 {source_id} 未找到对应的组件")
            return

        branch_handler = get_handler("jiuwen.branch")
        if isinstance(branch_handler, BranchHandler):
            branch_handler.handle_branch_connection(workflow, conn, context)

    def _finalize_branches(
        self,
        workflow: InvokableWorkflow,
        context: BuildContext,
    ) -> None:
        """完成分支组件的 finalization"""
        branch_handler = get_handler("jiuwen.branch")
        if isinstance(branch_handler, BranchHandler):
            branch_handler.finalize(workflow, context)
